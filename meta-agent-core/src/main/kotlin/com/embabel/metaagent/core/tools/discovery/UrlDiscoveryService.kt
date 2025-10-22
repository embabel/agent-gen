/*
 * Copyright 2024-2025 Embabel Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.embabel.metaagent.core.tools.discovery

import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.core.CoreToolGroups
import com.embabel.common.ai.model.LlmOptions
import com.embabel.common.ai.model.ModelSelectionCriteria.Companion.Auto
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Stage 1A.5: URL Discovery Service
 * 
 * Discovers documentation and schema URLs for API providers using LLM + Web Search.
 * Takes provider names from Stage 1A and generates candidate URLs for Stage 1B verification.
 * 
 * Enhanced with web search capabilities for real-time accuracy.
 */
@Component
class UrlDiscoveryService {

    private val logger = LoggerFactory.getLogger(UrlDiscoveryService::class.java)

    /**
     * Discover candidate URLs for a provider using LLM + Web Search.
     * 
     * @param providerName The provider name from Stage 1A (e.g., "OpenTable", "Yelp")
     * @param context Operation context for LLM integration
     * @return List of candidate URL patterns for verification
     */
    fun discoverProviderUrls(providerName: String, context: OperationContext): List<UrlCandidate> {
        logger.info("🔍 Stage 1A.5: Discovering URLs for provider: $providerName (plain LLM)")
        
        return try {
            logger.info("🤖 Plain LLM: Finding known documentation URLs for $providerName")
            
            val prompt = buildPlainLlmUrlPrompt(providerName)
            logger.info("📝 LLM Prompt: $prompt")
            
            val urlDiscovery = context.promptRunner()
                .withLlm(LlmOptions(criteria = Auto))
                .createObject(
                    prompt,
                    LlmUrlDiscovery::class.java
                )
            
            logger.info("🔍 Raw LLM Response - candidateUrls size: ${urlDiscovery.candidateUrls.size}")
            urlDiscovery.candidateUrls.forEachIndexed { index, candidate ->
                logger.info("  [$index] URL: '${candidate.url}', Type: '${candidate.type}', Confidence: ${candidate.confidence}")
            }
            
            val llmResults = urlDiscovery.candidateUrls.map { llmUrl ->
                UrlCandidate(
                    url = llmUrl.url,
                    type = parseUrlType(llmUrl.type),
                    confidence = llmUrl.confidence,
                    description = llmUrl.description
                )
            }
            
            logger.info("✅ LLM processed ${llmResults.size} URL candidates")
            
            // If LLM returned no results, search engine integration needed
            if (llmResults.isEmpty()) {
                logger.warn("⚠️ LLM returned 0 results for $providerName - search engine integration needed")
                emptyList() // Remove fallback patterns - they're useless
            } else {
                llmResults
            }
            
        } catch (e: Exception) {
            logger.error("❌ LLM URL discovery failed for $providerName: ${e.message}")
            logger.error("📋 Exception details: ${e.javaClass.simpleName} - ${e.localizedMessage}")
            emptyList() // No more fallback patterns
        }
    }

    /**
     * Build LLM prompt for plain URL discovery using LLM knowledge.
     * Uses LLM's training data knowledge of API documentation URLs.
     */
    private fun buildPlainLlmUrlPrompt(providerName: String): String {
        return """
        You are an expert in API documentation and developer resources.
        
        Task: Provide the official API documentation and schema URLs for "${providerName}" based on your knowledge.
        
        Provider: ${providerName}
        
        Please provide known, real documentation URLs for ${providerName} API based on your training data knowledge.
        
        I need:
        1. Official API documentation sites for ${providerName}
        2. Developer portal URLs for ${providerName}
        3. API reference documentation for ${providerName}
        4. OpenAPI/Swagger specification URLs if known
        
        Focus on providing:
        - Official developer documentation sites
        - API reference guides and getting started documentation  
        - OpenAPI specifications and Swagger documentation
        - Schema endpoints and API specification files
        
        IMPORTANT:
        - Only provide URLs that you are confident exist based on your knowledge
        - Use standard patterns you know for ${providerName} (docs.${providerName.lowercase()}.com, developer.${providerName.lowercase()}.com, etc.)
        - Include confidence scores based on your certainty about each URL
        - Prioritize official documentation over third-party resources
        
        For each URL, provide:
        - url: The actual URL based on your knowledge
        - type: URL type (DOCUMENTATION, SCHEMA, API_REFERENCE)  
        - confidence: Your confidence in this URL existing (0.0-1.0)
        - description: What type of content this URL typically contains
        
        Return only URLs you know or can reasonably infer for ${providerName}.
        Return only the JSON response with the candidateUrls array.
        """.trimIndent()
    }
    
    /**
     * Build LLM prompt for web search enhanced URL discovery.
     * Uses web search to find current, accurate documentation URLs.
     */
    private fun buildWebSearchUrlPrompt(providerName: String): String {
        return """
        You are an expert in API documentation research with access to web search.
        
        Task: Find the official API documentation and schema URLs for "${providerName}" using web search.
        
        Provider: ${providerName}
        
        Please search the web for:
        1. Official API documentation for ${providerName}
        2. Developer documentation sites for ${providerName}
        3. OpenAPI/Swagger schema URLs for ${providerName} API
        4. API reference documentation for ${providerName}
        
        Focus on finding:
        - Official developer portals and documentation sites
        - API reference guides and getting started documentation
        - OpenAPI specifications and Swagger documentation
        - Schema endpoints and API specification files
        
        Use web search to find current, accurate URLs. Look for:
        - docs.${providerName.lowercase()}.com patterns
        - developer.${providerName.lowercase()}.com patterns  
        - ${providerName.lowercase()}.com/developers patterns
        - API schema endpoints like /openapi.json, /swagger.yaml
        
        IMPORTANT:
        - Only return URLs that you find through web search
        - Verify these are official documentation sites for ${providerName}
        - Include confidence scores based on search result quality
        - Prioritize official developer documentation over third-party sites
        
        For each URL found, provide:
        - url: The actual URL found through web search
        - type: URL type (DOCUMENTATION, SCHEMA, API_REFERENCE)
        - confidence: Your confidence based on search results (0.0-1.0)
        - description: What type of content this URL contains
        
        Return only URLs discovered through web search for ${providerName}.
        Return only the JSON response with the candidateUrls array.
        """.trimIndent()
    }

    /**
     * Generate basic URL patterns as fallback when web search fails.
     * TODO: Replace with search engine integration (Brave Search, etc.)
     */
    private fun generateBasicUrlPatterns(providerName: String): List<UrlCandidate> {
        // Empty for now - will be replaced with search engine integration
        return emptyList()
    }
    
    private fun parseUrlType(typeString: String): UrlType {
        return when (typeString.uppercase()) {
            "DOCUMENTATION" -> UrlType.DOCUMENTATION
            "SCHEMA" -> UrlType.SCHEMA
            "API_REFERENCE" -> UrlType.API_REFERENCE
            else -> UrlType.DOCUMENTATION
        }
    }
}

/**
 * LLM response structure for URL discovery.
 */
@JsonClassDescription("URL discovery results from LLM + web search analysis")
data class LlmUrlDiscovery(
    @JsonPropertyDescription("List of candidate URLs found through web search")
    val candidateUrls: List<LlmUrlSuggestion> = emptyList()
)

@JsonClassDescription("Individual URL suggestion from LLM + web search")
data class LlmUrlSuggestion(
    @JsonPropertyDescription("The URL found through web search")
    val url: String,
    
    @JsonPropertyDescription("URL type: DOCUMENTATION, SCHEMA, API_REFERENCE")
    val type: String,
    
    @JsonPropertyDescription("Confidence based on search result quality (0.0-1.0)")
    val confidence: Double,
    
    @JsonPropertyDescription("Description of what this URL contains")
    val description: String
)

/**
 * URL candidate for verification.
 */
data class UrlCandidate(
    val url: String,
    val type: UrlType,
    val confidence: Double,
    val description: String
)

/**
 * Types of URLs we discover.
 */
enum class UrlType {
    DOCUMENTATION,      // Human-readable API documentation
    SCHEMA,            // Machine-readable API schemas (OpenAPI, Swagger)
    API_REFERENCE      // API reference documentation
}