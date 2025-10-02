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
package com.embabel.metaagent.core.agent

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
    val tools: List<LLMToolSuggestion>
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
            // Use LLM for semantic tool discovery
            val llmSuggestions = context.promptRunner().withLlm(LlmOptions(criteria = Auto)).createObject(
                buildToolDiscoveryPrompt(specification),
                LLMToolSuggestions::class.java
            )
            
            logger.info("🤖 LLM suggested ${llmSuggestions.tools.size} tools")
            
            // Convert LLM suggestions to DiscoveredTool objects
            val discoveredTools = llmSuggestions.tools.mapNotNull { suggestion ->
                // Only include tools that have API URLs
                if (suggestion.apiUrl != null) {
                    DiscoveredTool(
                        name = suggestion.name,
                        apiUrl = suggestion.apiUrl,
                        apiType = ApiType.REST, // Default to REST for now
                        authenticationRequired = true, // Default to requiring auth
                        authenticationScheme = AuthenticationScheme.API_KEY, // Default auth scheme
                        integrationComplexity = when (suggestion.complexity.uppercase()) {
                            "LOW" -> IntegrationComplexity.LOW
                            "MEDIUM" -> IntegrationComplexity.MEDIUM
                            "HIGH" -> IntegrationComplexity.HIGH
                            else -> IntegrationComplexity.MEDIUM
                        }
                    )
                } else {
                    logger.debug("Skipping tool ${suggestion.name} - no API URL provided")
                    null
                }
            }
            
            // Log discovered tools
            discoveredTools.forEach { tool ->
                logger.info("✅ Found: ${tool.name} (${tool.integrationComplexity}) - ${tool.apiUrl}")
            }
            
            discoveredTools
            
        } catch (e: Exception) {
            logger.warn("⚠️ LLM tool discovery failed, returning empty list: ${e.message}")
            emptyList()
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
        - **reasoning**: Why it's perfect for this specific agent
        - **confidence**: Your confidence in this suggestion (0.0-1.0)
        
        Focus on practical, production-ready tools that would genuinely help this agent achieve its goals.
        Return only the JSON response with the tools array.
        """.trimIndent()
    }
}