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
package com.embabel.metaagent.service

import com.embabel.agent.api.common.OperationContext
import com.embabel.metaagent.core.agent.ProviderDiscoveryService
import com.embabel.metaagent.core.model.AgentSpecification
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Tests for Provider Discovery Service (Stage 1A)
 */
@SpringBootTest(classes = [MetaAgentApplication::class])
@ActiveProfiles("test")
class ProviderDiscoveryServiceTest {

    @Autowired
    lateinit var operationContext: OperationContext

    @Autowired
    lateinit var providerDiscoveryService: ProviderDiscoveryService

    private val logger = LoggerFactory.getLogger(ProviderDiscoveryServiceTest::class.java)

    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            // Set shell configuration to non-interactive mode
            System.setProperty("embabel.agent.shell.interactive.enabled", "false")
        }
    }

    @Test
    fun `test discover providers for restaurant domain`() {
        logger.info("""
🏗️ ========== STAGE 1A: PROVIDER DISCOVERY TEST ==========
🎯 Goal: Discover API providers for restaurant domain
📋 Method: LLM-based provider identification (temporary)
        """.trimIndent())

        val specification = AgentSpecification(
            name = "RestaurantBookingAgent",
            domain = "restaurant-booking",
            specification = "Agent that helps users find and book restaurants with real-time availability",
            actionIntents = listOf("search restaurants", "check availability", "make reservation", "cancel booking"),
            goalIntents = listOf("find suitable restaurant", "complete booking", "manage reservations"),
            examples = listOf("Book dinner at Italian restaurant for 4 people tonight", "Find pizza places near me")
        )

        val providers = providerDiscoveryService.discoverProviders(specification, operationContext)

        // Verify we got some providers
        assert(providers.isNotEmpty()) { "Should discover at least some providers for restaurant domain" }

        // Log provider discovery results with pretty formatting
        val providerReport = buildString {
            appendLine("📋 PROVIDER DISCOVERY RESULTS")
            appendLine("=" .repeat(60))
            appendLine("Domain: ${specification.domain}")
            appendLine("Providers discovered: ${providers.size}")
            appendLine()
            providers.forEachIndexed { index, provider ->
                val statusIcon = when (provider.apiStatus) {
                    com.embabel.metaagent.core.agent.ApiStatus.ACTIVE -> "✅"
                    com.embabel.metaagent.core.agent.ApiStatus.BETA -> "🧪"
                    com.embabel.metaagent.core.agent.ApiStatus.DEPRECATED -> "⚠️"
                    else -> "❓"
                }
                val complexityIcon = when (provider.integrationComplexity) {
                    com.embabel.metaagent.core.agent.IntegrationComplexity.LOW -> "🟢"
                    com.embabel.metaagent.core.agent.IntegrationComplexity.MEDIUM -> "🟡"
                    com.embabel.metaagent.core.agent.IntegrationComplexity.HIGH -> "🔴"
                }
                appendLine("${index + 1}. ${provider.name} $statusIcon $complexityIcon")
                appendLine("   📄 Access: ${provider.accessModel}")
                appendLine("   🎯 Target: ${provider.targetMarket}")
                appendLine("   🔧 Capabilities: ${provider.capabilities.joinToString(", ")}")
                appendLine()
            }
            append("=" .repeat(60))
        }
        logger.info(providerReport)

        // Verify providers are relevant to domain (safe validation)
        val hasRelevantProviders = providers.any { provider ->
            provider.capabilities.isNotEmpty() && (
                // Check if capabilities match domain keywords
                provider.capabilities.any { capability ->
                    specification.domain.split("-").any { domainWord ->
                        domainWord.isNotBlank() && capability.contains(domainWord, ignoreCase = true)
                    }
                } ||
                // Check if capabilities match action keywords  
                provider.capabilities.any { capability ->
                    specification.actionIntents.any { action ->
                        action.split(" ").any { actionWord ->
                            actionWord.isNotBlank() && capability.contains(actionWord, ignoreCase = true)
                        }
                    }
                }
            )
        }

        assert(hasRelevantProviders) { "Should find providers relevant to restaurant domain" }

        // Verify all providers have required fields
        providers.forEach { provider ->
            assert(provider.name.isNotBlank()) { "Provider name should not be blank" }
            assert(provider.capabilities.isNotEmpty()) { "Provider should have capabilities" }
            assert(provider.targetMarket.isNotBlank()) { "Provider should have target market" }
        }

        logger.info("✅ Provider discovery config passed - found ${providers.size} relevant providers")
    }

    @Test
    fun `test discover providers for weather domain`() {
        logger.info("""
🏗️ ========== STAGE 1A: WEATHER DOMAIN TEST ==========
🎯 Goal: Test domain-agnostic provider discovery
📋 Method: Different domain to verify generic approach
        """.trimIndent())

        val specification = AgentSpecification(
            name = "WeatherAgent",
            domain = "weather",
            specification = "Agent that provides weather forecasts and alerts",
            actionIntents = listOf("get current weather", "get forecast", "set weather alerts"),
            goalIntents = listOf("provide accurate weather information", "notify of weather changes"),
            examples = listOf("What's the weather like today?", "Will it rain tomorrow?")
        )

        val providers = providerDiscoveryService.discoverProviders(specification, operationContext)

        assert(providers.isNotEmpty()) { "Should discover providers for weather domain" }

        logger.info("📊 Weather domain providers: ${providers.size}")
        providers.forEach { provider ->
            logger.info("  - ${provider.name}: ${provider.capabilities.joinToString(", ")}")
        }

        // Verify weather-relevant providers (safe validation)
        val hasWeatherProviders = providers.any { provider ->
            provider.capabilities.isNotEmpty() && 
            provider.capabilities.any { capability ->
                capability.contains("weather", ignoreCase = true) || 
                capability.contains("forecast", ignoreCase = true) ||
                capability.contains("climate", ignoreCase = true)
            }
        }

        assert(hasWeatherProviders) { "Should find weather-related providers" }
        logger.info("✅ Weather domain config passed")
    }

    @Test
    fun `test provider discovery with empty action intents`() {
        logger.info("🔍 Testing provider discovery with minimal specification")

        val specification = AgentSpecification(
            name = "MinimalAgent",
            domain = "config",
            specification = "Minimal config agent",
            actionIntents = emptyList(),
            goalIntents = emptyList(),
            examples = emptyList()
        )

        val providers = providerDiscoveryService.discoverProviders(specification, operationContext)

        // Should handle edge case gracefully
        logger.info("📊 Minimal spec providers: ${providers.size}")

        // Verify no crash and some form of response
        assert(providers != null) { "Should return non-null response even for minimal spec" }
        logger.info("✅ Minimal specification config passed")
    }
}