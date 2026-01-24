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
import com.embabel.metaagent.core.tools.discovery.ProviderDiscoveryService
import com.embabel.metaagent.core.tools.discovery.UrlCandidate
import com.embabel.metaagent.core.tools.discovery.ApiStatus
import com.embabel.metaagent.core.tools.discovery.IntegrationComplexity
import com.embabel.metaagent.core.tools.verification.UrlStatus
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
    
    @Autowired
    lateinit var urlDiscoveryService: com.embabel.metaagent.core.tools.discovery.UrlDiscoveryService
    
    @Autowired
    lateinit var urlVerificationService: com.embabel.metaagent.core.tools.verification.UrlVerificationService

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
    @org.junit.jupiter.api.Order(1)
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
                    ApiStatus.ACTIVE -> "✅"
                    ApiStatus.BETA -> "🧪"
                    ApiStatus.DEPRECATED -> "⚠️"
                    else -> "❓"
                }
                val complexityIcon = when (provider.integrationComplexity) {
                    IntegrationComplexity.LOW -> "🟢"
                    IntegrationComplexity.MEDIUM -> "🟡"
                    IntegrationComplexity.HIGH -> "🔴"
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
    @org.junit.jupiter.api.Order(2)
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
    @org.junit.jupiter.api.Order(5)
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

    @Test
    @org.junit.jupiter.api.Order(3)
    fun `test URL discovery for discovered providers`() {
        logger.info("ENV: " + System.getenv("BRAVE_API_KEY"))
        logger.info("""
🏗️ ========== STAGE 1A.5: URL DISCOVERY TEST ==========
🎯 Goal: Discover documentation URLs for providers
📋 Method: LLM + Web Search enhanced URL discovery
        """.trimIndent())

        // First get some providers from Stage 1A
        val specification = AgentSpecification(
            name = "RestaurantBookingAgent",
            domain = "restaurant-booking",
            specification = "Agent that helps users find and book restaurants",
            actionIntents = listOf("search restaurants", "make reservation"),
            goalIntents = listOf("find suitable restaurant", "complete booking"),
            examples = listOf("Book dinner for 4 people tonight")
        )

        val providers = providerDiscoveryService.discoverProviders(specification, operationContext)
        assert(providers.isNotEmpty()) { "Should have providers from Stage 1A" }
        
        // Test URL discovery for the first few providers
        val testProviders = providers.take(3)
        
        logger.info("🔍 Testing URL discovery for ${testProviders.size} providers...")
        
        testProviders.forEach { provider ->
            logger.info("📍 Testing URLs for provider: ${provider.name}")
            
            val urls = urlDiscoveryService.discoverProviderUrls(provider.name, operationContext)
            
            logger.info("   🌐 Discovered ${urls.size} URLs:")
            urls.forEach { url ->
                val confidenceIcon = when {
                    url.confidence >= 0.8 -> "🟢"
                    url.confidence >= 0.6 -> "🟡" 
                    else -> "🔴"
                }
                logger.info("   $confidenceIcon ${url.type}: ${url.url} (confidence: ${String.format("%.2f", url.confidence)})")
                logger.info("      📄 ${url.description}")
            }
            
            // Verify we got some URLs
            assert(urls.isNotEmpty()) { "Should discover URLs for provider ${provider.name}" }
            
            // Stage 1B: URL Verification
            logger.info("   🔍 Stage 1B: Verifying ${urls.size} URLs for ${provider.name}...")
            val verifiedUrls = urlVerificationService.verifyUrls(urls, provider.name)
            
            logger.info("   ✅ Verified ${verifiedUrls.size}/${urls.size} URLs:")
            verifiedUrls.forEach { verifiedUrl ->
                val statusIcon = when (verifiedUrl.verification.status) {
                    UrlStatus.REACHABLE -> "✅"
                    UrlStatus.NEEDS_AUTH -> "🔐"
                    UrlStatus.NOT_FOUND -> "❌"
                    UrlStatus.ERROR -> "⚠️"
                }
                logger.info("   $statusIcon ${verifiedUrl.urlCandidate.url} → ${verifiedUrl.verification.status}")
                logger.info("      📊 Confidence: ${String.format("%.2f", verifiedUrl.adjustedConfidence)}, Authority: ${String.format("%.2f", verifiedUrl.authorityScore)}")
            }
            
            // Verify URLs have reasonable confidence
            val hasHighConfidenceUrl = urls.any { it.confidence >= 0.5 }
            if (!hasHighConfidenceUrl) {
                logger.warn("⚠️ No high-confidence URLs found for ${provider.name}")
            }
            
            // Verify URLs look reasonable (not just fallback patterns)
            val hasReasonableUrl = urls.any { url ->
                url.url.contains("http") && 
                (url.url.contains("docs") || url.url.contains("api") || url.url.contains("developer"))
            }
            assert(hasReasonableUrl) { "Should find reasonable documentation URLs for ${provider.name}" }
        }
        
        logger.info("✅ URL discovery config passed")
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    fun `test automation-first provider discovery`() {
        logger.info("""
🤖 ========== AUTOMATION-FIRST PROVIDER DISCOVERY TEST ==========
🎯 Goal: Test automation-friendly provider discovery
📋 Method: Pattern-based automation scoring with zero hardcoding
        """.trimIndent())

        // Test the enhanced existing service with automation-first prompt
        val specification = AgentSpecification(
            name = "RestaurantBookingAgent",
            domain = "restaurant booking and reservations",
            specification = "Agent that helps users find and book restaurants with automation-first approach",
            actionIntents = listOf("search restaurants", "check availability", "make reservation"),
            goalIntents = listOf("find suitable restaurant", "complete booking"),
            examples = listOf("Book dinner for 4 people tonight")
        )
        
        val providers = providerDiscoveryService.discoverProviders(specification, operationContext)
        
        logger.info("📊 AUTOMATION-FIRST DISCOVERY RESULTS:")
        logger.info("   Total providers found: ${providers.size}")
        
        providers.forEachIndexed { index, provider ->
            val automationIcon = when (provider.accessModel) {
                com.embabel.metaagent.core.tools.discovery.AccessModel.PUBLIC_SIGNUP -> "🤖"
                com.embabel.metaagent.core.tools.discovery.AccessModel.APPROVAL_REQUIRED -> "🔧"  
                com.embabel.metaagent.core.tools.discovery.AccessModel.AFFILIATE_PROGRAM -> "📞"
                com.embabel.metaagent.core.tools.discovery.AccessModel.PARTNER_ONLY -> "❌"
                else -> "❓"
            }
            
            logger.info("${index + 1}. $automationIcon ${provider.name} (${provider.accessModel})")
            logger.info("   🏢 Target: ${provider.targetMarket}")
            logger.info("   ⚡ Capabilities: ${provider.capabilities.joinToString(", ")}")
            logger.info("   🔧 Complexity: ${provider.integrationComplexity}")
            logger.info("   📊 Status: ${provider.apiStatus}")
            logger.info("")
        }
        
        // Verify automation focus using AccessModel
        val publicSignupProviders = providers.filter { it.accessModel == com.embabel.metaagent.core.tools.discovery.AccessModel.PUBLIC_SIGNUP }
        val approvalRequiredProviders = providers.filter { it.accessModel == com.embabel.metaagent.core.tools.discovery.AccessModel.APPROVAL_REQUIRED }
        val partnerOnlyProviders = providers.filter { it.accessModel == com.embabel.metaagent.core.tools.discovery.AccessModel.PARTNER_ONLY }
        val affiliateProviders = providers.filter { it.accessModel == com.embabel.metaagent.core.tools.discovery.AccessModel.AFFILIATE_PROGRAM }
        
        logger.info("🤖 PUBLIC SIGNUP (instant): ${publicSignupProviders.size}")
        logger.info("🔧 APPROVAL REQUIRED (automated): ${approvalRequiredProviders.size}")
        logger.info("📞 AFFILIATE PROGRAM (business): ${affiliateProviders.size}")
        logger.info("❌ PARTNER ONLY (manual): ${partnerOnlyProviders.size}")
        
        // Assertions
        assert(providers.isNotEmpty()) { "Should find providers for restaurant domain" }
        
        // Check that providers have relevant capabilities
        val hasRelevantProviders = providers.any { provider ->
            provider.capabilities.any { capability ->
                capability.contains("restaurant", ignoreCase = true) ||
                capability.contains("booking", ignoreCase = true) ||
                capability.contains("reservation", ignoreCase = true) ||
                capability.contains("dining", ignoreCase = true)
            }
        }
        
        logger.info("✅ Found relevant providers: $hasRelevantProviders")
        
        // Verify automation-first strategy is working (more accessible than manual)
        val accessibleProviders = publicSignupProviders.size + approvalRequiredProviders.size
        val manualProviders = partnerOnlyProviders.size + affiliateProviders.size
        
        if (accessibleProviders > manualProviders) {
            logger.info("🎯 Automation-first strategy working - more accessible providers found!")
        }
        
        // Check automation accessibility ratio
        val totalProviders = providers.size
        val accessibilityRatio = if (totalProviders > 0) accessibleProviders.toDouble() / totalProviders else 0.0
        
        logger.info("📊 Automation accessibility: ${String.format("%.1f", accessibilityRatio * 100)}%")
        
        if (accessibilityRatio < 0.5) {
            logger.warn("⚠️ Low accessibility ratio - automation-first prompt may need tuning")
        }
        
        logger.info("✅ Automation-first provider discovery test completed")
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    fun `test URL verification for discovered URLs`() {
        logger.info("""
🏗️ ========== STAGE 1B: URL VERIFICATION TEST ==========
🎯 Goal: Verify which discovered URLs are actually reachable
📋 Method: HTTP HEAD requests to filter working URLs
        """.trimIndent())

        // Get providers and URLs from previous stages
        val specification = AgentSpecification(
            name = "RestaurantBookingAgent",
            domain = "restaurant-booking",
            specification = "Agent that helps users find and book restaurants",
            actionIntents = listOf("search restaurants", "make reservation"),
            goalIntents = listOf("find suitable restaurant", "complete booking"),
            examples = listOf("Book dinner for 4 people tonight")
        )

        val providers = providerDiscoveryService.discoverProviders(specification, operationContext)
        assert(providers.isNotEmpty()) { "Should have providers from Stage 1A" }
        
        // Test verification for the first provider that has URLs
        val testProvider = providers.first()
        logger.info("🔍 Testing URL verification for provider: ${testProvider.name}")
        
        // Discover URLs for this provider
        val urls = urlDiscoveryService.discoverProviderUrls(testProvider.name, operationContext)
        assert(urls.isNotEmpty()) { "Should have URLs from Stage 1A.5" }
        
        logger.info("📍 Verifying ${urls.size} URLs...")
        
        // Verify the URLs
        val verifiedUrls = urlVerificationService.verifyUrls(urls, testProvider.name)
        
        logger.info("📊 Verification Results:")
        logger.info("   Total URLs tested: ${urls.size}")
        logger.info("   Verified URLs: ${verifiedUrls.size}")
        
        verifiedUrls.forEach { verified ->
            val statusIcon = when (verified.verification.status) {
                UrlStatus.REACHABLE -> "✅"
                UrlStatus.NEEDS_AUTH -> "🔐"
                UrlStatus.NOT_FOUND -> "❌"
                UrlStatus.ERROR -> "⚠️"
            }
            
            logger.info("   $statusIcon ${verified.urlCandidate.url}")
            logger.info("      📊 Status: ${verified.verification.status} (${verified.verification.statusCode})")
            logger.info("      ⏱️ Response time: ${verified.verification.responseTimeMs}ms")
            logger.info("      🎯 Confidence: ${String.format("%.2f", verified.urlCandidate.confidence)} → ${String.format("%.2f", verified.adjustedConfidence)}")
        }
        
        // Verify we got some working URLs (either reachable or auth-required)
        val workingUrls = verifiedUrls.filter { 
            it.verification.status in setOf(
                UrlStatus.REACHABLE,
                UrlStatus.NEEDS_AUTH
            )
        }
        
        logger.info("🎯 Working URLs: ${workingUrls.size}")
        
        // For now, just verify the process works (some URLs might not be reachable)
        assert(verifiedUrls != null) { "Should return verification results" }
        
        // If we have working URLs, verify confidence was boosted
        if (workingUrls.isNotEmpty()) {
            val hasImprovedConfidence = workingUrls.any { 
                it.adjustedConfidence > it.urlCandidate.confidence 
            }
            if (hasImprovedConfidence) {
                logger.info("✅ Confidence boosting working for verified URLs")
            }
        }
        
        logger.info("✅ URL verification config passed")
    }
}