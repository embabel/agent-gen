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

import com.embabel.agent.search.BraveWebSearchService
import com.embabel.agent.search.WebSearchRequest
import com.embabel.agent.api.common.OperationContext
import com.embabel.common.ai.model.LlmOptions
import com.embabel.common.ai.model.ModelSelectionCriteria.Companion.Auto
import com.embabel.metaagent.core.model.AgentSpecification
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Stage 1A: Provider Discovery Service
 * 
 * TEMPORARY IMPLEMENTATION: Uses LLM for provider discovery
 * TODO: Replace with Brave/Google Search API integration for real-time accuracy
 * 
 * Discovers API providers for a given domain without making technical claims
 * about URLs or endpoints (which leads to hallucination).
 */
@Component
class ProviderDiscoveryService(
    @Autowired(required = false) private val braveWebSearchService: BraveWebSearchService?
) {

    private val logger = LoggerFactory.getLogger(ProviderDiscoveryService::class.java)

    /**
     * Discover API providers for domain using LLM knowledge.
     * 
     * @param specification Agent specification containing domain and requirements
     * @param context Operation context for LLM integration
     * @return List of provider candidates without technical details
     */
    fun discoverProviders(specification: AgentSpecification, context: OperationContext): List<ProviderCandidate> {
        logger.info("🔍 Stage 1A: Discovering providers for domain: ${specification.domain}")
        
        return try {
            logger.info("🤖 Sending LLM prompt for provider discovery")
            val discoveryResult = context.promptRunner().withLlm(LlmOptions(criteria = Auto)).createObject(
                buildProviderDiscoveryPrompt(specification),
                LlmProviderDiscovery::class.java
            )
            logger.info("✅ LLM discovered ${discoveryResult.providers.size} providers")
            
            discoveryResult.providers.map { llmProvider ->
                ProviderCandidate(
                    name = llmProvider.name,
                    accessModel = parseAccessModel(llmProvider.accessModel),
                    capabilities = llmProvider.capabilities,
                    targetMarket = llmProvider.targetMarket,
                    integrationComplexity = parseComplexity(llmProvider.integrationComplexity),
                    apiStatus = parseApiStatus(llmProvider.apiStatus)
                )
            }
            
        } catch (e: Exception) {
            logger.warn("⚠️ Provider discovery failed: ${e.message}")
            emptyList()
        }
    }

    /**
     * Build LLM prompt for provider discovery.
     * Reuses proven fragments from existing prompt while avoiding URL requests.
     */
    private fun buildProviderDiscoveryPrompt(specification: AgentSpecification): String {
        return """
        You are an expert in API integration and software architecture.
        
        Task: Discover the most relevant API providers for building an agent in the "${specification.domain}" domain.
        
        Agent Context:
        - Name: ${specification.name}
        - Domain: ${specification.domain}
        - Purpose: ${specification.specification}
        - Intended Actions: ${specification.actionIntents.joinToString(", ")}
        - Target Goals: ${specification.goalIntents.joinToString(", ")}
        - Examples: ${specification.examples.joinToString(", ")}
        
        Please identify 5-8 most relevant API providers considering:
        1. Domain Relevance: Providers specifically useful for ${specification.domain}
        2. Action Support: APIs that enable the intended actions: ${specification.actionIntents.joinToString(", ")}
        3. AUTOMATION-FIRST AWARENESS: Recognize automation-friendly patterns (self-service, instant access, developer portals) vs manual patterns (contact sales, partnership required)
        4. Integration Quality: Well-documented APIs with good developer experience
        5. Reliability: Established providers with good uptime and support
        6. Access Feasibility: How easily developers can get API access
        
        IMPORTANT: Do NOT provide specific URLs, endpoints, or technical details.
        Only provide business-level information about providers.
        
        For each provider, identify:
        - name: Provider/service name (e.g., "OpenTable", "Yelp")
        - accessModel: How API access is obtained. Classify based on actual onboarding process:
          * public_signup (self-service signup, instant API key, developer portal)
          * approval_required (developer application, review process, automated approval)
          * affiliate_program (business partnership, affiliate signup required)
          * partner_only (enterprise sales contact, manual partnership required)
        - capabilities: What functionality it provides (list of capabilities)
        - targetMarket: What type of businesses/use cases it serves
        - integrationComplexity: Development difficulty (LOW, MEDIUM, HIGH)
        - apiStatus: Current status (ACTIVE, DEPRECATED, BETA, UNKNOWN)
        
        Focus on real, established providers that genuinely serve this domain.
        Return only the JSON response with the providers array.
        """.trimIndent()
    }
    
    private fun parseAccessModel(accessModelString: String): AccessModel {
        return when (accessModelString.lowercase()) {
            "affiliate_program" -> AccessModel.AFFILIATE_PROGRAM
            "public_signup" -> AccessModel.PUBLIC_SIGNUP
            "partner_only" -> AccessModel.PARTNER_ONLY
            "approval_required" -> AccessModel.APPROVAL_REQUIRED
            else -> AccessModel.UNKNOWN
        }
    }
    
    private fun parseComplexity(complexityString: String): IntegrationComplexity {
        return when (complexityString.uppercase()) {
            "LOW" -> IntegrationComplexity.LOW
            "MEDIUM" -> IntegrationComplexity.MEDIUM
            "HIGH" -> IntegrationComplexity.HIGH
            else -> IntegrationComplexity.MEDIUM
        }
    }
    
    private fun parseApiStatus(statusString: String): ApiStatus {
        return when (statusString.uppercase()) {
            "ACTIVE" -> ApiStatus.ACTIVE
            "DEPRECATED" -> ApiStatus.DEPRECATED
            "BETA" -> ApiStatus.BETA
            else -> ApiStatus.UNKNOWN
        }
    }
}

/**
 * LLM response structure for provider discovery.
 */
@JsonClassDescription("Provider discovery results from LLM analysis")
data class LlmProviderDiscovery(
    @JsonPropertyDescription("List of discovered API providers")
    val providers: List<LlmProviderSuggestion> = emptyList()
)

@JsonClassDescription("Individual provider suggestion from LLM")
data class LlmProviderSuggestion(
    @JsonPropertyDescription("Provider/service name")
    val name: String,
    
    @JsonPropertyDescription("How API access is obtained")
    val accessModel: String,
    
    @JsonPropertyDescription("What functionality it provides")
    val capabilities: List<String>,
    
    @JsonPropertyDescription("What type of businesses it serves")
    val targetMarket: String,
    
    @JsonPropertyDescription("Development difficulty: LOW, MEDIUM, HIGH")
    val integrationComplexity: String,
    
    @JsonPropertyDescription("Current status: ACTIVE, DEPRECATED, BETA, UNKNOWN")
    val apiStatus: String
)

/**
 * Provider candidate discovered by Stage 1A.
 * Contains business information but no technical details (URLs, endpoints).
 */
data class ProviderCandidate(
    val name: String,
    val accessModel: AccessModel,
    val capabilities: List<String>,
    val targetMarket: String,
    val integrationComplexity: IntegrationComplexity,
    val apiStatus: ApiStatus
)

/**
 * How API access is obtained.
 */
enum class AccessModel {
    AFFILIATE_PROGRAM,
    PUBLIC_SIGNUP,
    PARTNER_ONLY,
    APPROVAL_REQUIRED,
    UNKNOWN
}

/**
 * Integration complexity levels.
 */
enum class IntegrationComplexity {
    LOW, MEDIUM, HIGH
}

/**
 * API status indicators.
 */
enum class ApiStatus {
    ACTIVE, DEPRECATED, BETA, UNKNOWN
}