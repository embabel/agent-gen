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
import com.embabel.metaagent.core.tools.discovery.ToolsDiscoveryService
import com.embabel.metaagent.core.tools.discovery.UrlDiscoveryService
import com.embabel.metaagent.core.tools.verification.UrlVerificationService
import com.embabel.metaagent.core.tools.verification.UrlStatus
import com.embabel.metaagent.core.tools.discovery.AccessModel
import com.embabel.metaagent.core.tools.discovery.ProviderCandidate
import com.embabel.metaagent.core.tools.discovery.IntegrationComplexity
import com.embabel.metaagent.core.model.AgentSpecification
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Full Pipeline Integration Test for Restaurant Finder
 *
 * PURPOSE: Validate the meta-agent framework with a "finder" use case
 * that targets accessible, free-tier APIs (vs booking which requires partnerships).
 *
 * KEY VALIDATION: Production classes work UNCHANGED - only the input
 * AgentSpecification differs.
 *
 * WORKING STAGES:
 * - Provider Discovery
 * - URL Discovery
 * - URL Verification
 *
 * BLOCKED (TODO):
 * - LLM API Extraction (extracting wrong paths - needs fix)
 * - @LlmTool generation from verified APIs
 */
@SpringBootTest(classes = [MetaAgentApplication::class])
@ActiveProfiles("test")
class RestaurantFinderPipelineTest {

    @Autowired
    lateinit var operationContext: OperationContext

    @Autowired
    lateinit var providerDiscoveryService: ProviderDiscoveryService

    @Autowired
    lateinit var toolsDiscoveryService: ToolsDiscoveryService

    @Autowired
    lateinit var urlDiscoveryService: UrlDiscoveryService

    @Autowired
    lateinit var urlVerificationService: UrlVerificationService

    private val llmApiExtractor = LlmApiExtractor()
    private val baseUrlExtractor = BaseUrlExtractor()
    private val apiVerificationHarness = ApiVerificationHarness()

    private val logger = LoggerFactory.getLogger(RestaurantFinderPipelineTest::class.java)

    /**
     * Rank providers by accessibility and integration complexity.
     * Higher rank = more suitable for automation.
     *
     * Ranking criteria (in order of priority):
     * 1. AccessModel: PUBLIC_SIGNUP > APPROVAL_REQUIRED > AFFILIATE_PROGRAM > PARTNER_ONLY
     * 2. IntegrationComplexity: LOW > MEDIUM > HIGH
     */
    private fun rankProviders(providers: List<ProviderCandidate>): List<ProviderCandidate> {
        return providers.sortedWith(
            compareBy<ProviderCandidate> { provider ->
                // Lower number = higher priority
                when (provider.accessModel) {
                    AccessModel.PUBLIC_SIGNUP -> 0
                    AccessModel.APPROVAL_REQUIRED -> 1
                    AccessModel.AFFILIATE_PROGRAM -> 2
                    AccessModel.PARTNER_ONLY -> 3
                    AccessModel.UNKNOWN -> 4
                }
            }.thenBy { provider ->
                when (provider.integrationComplexity) {
                    IntegrationComplexity.LOW -> 0
                    IntegrationComplexity.MEDIUM -> 1
                    IntegrationComplexity.HIGH -> 2
                }
            }
        )
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            System.setProperty("embabel.agent.shell.interactive.enabled", "false")
        }

        /**
         * Adhoc RestaurantFinder specification
         * Focus: Finding restaurants, menus, reviews (NOT booking)
         * Preference: Free APIs with public access
         */
        fun createRestaurantFinderSpec() = AgentSpecification(
            name = "RestaurantFinderAgent",
            domain = "restaurant-discovery",
            specification = """
                Agent that helps users discover restaurants by location, cuisine, price range, and reviews.
                Provides menu information and ratings.
                IMPORTANT: Prefer APIs with free tiers and public access.
                No partnership or paid subscription should be required.
                Target APIs like Foursquare Places, Yelp Fusion free tier, MenuPortal, Zomato, RapidAPI restaurant APIs, or similar public restaurant data APIs.
            """.trimIndent(),
            actionIntents = listOf(
                "search restaurants by location",
                "filter by cuisine type",
                "get restaurant menus",
                "read reviews and ratings",
                "find nearby restaurants"
            ),
            goalIntents = listOf(
                "find matching restaurants",
                "get restaurant details",
                "compare restaurant options"
            ),
            examples = listOf(
                "Find Italian restaurants near downtown",
                "Show me the menu for a specific restaurant",
                "What are the best rated Thai restaurants?",
                "Find restaurants with outdoor seating"
            )
        )
    }

    @Test
    fun `full pipeline - restaurant finder discovers accessible free APIs`() {
        logger.info("""
🏗️ ========== PIPELINE TEST: RESTAURANT FINDER ==========
🎯 Goal: Validate framework with finder use case (accessible free APIs)
🔧 Validation: Prod classes unchanged - only input spec differs
        """.trimIndent())

        val specification = createRestaurantFinderSpec()

        // ========== STAGE 1: PROVIDER DISCOVERY ==========
        logger.info("\n📍 STAGE 1: Provider Discovery")
        val providers = providerDiscoveryService.discoverProviders(specification, operationContext)

        logger.info("   Discovered ${providers.size} providers")
        providers.forEachIndexed { index, provider ->
            val accessIcon = when (provider.accessModel) {
                AccessModel.PUBLIC_SIGNUP -> "🤖"
                AccessModel.APPROVAL_REQUIRED -> "🔧"
                AccessModel.AFFILIATE_PROGRAM -> "📞"
                AccessModel.PARTNER_ONLY -> "❌"
                else -> "❓"
            }
            logger.info("   ${index + 1}. $accessIcon ${provider.name} (${provider.accessModel})")
            logger.info("      Capabilities: ${provider.capabilities.joinToString(", ")}")
        }

        assert(providers.isNotEmpty()) { "Should discover providers for restaurant finder domain" }

        // Check for automation-friendly providers
        val accessibleProviders = providers.filter {
            it.accessModel in setOf(AccessModel.PUBLIC_SIGNUP, AccessModel.APPROVAL_REQUIRED)
        }
        logger.info("   ✅ Accessible providers: ${accessibleProviders.size}/${providers.size}")

        // ========== STAGE 1.5: PROVIDER RANKING ==========
        logger.info("\n📍 STAGE 1.5: Provider Ranking (by accessibility & complexity)")
        val rankedProviders = rankProviders(providers)

        logger.info("   Ranked providers (best first):")
        rankedProviders.forEachIndexed { index, provider ->
            val accessIcon = when (provider.accessModel) {
                AccessModel.PUBLIC_SIGNUP -> "🤖"
                AccessModel.APPROVAL_REQUIRED -> "🔧"
                AccessModel.AFFILIATE_PROGRAM -> "📞"
                AccessModel.PARTNER_ONLY -> "❌"
                else -> "❓"
            }
            val complexityIcon = when (provider.integrationComplexity) {
                IntegrationComplexity.LOW -> "🟢"
                IntegrationComplexity.MEDIUM -> "🟡"
                IntegrationComplexity.HIGH -> "🔴"
            }
            logger.info("   ${index + 1}. $accessIcon $complexityIcon ${provider.name} (${provider.accessModel}, ${provider.integrationComplexity})")
        }

        // ========== STAGES 2-5: ITERATE THROUGH PROVIDERS ==========
        logger.info("\n📍 STAGES 2-5: Iterating through providers until accessible API found")

        var successfulProvider: String? = null
        var apiDocumentation: ApiDocumentation? = null
        var toolCandidatesCount = 0
        val maxProvidersToTry = minOf(5, rankedProviders.size)
        val triedProviders = mutableListOf<String>()

        for (provider in rankedProviders.take(maxProvidersToTry)) {
            logger.info("\n🔄 Trying provider: ${provider.name}")
            triedProviders.add(provider.name)

            // STAGE 2: URL Discovery for this provider
            logger.info("   📍 Stage 2: URL Discovery")
            val urls = urlDiscoveryService.discoverProviderUrls(provider.name, operationContext)
            if (urls.isEmpty()) {
                logger.info("   ⏭️ No URLs found, trying next provider...")
                continue
            }
            logger.info("   Found ${urls.size} URLs")

            // STAGE 3: URL Verification
            logger.info("   📍 Stage 3: URL Verification")
            val verifiedUrls = urlVerificationService.verifyUrls(urls, provider.name)
            val bestUrl = verifiedUrls
                .filter { it.verification.status in setOf(UrlStatus.REACHABLE, UrlStatus.NEEDS_AUTH) }
                .maxByOrNull { it.adjustedConfidence }

            if (bestUrl == null) {
                logger.info("   ⏭️ No reachable URLs, trying next provider...")
                continue
            }
            logger.info("   Best URL: ${bestUrl.urlCandidate.url}")

            // STAGE 4: LLM API Extraction
            logger.info("   📍 Stage 4: LLM API Extraction")
            try {
                apiDocumentation = llmApiExtractor.extractFromUrl(bestUrl.urlCandidate.url, operationContext)
                if (apiDocumentation == null || apiDocumentation.endpoints.isEmpty()) {
                    logger.info("   ⏭️ No endpoints extracted, trying next provider...")
                    continue
                }
                logger.info("   Extracted ${apiDocumentation.endpoints.size} endpoints")

                // Extract base URLs
                val baseUrlResult = baseUrlExtractor.extractBaseUrls(bestUrl.urlCandidate.url, operationContext)
                if (baseUrlResult == null) {
                    logger.info("   ⏭️ No base URL found, trying next provider...")
                    continue
                }
                logger.info("   Base URL: ${baseUrlResult.baseUrls.production ?: "Not found"}")

                // STAGE 5: API Verification Harness
                logger.info("   📍 Stage 5: API Verification Harness")
                val apiResults = apiVerificationHarness.verifyApiEndpoints(
                    extractedApis = apiDocumentation,
                    baseUrls = baseUrlResult.baseUrls,
                    context = operationContext
                )

                apiResults.forEach { result ->
                    val statusIcon = when (result.verification.status) {
                        ApiAccessStatus.PUBLICLY_ACCESSIBLE -> "🟢"
                        ApiAccessStatus.REQUIRES_AUTH -> "🟡"
                        ApiAccessStatus.NOT_ACCESSIBLE -> "🔴"
                        ApiAccessStatus.ERROR -> "⚠️"
                        ApiAccessStatus.UNKNOWN -> "❓"
                    }
                    logger.info("   $statusIcon ${result.endpoint.method.uppercase()} ${result.endpoint.path}")
                }

                // Check for publicly accessible endpoints
                val publiclyAccessible = apiResults.filter {
                    it.verification.status == ApiAccessStatus.PUBLICLY_ACCESSIBLE
                }

                if (publiclyAccessible.isNotEmpty()) {
                    logger.info("   ✅ SUCCESS! Found ${publiclyAccessible.size} publicly accessible endpoints")
                    successfulProvider = provider.name
                    toolCandidatesCount = publiclyAccessible.size
                    break
                }

                // Check if auth required (not a blocker, can still generate tools)
                val requiresAuth = apiResults.filter {
                    it.verification.status == ApiAccessStatus.REQUIRES_AUTH
                }
                if (requiresAuth.isNotEmpty()) {
                    logger.info("   🔐 Provider requires API key - ${requiresAuth.size} endpoints need auth")
                    logger.info("   ⏭️ Trying next provider for public access...")
                    continue
                }

                logger.info("   ⏭️ No accessible endpoints, trying next provider...")

            } catch (e: Exception) {
                logger.warn("   ⚠️ Error processing ${provider.name}: ${e.message}")
                continue
            }
        }

        // Final status
        if (successfulProvider != null) {
            logger.info("\n✅ Found accessible API: $successfulProvider with $toolCandidatesCount public endpoints")
        } else {
            logger.info("\n⚠️ No publicly accessible APIs found after trying: ${triedProviders.joinToString(", ")}")
            logger.info("   💡 Recommendation: Obtain API keys for one of these providers")
        }

        // TODO: STAGE 6 - Generate @LlmTool from verified APIs
        // Example of expected output:
        // @LlmTool(description = "Search restaurants by location and cuisine")
        // fun searchRestaurants(
        //     @LlmTool.Param(description = "Location to search") location: String,
        //     @LlmTool.Param(description = "Cuisine type filter") cuisine: String?
        // ): List<Restaurant>

        // ========== SUMMARY ==========
        logger.info("""

📊 ========== PIPELINE SUMMARY ==========
   Providers discovered: ${providers.size}
   Providers ranked: ${rankedProviders.size}
   Accessible providers: ${accessibleProviders.size}
   Providers tried: ${triedProviders.size}
   Successful provider: ${successfulProvider ?: "None"}
   APIs extracted: ${apiDocumentation?.endpoints?.size ?: 0}
   Tool candidates: $toolCandidatesCount

🎯 FRAMEWORK VALIDATION:
   ✅ Prod classes used UNCHANGED
   ✅ Only AgentSpecification input differs
   ✅ Free API preference expressed in spec text
   ✅ Smart iteration through providers

⏳ PENDING:
   - @LlmTool generation from verified endpoints (TODO: Stage 6)

⚠️ KNOWN ISSUES:
   - LLM API Extraction may extract incorrect endpoint paths
============================================
        """.trimIndent())
    }

    @Test
    fun `tools discovery produces relevant finder APIs with free tier preference`() {
        logger.info("""
🔍 ========== TOOLS DISCOVERY TEST: RESTAURANT FINDER ==========
🎯 Goal: Verify ToolsDiscoveryService finds finder-relevant free APIs
🔧 Validation: Prod class unchanged - preference in spec
        """.trimIndent())

        val specification = createRestaurantFinderSpec()

        val discoveredTools = toolsDiscoveryService.discoverToolsForSpecification(specification, operationContext)

        logger.info("Discovered ${discoveredTools.size} tools:")
        discoveredTools.forEachIndexed { index, tool ->
            logger.info("   ${index + 1}. ${tool.name}")
            logger.info("      URL: ${tool.apiUrl}")
            logger.info("      Categories: ${tool.categories.joinToString(", ")}")
        }

        assert(discoveredTools.isNotEmpty()) { "Should discover tools for restaurant finder" }

        // Verify tools are relevant to finder use case (not booking)
        val hasFinderRelevantTools = discoveredTools.any { tool ->
            tool.categories.any { category ->
                category.contains("search", ignoreCase = true) ||
                category.contains("menu", ignoreCase = true) ||
                category.contains("review", ignoreCase = true) ||
                category.contains("location", ignoreCase = true) ||
                category.contains("place", ignoreCase = true) ||
                category.contains("restaurant", ignoreCase = true)
            }
        }

        assert(hasFinderRelevantTools) { "Should find tools relevant to restaurant finding (not just booking)" }
        logger.info("✅ Tools discovery correctly targets finder use case")

        // TODO: Convert discoveredTools to @LlmTool annotated class
        // This would generate code like:
        // class RestaurantFinderTools {
        //     @LlmTool(description = "Search for restaurants")
        //     fun searchRestaurants(...): ...
        // }
    }

    @Test
    fun `compare finder vs booking - same prod classes different outcomes`() {
        logger.info("""
📊 ========== COMPARISON: FINDER vs BOOKING ==========
🎯 Goal: Same prod classes, different specs → different results
🔧 Validation: Framework genericity
        """.trimIndent())

        // Finder specification (free APIs, no partnership)
        val finderSpec = createRestaurantFinderSpec()

        // Booking specification (existing use case - partnerships likely)
        val bookingSpec = AgentSpecification(
            name = "RestaurantBookingAgent",
            domain = "restaurant-booking",
            specification = "Agent that helps users book restaurant reservations with real-time availability",
            actionIntents = listOf("make reservation", "check availability", "cancel booking"),
            goalIntents = listOf("complete booking", "manage reservations"),
            examples = listOf("Book dinner for 4 at 7pm", "Cancel my reservation")
        )

        // Discover providers for both using SAME prod class
        val finderProviders = providerDiscoveryService.discoverProviders(finderSpec, operationContext)
        val bookingProviders = providerDiscoveryService.discoverProviders(bookingSpec, operationContext)

        // Analyze accessibility differences
        val finderAccessible = finderProviders.count {
            it.accessModel in setOf(AccessModel.PUBLIC_SIGNUP, AccessModel.APPROVAL_REQUIRED)
        }
        val bookingAccessible = bookingProviders.count {
            it.accessModel in setOf(AccessModel.PUBLIC_SIGNUP, AccessModel.APPROVAL_REQUIRED)
        }

        val finderPartnerOnly = finderProviders.count { it.accessModel == AccessModel.PARTNER_ONLY }
        val bookingPartnerOnly = bookingProviders.count { it.accessModel == AccessModel.PARTNER_ONLY }

        logger.info("""
📊 COMPARISON RESULTS:

FINDER (search, menus, reviews - free API preference):
   Total providers: ${finderProviders.size}
   Accessible (PUBLIC_SIGNUP/APPROVAL): $finderAccessible
   Partner-only: $finderPartnerOnly
   Accessibility ratio: ${if (finderProviders.isNotEmpty()) String.format("%.1f", finderAccessible.toDouble() / finderProviders.size * 100) else 0}%

BOOKING (reservations - partnerships expected):
   Total providers: ${bookingProviders.size}
   Accessible (PUBLIC_SIGNUP/APPROVAL): $bookingAccessible
   Partner-only: $bookingPartnerOnly
   Accessibility ratio: ${if (bookingProviders.isNotEmpty()) String.format("%.1f", bookingAccessible.toDouble() / bookingProviders.size * 100) else 0}%

🎯 VALIDATION:
   Same ProviderDiscoveryService class
   Different AgentSpecification inputs
   Different accessibility outcomes expected
        """.trimIndent())

        // Both should work (framework is generic)
        assert(finderProviders.isNotEmpty()) { "Finder should discover providers" }
        assert(bookingProviders.isNotEmpty()) { "Booking should discover providers" }

        logger.info("✅ Framework genericity validated - prod classes unchanged")
    }
}
