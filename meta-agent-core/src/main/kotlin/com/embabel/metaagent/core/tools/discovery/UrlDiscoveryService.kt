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

import com.embabel.metaagent.search.BraveWebSearchService
import com.embabel.metaagent.search.WebSearchRequest
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.core.CoreToolGroups
import com.embabel.common.ai.model.LlmOptions
import com.embabel.common.ai.model.ModelSelectionCriteria.Companion.Auto
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
class UrlDiscoveryService(
    @Autowired(required = false) private val braveWebSearchService: BraveWebSearchService?
) {

    private val logger = LoggerFactory.getLogger(UrlDiscoveryService::class.java)
    
    companion object {
        @Volatile
        private var lastBraveApiCall: Long = 0
        private const val BRAVE_API_DELAY_MS = 2000L
    }

    /**
     * Discover candidate URLs for a provider using LLM + Web Search.
     * 
     * @param providerName The provider name from Stage 1A (e.g., "OpenTable", "Yelp")
     * @param context Operation context for LLM integration
     * @return List of candidate URL patterns for verification
     */
    fun discoverProviderUrls(providerName: String, context: OperationContext): List<UrlCandidate> {
        logger.info("🔍 Stage 1A.5: Discovering URLs for provider: $providerName using Brave search")
        
        return if (braveWebSearchService != null) {
            try {
                // Provider-level rate limiting for Brave API
                synchronized(this) {
                    val timeSinceLastCall = System.currentTimeMillis() - lastBraveApiCall
                    if (timeSinceLastCall < BRAVE_API_DELAY_MS) {
                        val sleepTime = BRAVE_API_DELAY_MS - timeSinceLastCall
                        logger.info("⏳ Provider rate limiting: waiting ${sleepTime}ms before processing $providerName")
                        Thread.sleep(sleepTime)
                    }
                    lastBraveApiCall = System.currentTimeMillis()
                }
                
                logger.info("🔍 Using Brave search for $providerName URL discovery")
                val braveResults = searchWithBrave(providerName)
                logger.info("✅ Brave search found ${braveResults.size} URL candidates")
                braveResults
            } catch (e: Exception) {
                logger.error("❌ Brave search failed for $providerName: ${e.message}")
                emptyList()
            }
        } else {
            logger.warn("⚠️ Brave search not available - BRAVE_API_KEY required for URL discovery")
            emptyList()
        }
    }

    /**
     * Search for API documentation URLs using Brave search.
     * 
     * @param providerName The provider name to search for
     * @return List of URL candidates found via Brave search
     */
    private fun searchWithBrave(providerName: String): List<UrlCandidate> {
        return try {
            logger.info("🔍 Brave: Searching for $providerName API resources")
            
            val allUrlCandidates = mutableListOf<UrlCandidate>()
            
            // Single comprehensive search query to avoid rate limiting
            val searchQuery = "$providerName API documentation developer OpenAPI swagger reference guide"
            
            try {
                val searchRequest = WebSearchRequest(query = searchQuery, count = 10) // More results in single query
                val braveResults = braveWebSearchService!!.search(searchRequest)
                logger.info("🔍 Brave search returned ${braveResults.results.size} results for '$searchQuery'")
                
                // Log raw results for debugging
                braveResults.results.forEachIndexed { index, result ->
                    logger.debug("  Raw result [$index]: title='${result.title}', url='${result.url}', desc='${result.description?.take(100)}...'")
                }
                
                // Convert Brave results to URL candidates
                val urlCandidates = braveResults.results.mapIndexed { index, result ->
                    val isRelevant = isApiDocumentationUrl(result.url, result.title, result.description)
                    logger.debug("  Result [$index] relevance check: $isRelevant")
                    
                    if (isRelevant) {
                        val urlType = inferUrlType(result.url, result.title)
                        logger.debug("  Result [$index] inferred type: $urlType")
                        
                        UrlCandidate(
                            url = result.url,
                            type = urlType,
                            confidence = 0.7, // Brave search confidence
                            description = "Found via Brave search: ${result.title}"
                        )
                    } else {
                        null
                    }
                }.filterNotNull()
                
                allUrlCandidates.addAll(urlCandidates)
                logger.info("✅ Brave search processed ${urlCandidates.size}/${braveResults.results.size} relevant URLs")
                
            } catch (e: Exception) {
                logger.error("❌ Brave search failed for query '$searchQuery': ${e.message}", e)
            }
            
            // Remove duplicates based on URL
            val uniqueUrls = allUrlCandidates.distinctBy { it.url }
            logger.info("🎯 Brave search total: ${allUrlCandidates.size} candidates, ${uniqueUrls.size} unique URLs")
            uniqueUrls
            
        } catch (e: Exception) {
            logger.warn("⚠️ Brave search failed for $providerName: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Check if a URL looks like API documentation based on URL, title, and description.
     */
    private fun isApiDocumentationUrl(url: String, title: String?, description: String?): Boolean {
        val combinedText = "$url ${title ?: ""} ${description ?: ""}".lowercase()
        
        val apiKeywords = listOf("api", "docs", "documentation", "developer", "reference", "guide")
        val relevantKeywords = listOf("rest", "openapi", "swagger", "sdk", "integration")
        
        return apiKeywords.any { it in combinedText } || relevantKeywords.any { it in combinedText }
    }
    
    /**
     * Infer URL type from URL path and title.
     */
    private fun inferUrlType(url: String, title: String?): UrlType {
        val urlLower = url.lowercase()
        val titleLower = title?.lowercase() ?: ""
        
        return when {
            "swagger" in urlLower || "swagger" in titleLower -> UrlType.SCHEMA
            "openapi" in urlLower || "openapi" in titleLower -> UrlType.SCHEMA
            "/docs" in urlLower || "documentation" in titleLower -> UrlType.DOCUMENTATION
            "/api" in urlLower || "api reference" in titleLower -> UrlType.API_REFERENCE
            "reference" in titleLower -> UrlType.API_REFERENCE
            else -> UrlType.DOCUMENTATION
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