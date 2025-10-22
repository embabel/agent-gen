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
import com.embabel.common.ai.model.LlmOptions
import com.embabel.common.ai.model.ModelSelectionCriteria.Companion.Auto
import com.embabel.metaagent.core.model.AgentSpecification
import com.embabel.metaagent.core.model.DiscoveredTool
import com.embabel.metaagent.core.model.ApiType
import com.embabel.metaagent.core.model.AuthenticationScheme
import com.embabel.metaagent.core.model.IntegrationComplexity
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * LLM response structure for tool discovery.
 * 
 * Defines the expected JSON structure from LLM when discovering tools
 * for a specific domain and agent specification.
 */
@JsonClassDescription("Tool suggestions from LLM semantic analysis")
data class LLMToolSuggestions(
    @JsonPropertyDescription("List of relevant tools/APIs for the domain")
    val tools: List<LLMToolSuggestion> = emptyList()
)

@JsonClassDescription("Individual tool suggestion from LLM")
data class LLMToolSuggestion(
    @JsonPropertyDescription("Tool/API name (e.g., 'OpenTable API')")
    val name: String,
    
    @JsonPropertyDescription("Primary use case description")
    val description: String,
    
    @JsonPropertyDescription("API base URL if known")
    val apiUrl: String?,
    
    @JsonPropertyDescription("Integration complexity: LOW, MEDIUM, HIGH")
    val complexity: String,
    
    @JsonPropertyDescription("Functional categories that best describe this tool's capabilities")
    val categories: List<String>,
    
    @JsonPropertyDescription("Official API name from documentation (if different from name)")
    val officialName: String?,
    
    @JsonPropertyDescription("Documentation URL if available")
    val documentationUrl: String?,
    
    @JsonPropertyDescription("API status: ACTIVE, DEPRECATED, BETA, UNKNOWN")
    val apiStatus: String?,
    
    @JsonPropertyDescription("Payment required: true if API requires payment/subscription, false if free")
    val paymentRequired: Boolean?,
    
    @JsonPropertyDescription("Free tier available: true if free usage tier exists")
    val freeTierAvailable: Boolean?,
    
    @JsonPropertyDescription("Token generation method: INSTANT_API_KEY, EMAIL_VERIFICATION, OAUTH_AUTOMATED, MANUAL_ONLY")
    val tokenGenerationMethod: String?,
    
    @JsonPropertyDescription("Key API endpoints for this tool (e.g., ['/restaurants/{id}/reservations', '/menu/{restaurantId}'])")
    val keyEndpoints: List<String>?,
    
    @JsonPropertyDescription("Why this tool is relevant for the agent")
    val reasoning: String,
    
    @JsonPropertyDescription("Confidence score 0.0-1.0")
    val confidence: Double
)

/**
 * Tools Discovery Service - Semantic Tool Discovery for Agent Integration
 * 
 * PURPOSE: Analyze agent specifications and suggest relevant external tools/APIs
 * for integration based on domain, actions, and requirements using LLM semantic analysis.
 * 
 * **Discovery Strategy:**
 * 1. LLM semantic analysis of specification
 * 2. Domain-specific tool suggestions  
 * 3. User-guided refinement and selection
 * 
 * This service encapsulates all tools discovery logic and provides clean dependency injection
 * for Spring Boot integration and testing.
 */
@Component
class ToolsDiscoveryService {

    private val logger = LoggerFactory.getLogger(ToolsDiscoveryService::class.java)

    /**
     * Discover tools for agent integration using LLM semantic analysis.
     * 
     * @param specification The agent specification containing domain and requirements
     * @param context Operation context for LLM integration
     * @return List of discovered tools suitable for integration
     */
    fun discoverToolsForSpecification(specification: AgentSpecification, context: OperationContext): List<DiscoveredTool> {
        logger.info("🔍 Starting semantic tools discovery for domain: ${specification.domain}")
        logger.debug("Action Intents: ${specification.actionIntents}")
        logger.debug("Goal Intents: ${specification.goalIntents}")
        
        return try {
            // Use LLM for semantic tool discovery with enhanced error handling
            val llmSuggestions = try {
                context.promptRunner().withLlm(LlmOptions(criteria = Auto)).createObject(
                    buildToolDiscoveryPrompt(specification),
                    LLMToolSuggestions::class.java
                )
            } catch (e: Exception) {
                logger.warn("LLM tool discovery failed, using fallback: ${e.message}")
                LLMToolSuggestions(tools = emptyList())
            }
            
            logger.info("🤖 LLM suggested ${llmSuggestions.tools.size} tools")
            
            // Convert LLM suggestions to DiscoveredTool objects
            val discoveredTools = llmSuggestions.tools.mapNotNull { suggestion ->
                // Only include tools that have API URLs
                if (suggestion.apiUrl != null) {
                    DiscoveredTool(
                        name = suggestion.officialName ?: suggestion.name, // Use official name if available
                        apiUrl = suggestion.apiUrl,
                        apiType = ApiType.REST, // Default to REST for now
                        authenticationRequired = true, // Default to requiring auth
                        authenticationScheme = AuthenticationScheme.API_KEY, // Default auth scheme
                        integrationComplexity = when (suggestion.complexity.uppercase()) {
                            "LOW" -> IntegrationComplexity.LOW
                            "MEDIUM" -> IntegrationComplexity.MEDIUM
                            "HIGH" -> IntegrationComplexity.HIGH
                            else -> IntegrationComplexity.MEDIUM
                        },
                        categories = suggestion.categories.toSet()
                    )
                } else {
                    logger.debug("Skipping tool ${suggestion.name} - no API URL provided")
                    null
                }
            }
            
            // Log discovered tools with comprehensive details (single formatted log per tool)
            discoveredTools.forEachIndexed { index, tool ->
                val suggestion = llmSuggestions.tools.find { it.name == tool.name || it.officialName == tool.name }
                val logMessage = buildString {
                    appendLine("✅ API #${index + 1}: ${tool.name}")
                    appendLine("   URL: ${tool.apiUrl}")
                    appendLine("   Categories: ${tool.categories.joinToString(", ")}")
                    appendLine("   Complexity: ${tool.integrationComplexity}")
                    suggestion?.let { s ->
                        appendLine("   Payment Required: ${s.paymentRequired ?: "unknown"}")
                        appendLine("   Free Tier: ${s.freeTierAvailable ?: "unknown"}")
                        appendLine("   Auth Method: ${s.tokenGenerationMethod ?: "unknown"}")
                        appendLine("   Status: ${s.apiStatus ?: "unknown"}")
                        s.keyEndpoints?.let { endpoints ->
                            if (endpoints.isNotEmpty()) {
                                appendLine("   Key Endpoints:")
                                endpoints.forEach { endpoint ->
                                    appendLine("     - $endpoint")
                                }
                            }
                        }
                    }
                    append("   Official Name: ${suggestion?.officialName ?: "same as name"}")
                }
                logger.info(logMessage)
            }
            
            discoveredTools
            
        } catch (e: Exception) {
            logger.warn("⚠️ LLM tool discovery failed, returning empty list: ${e.message}")
            emptyList()
        }
    }

    /**
     * Score how well a tool supports a specific action intent.
     * 
     * Uses semantic matching between action intent and tool's dynamic categories,
     * name, and description. Fully generic - works with any LLM-suggested categories.
     * 
     * @param tool The discovered tool with LLM-suggested categories
     * @param actionIntent The specific action (e.g., "booking", "menus", "locations")
     * @return Score 0.0-1.0 where 1.0 = perfect match
     */
    fun scoreToolForAction(tool: DiscoveredTool, actionIntent: String): Double {
        // TODO: Externalize these weights to configuration
        val categoryWeight = 0.6   // Primary signal - tool categories
        val nameWeight = 0.3       // Secondary signal - tool name  
        val descriptionWeight = 0.1 // Tertiary signal - tool description
        
        // 1. Category matching - find best category match
        val categoryScore = if (tool.categories.isNotEmpty()) {
            tool.categories.maxOf { category ->
                calculateSemanticSimilarity(actionIntent, category)
            }
        } else {
            0.0
        }
        
        // 2. Tool name matching
        val nameScore = calculateSemanticSimilarity(actionIntent, tool.name)
        
        // 3. Description matching (using tool name as description for now)
        val descriptionScore = calculateSemanticSimilarity(actionIntent, tool.name)
        
        // Weighted combination
        val finalScore = (categoryWeight * categoryScore) + 
                        (nameWeight * nameScore) + 
                        (descriptionWeight * descriptionScore)
        
        logger.info("🎯 Action '$actionIntent' vs Tool '${tool.name}': category=$categoryScore, name=$nameScore, final=$finalScore")
        
        return finalScore.coerceIn(0.0, 1.0)
    }

    /**
     * Calculate semantic similarity between two strings.
     * 
     * Currently uses simple string-based similarity. 
     * TODO: Enhance with LLM-based semantic comparison for better accuracy.
     * 
     * @param text1 First text to compare
     * @param text2 Second text to compare  
     * @return Similarity score 0.0-1.0
     */
    private fun calculateSemanticSimilarity(text1: String, text2: String): Double {
        val t1 = text1.lowercase().trim()
        val t2 = text2.lowercase().trim()
        
        // Exact match
        if (t1 == t2) return 1.0
        
        // Contains match
        if (t1.contains(t2) || t2.contains(t1)) return 0.8
        
        // Word overlap
        val words1 = t1.split(Regex("[\\s\\-_]+")).toSet()
        val words2 = t2.split(Regex("[\\s\\-_]+")).toSet()
        val intersection = words1.intersect(words2)
        val union = words1.union(words2)
        
        return if (union.isNotEmpty()) {
            intersection.size.toDouble() / union.size.toDouble()
        } else {
            0.0
        }
    }

    /**
     * Validate and enrich discovered tool suggestions.
     * 
     * PURPOSE: Verify tool availability and enrich with metadata.
     * 
     * @param tools Raw tool suggestions to validate
     * @return Validated and enriched tools
     */
    fun validateDiscoveredTools(tools: List<DiscoveredTool>): List<DiscoveredTool> {
        logger.info("🔍 Validating ${tools.size} discovered tools")
        
        // TODO: Implement tool validation
        // For now, return unchanged (maintains compatibility)
        return tools
    }

    /**
     * Build LLM prompt for semantic tool discovery.
     * 
     * Creates a structured prompt that guides the LLM to suggest relevant tools
     * based on the agent specification and domain requirements.
     */
    private fun buildToolDiscoveryPrompt(specification: AgentSpecification): String {
        return """
        You are an expert in API integration and software architecture.
        
        Task: Discover the most relevant APIs and tools for building an agent in the "${specification.domain}" domain.
        
        Agent Context:
        - Name: ${specification.name}
        - Domain: ${specification.domain}
        - Purpose: ${specification.specification}
        - Intended Actions: ${specification.actionIntents.joinToString(", ")}
        - Target Goals: ${specification.goalIntents.joinToString(", ")}
        - Examples: ${specification.examples.joinToString(", ")}
        
        **IMPORTANT: API Documentation Analysis**
        For each API you suggest, consider:
        - Review known API documentation (Swagger/OpenAPI specs, developer docs)
        - Analyze actual endpoint capabilities and data models
        - Verify current API status and availability
        - Identify proper API names and official endpoints
        - **CRITICAL: Payment and Authentication Analysis**
          * Does the API require payment or subscription?
          * Is there a free tier available?
          * How are API keys/tokens generated? (instant, email verification, manual signup)
          * Prioritize APIs with free tiers and instant key generation
        - Consider both public documentation and real-world usage patterns
        
        Please suggest 5-8 most relevant tools/APIs considering:
        
        1. **Domain Relevance**: Tools specifically useful for ${specification.domain}
        2. **Action Support**: APIs that enable the intended actions: ${specification.actionIntents.joinToString(", ")}
        3. **Integration Quality**: Well-documented APIs with good developer experience
        4. **Reliability**: Established tools with good uptime and support
        5. **Practical Usage**: Real-world APIs that are commonly used in production
        
        For each tool, provide:
        - **name**: Clear tool/API name
        - **description**: What it does and primary use case
        - **apiUrl**: Base API URL if known (or null if unknown)
        - **complexity**: Integration difficulty (LOW/MEDIUM/HIGH)
        - **categories**: Please provide the most best match API categories for this tool based on its functionality
        - **officialName**: Official API name from documentation (if different from name)
        - **documentationUrl**: Link to API documentation
        - **apiStatus**: Current status (ACTIVE, DEPRECATED, BETA, UNKNOWN)
        - **paymentRequired**: true if API requires payment/subscription
        - **freeTierAvailable**: true if free usage tier exists
        - **tokenGenerationMethod**: How API keys are obtained (INSTANT_API_KEY, EMAIL_VERIFICATION, OAUTH_AUTOMATED, MANUAL_ONLY)
        - **keyEndpoints**: List of specific API endpoints with complete CRUD operations. Include CREATE (POST), READ (GET), UPDATE (PUT/PATCH), and DELETE operations for each resource type to cover the full lifecycle (e.g., ["POST /restaurants/{id}/reservations", "GET /restaurants/{id}/reservations", "DELETE /reservations/{id}"])
        - **reasoning**: Why it's perfect for this specific agent
        - **confidence**: Your confidence in this suggestion (0.0-1.0)
        
        Focus on practical, production-ready tools that would genuinely help this agent achieve its goals.
        Return only the JSON response with the tools array.
        """.trimIndent()
    }
}