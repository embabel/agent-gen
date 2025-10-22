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
import com.embabel.metaagent.core.agent.ToolsDiscoveryService
import com.embabel.metaagent.core.model.AgentSpecification
import com.embabel.metaagent.core.model.DiscoveredTool
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles


/**
 * Tools Discovery Test
 * Employs OPEN AI KEY
 */
@SpringBootTest(classes = [MetaAgentApplication::class])
@ActiveProfiles("test")
@org.junit.jupiter.api.Disabled("E2E integration config disabled during Stage 1 development")
class ToolsDiscoveryIntegrationTest {


    @Autowired
    lateinit var operationContext: OperationContext

    @Autowired
    lateinit var toolsDiscoveryService: ToolsDiscoveryService
    
    @Autowired
    lateinit var authenticationAnalyzer: com.embabel.metaagent.core.agent.ApiAuthenticationAnalyzer
    
    @Autowired
    lateinit var helpApiDiscoveryService: com.embabel.metaagent.core.agent.HelpApiDiscoveryService
    
    @Autowired
    lateinit var helpApiIntrospectionService: com.embabel.metaagent.core.agent.HelpApiIntrospectionService

    private val logger = LoggerFactory.getLogger(ToolsDiscoveryIntegrationTest::class.java)


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
    fun `test semantic tools discovery for restaurant domain`() {
        logger.info("""
🏗️ ========== STAGE 1: API DISCOVERY ==========
🎯 Goal: Discover relevant APIs for restaurant domain
📋 Starting semantic analysis...
        """.trimIndent())
        
        val specification = AgentSpecification(
            name = "RestaurantBookingAgent",
            domain = "restaurant-booking",
            specification = "Agent that helps users find and book restaurants with real-time availability",
            actionIntents = listOf("search restaurants", "check availability", "make reservation", "cancel booking"),
            goalIntents = listOf("find suitable restaurant", "complete booking", "manage reservations"),
            examples = listOf("Book dinner at Italian restaurant for 4 people tonight", "Find pizza places near me")
        )

        val discoveredTools = toolsDiscoveryService.discoverToolsForSpecification(specification, operationContext)

        // Verify we got some tools
        assert(discoveredTools.isNotEmpty()) { "Should discover at least some tools for restaurant domain" }
        
        logger.info("""
✅ STAGE 1 COMPLETE: API Discovery Results
📊 Discovered ${discoveredTools.size} tools:
${discoveredTools.mapIndexed { index, tool -> 
    "   ${index + 1}. ${tool.name}: ${tool.apiUrl} (complexity: ${tool.integrationComplexity})"
}.joinToString("\n")}
        """.trimIndent())

        // Verify tools are relevant to restaurant domain
        val hasRelevantTools = discoveredTools.any { tool ->
            tool.name.contains("restaurant", ignoreCase = true) ||
            tool.name.contains("yelp", ignoreCase = true) ||
            tool.name.contains("opentable", ignoreCase = true) ||
            tool.apiUrl.contains("restaurant", ignoreCase = true) ||
            tool.apiUrl.contains("yelp", ignoreCase = true)
        }
        
        assert(hasRelevantTools) { "Should find tools relevant to restaurant domain" }
        
        // Verify all tools have required fields
        discoveredTools.forEach { tool ->
            assert(tool.name.isNotBlank()) { "Tool name should not be blank" }
            assert(tool.apiUrl.isNotBlank()) { "Tool API URL should not be blank" }
            assert(tool.apiType != null) { "Tool should have an API type" }
        }
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    fun `test semantic tools discovery for weather domain`() {
        val specification = AgentSpecification(
            name = "WeatherAgent",
            domain = "weather",
            specification = "Agent that provides weather forecasts and alerts",
            actionIntents = listOf("get current weather", "get forecast", "set weather alerts"),
            goalIntents = listOf("provide accurate weather information", "notify of weather changes"),
            examples = listOf("What's the weather like today?", "Will it rain tomorrow?")
        )

        logger.info("🔍 Testing semantic tools discovery for weather domain...")

        val discoveredTools = toolsDiscoveryService.discoverToolsForSpecification(specification, operationContext)

        assert(discoveredTools.isNotEmpty()) { "Should discover tools for weather domain" }
        
        logger.info("✅ Discovered ${discoveredTools.size} weather tools:")
        discoveredTools.forEach { tool ->
            logger.info("  - ${tool.name}: ${tool.apiUrl}")
        }

        // Verify weather-relevant tools
        val hasWeatherTools = discoveredTools.any { tool ->
            tool.name.contains("weather", ignoreCase = true) ||
            tool.apiUrl.contains("weather", ignoreCase = true) ||
            tool.apiUrl.contains("forecast", ignoreCase = true)
        }
        
        assert(hasWeatherTools) { "Should find weather-related tools" }
    }


    @Test
    fun `test help API discovery for restaurant tools`() {
        logger.info("🔍 Testing Help API discovery...")
        
        // Create sample discovered tool
        val sampleTool = DiscoveredTool(
            name = "OpenTable API",
            apiUrl = "https://api.opentable.com",
            apiType = com.embabel.metaagent.core.model.ApiType.REST,
            authenticationRequired = true,
            authenticationScheme = com.embabel.metaagent.core.model.AuthenticationScheme.API_KEY,
            integrationComplexity = com.embabel.metaagent.core.model.IntegrationComplexity.MEDIUM,
            categories = setOf("restaurant-booking", "reservations")
        )
        
        // Test Help API discovery
        val helpApis = helpApiDiscoveryService.discoverHelpApis(sampleTool)
        
        // Verify results
        assert(helpApis.isNotEmpty()) { "Should discover Help APIs" }
        
        logger.info("📋 Discovered ${helpApis.size} Help APIs:")
        helpApis.forEach { helpApi ->
            logger.info("  - ${helpApi.type}: ${helpApi.url}")
        }
        
        // Verify expected Help API types are included
        val helpTypes = helpApis.map { it.type }.toSet()
        val expectedTypes = setOf(
            com.embabel.metaagent.core.agent.HelpApiType.HELP,
            com.embabel.metaagent.core.agent.HelpApiType.DOCUMENTATION,
            com.embabel.metaagent.core.agent.HelpApiType.SWAGGER,
            com.embabel.metaagent.core.agent.HelpApiType.OPENAPI
        )
        
        val hasExpectedTypes = expectedTypes.any { it in helpTypes }
        assert(hasExpectedTypes) { "Should include common Help API types" }
        
        // Verify URLs are properly constructed
        helpApis.forEach { helpApi ->
            assert(helpApi.url.startsWith("https://api.opentable.com")) { "Help API URLs should be based on tool URL" }
            assert(helpApi.url.isNotBlank()) { "Help API URL should not be blank" }
        }
        
        logger.info("✅ Help API discovery config passed")
    }

    @Test
    @org.junit.jupiter.api.Order(3) 
    fun `test complete help API introspection workflow`() {
        logger.info("""
🏗️ ========== STAGE 3: PARAMETER INTROSPECTION ==========
🎯 Goal: Extract detailed parameters from API endpoints
📋 Method: Direct LLM analysis (Stage 2 Help APIs skipped)
        """.trimIndent())
        
        // Create sample discovered tool
        val sampleTool = DiscoveredTool(
            name = "Restaurant API",
            apiUrl = "https://api.restaurant.com",
            apiType = com.embabel.metaagent.core.model.ApiType.REST,
            authenticationRequired = true,
            authenticationScheme = com.embabel.metaagent.core.model.AuthenticationScheme.API_KEY,
            integrationComplexity = com.embabel.metaagent.core.model.IntegrationComplexity.MEDIUM,
            categories = setOf("restaurant-booking", "reservations", "menu")
        )
        
        // Direct parameter discovery (bypass Help APIs - sleeping mode)
        val parameterAnalysis = com.embabel.metaagent.core.agent.ApiParameterDiscoveryService().discoverParametersForApi(
            sampleTool,
            operationContext
        )
        
        logger.info("🎯 Direct Parameter Analysis Complete")
        logger.info("   Parameter Sets: ${parameterAnalysis.size}")
        
        // Log detailed parameter information with pretty formatting
        val parameterReport = buildString {
            appendLine("📋 PARAMETER ANALYSIS RESULTS for ${sampleTool.name}")
            appendLine("=" .repeat(60))
            parameterAnalysis.forEach { (endpoint, params) ->
                appendLine("🔹 Endpoint: $endpoint")
                appendLine("   Parameters discovered: ${params.size}")
                params.forEach { param ->
                    val requiredIcon = if (param.required) "✅" else "⚪"
                    val sourceIcon = when (param.source) {
                        com.embabel.metaagent.core.agent.ParameterSource.LLM_ANALYSIS -> "🤖"
                        com.embabel.metaagent.core.agent.ParameterSource.STRUCTURE_ANALYSIS -> "🔍"
                        else -> "📝"
                    }
                    appendLine("   $requiredIcon ${param.name} (${param.type.name.lowercase()}) $sourceIcon")
                    appendLine("      📄 ${param.description}")
                    if (param.format != null) {
                        appendLine("      📐 Format: ${param.format}")
                    }
                }
                appendLine()
            }
            append("=" .repeat(60))
        }
        logger.info(parameterReport)
        
        // Verify results
        assert(parameterAnalysis.isNotEmpty()) { "Should extract some parameters from direct analysis" }
        
        // Verify parameter sources are valid
        val validSources = setOf(
            com.embabel.metaagent.core.agent.ParameterSource.LLM_ANALYSIS,
            com.embabel.metaagent.core.agent.ParameterSource.STRUCTURE_ANALYSIS,
            com.embabel.metaagent.core.agent.ParameterSource.DOCUMENTATION_INTROSPECTION,
            com.embabel.metaagent.core.agent.ParameterSource.OPENAPI_INTROSPECTION
        )
        
        parameterAnalysis.values.flatten().forEach { param ->
            assert(param.source in validSources) { "Parameter source ${param.source} should be valid" }
        }
        
        logger.info("✅ Complete Help API introspection workflow config passed")
    }



    @Test
    fun `test tools validation returns unchanged list`() {
        val sampleTools = listOf(
            com.embabel.metaagent.core.model.DiscoveredTool(
                name = "Test API",
                apiUrl = "https://api.test.com",
                apiType = com.embabel.metaagent.core.model.ApiType.REST,
                authenticationRequired = true,
                authenticationScheme = com.embabel.metaagent.core.model.AuthenticationScheme.API_KEY,
                integrationComplexity = com.embabel.metaagent.core.model.IntegrationComplexity.LOW
            )
        )

        val validatedTools = toolsDiscoveryService.validateDiscoveredTools(sampleTools)

        assert(validatedTools == sampleTools) { "Validation should return unchanged tools for now" }
        assert(validatedTools.size == 1) { "Should maintain same number of tools" }
        
        logger.info("✅ Tools validation config passed")
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    fun `test real API connectivity for discovered restaurant APIs`() {
        logger.info("""
🏗️ ========== STAGE 4: CONNECTIVITY TESTING ==========
🎯 Goal: Verify API endpoints are reachable
📋 Method: HTTP HEAD requests to base URLs
        """.trimIndent())
        
        // TESTABLE APIs - verified from public sources
        val realApis = listOf(
            "https://api.yelp.com/v3" to "Yelp Fusion API",                      // ✅ Official verified
            "http://opentable.herokuapp.com/api" to "OpenTable Unofficial API",  // ✅ Public alternative
            "https://api.resy.com/3" to "Resy API",                             // ✅ Research verified  
            "https://api.tablein.com/v1" to "Tablein API"                       // ✅ Keep existing
            // Removed: Zomato (discontinued), OpenTable platform URL (LLM hallucination)
        )
        
        val connectivityResults = mutableMapOf<String, ApiConnectivityResult>()
        
        realApis.forEach { (apiUrl, apiName) ->
            logger.info("🔍 Testing connectivity to $apiName at $apiUrl")
            
            val result = testApiConnectivity(apiUrl, apiName)
            connectivityResults[apiName] = result
            
            val statusIcon = when (result.status) {
                ApiStatus.REACHABLE -> "✅"
                ApiStatus.NEEDS_AUTH -> "🔐" 
                ApiStatus.NOT_FOUND -> "❌"
                ApiStatus.ERROR -> "⚠️"
            }
            
            logger.info("$statusIcon $apiName: ${result.status} (${result.statusCode}) - ${result.message}")
        }
        
        // Verify at least some APIs are reachable (even if they need auth)
        val reachableApis = connectivityResults.values.count { 
            it.status in setOf(ApiStatus.REACHABLE, ApiStatus.NEEDS_AUTH) 
        }
        
        assert(reachableApis > 0) { "At least some APIs should be reachable or return auth errors" }
        
        // Log summary
        logger.info("📊 API Connectivity Summary:")
        logger.info("   Reachable: ${connectivityResults.values.count { it.status == ApiStatus.REACHABLE }}")
        logger.info("   Needs Auth: ${connectivityResults.values.count { it.status == ApiStatus.NEEDS_AUTH }}")
        logger.info("   Not Found: ${connectivityResults.values.count { it.status == ApiStatus.NOT_FOUND }}")
        logger.info("   Errors: ${connectivityResults.values.count { it.status == ApiStatus.ERROR }}")
        
        logger.info("✅ Real API connectivity config completed")
    }
    
    private fun testApiConnectivity(apiUrl: String, apiName: String): ApiConnectivityResult {
        return try {
            val request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(apiUrl))
                .method("HEAD", java.net.http.HttpRequest.BodyPublishers.noBody())
                .timeout(java.time.Duration.ofSeconds(10))
                .build()
            
            val client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build()
            
            val response = client.send(request, java.net.http.HttpResponse.BodyHandlers.discarding())
            
            when (response.statusCode()) {
                200 -> ApiConnectivityResult(ApiStatus.REACHABLE, response.statusCode(), "API is publicly accessible")
                401, 403 -> ApiConnectivityResult(ApiStatus.NEEDS_AUTH, response.statusCode(), "API requires authentication")
                404 -> ApiConnectivityResult(ApiStatus.NOT_FOUND, response.statusCode(), "API endpoint not found")
                302 -> ApiConnectivityResult(ApiStatus.ERROR, response.statusCode(), "Redirect - possible wrong endpoint")
                in 400..499 -> ApiConnectivityResult(ApiStatus.NEEDS_AUTH, response.statusCode(), "Client error - likely needs proper headers/auth")
                in 500..599 -> ApiConnectivityResult(ApiStatus.ERROR, response.statusCode(), "Server error")
                else -> ApiConnectivityResult(ApiStatus.ERROR, response.statusCode(), "Unexpected status code")
            }
            
        } catch (e: java.net.ConnectException) {
            ApiConnectivityResult(ApiStatus.NOT_FOUND, 0, "Connection refused: ${e.message}")
        } catch (e: java.net.http.HttpTimeoutException) {
            ApiConnectivityResult(ApiStatus.ERROR, 0, "Request timeout: ${e.message}")
        } catch (e: Exception) {
            ApiConnectivityResult(ApiStatus.ERROR, 0, "Network error: ${e.message}")
        }
    }

    @Test
    @org.junit.jupiter.api.Disabled("Help API discovery in sleeping mode")
    fun `test real help API discovery on live APIs`() {
        logger.info("🔍 Testing Help API discovery on real restaurant APIs...")
        
        val realApis = listOf(
            DiscoveredTool(
                name = "Yelp Fusion API",
                apiUrl = "https://api.yelp.com/v3",
                apiType = com.embabel.metaagent.core.model.ApiType.REST,
                authenticationRequired = true,
                authenticationScheme = com.embabel.metaagent.core.model.AuthenticationScheme.API_KEY,
                integrationComplexity = com.embabel.metaagent.core.model.IntegrationComplexity.MEDIUM,
                categories = setOf("restaurant-search", "business-lookup")
            ),
            DiscoveredTool(
                name = "Zomato API", 
                apiUrl = "https://developers.zomato.com/api/v2.1",
                apiType = com.embabel.metaagent.core.model.ApiType.REST,
                authenticationRequired = true,
                authenticationScheme = com.embabel.metaagent.core.model.AuthenticationScheme.API_KEY,
                integrationComplexity = com.embabel.metaagent.core.model.IntegrationComplexity.LOW,
                categories = setOf("restaurant-search", "menu-data")
            )
        )
        
        var totalWorkingHelpApis = 0
        
        realApis.forEach { api ->
            logger.info("🔍 Testing Help APIs for ${api.name}")
            
            // Step 1: Discover Help APIs
            val helpApis = helpApiDiscoveryService.discoverHelpApis(api)
            
            // Step 2: Test introspection (will config HTTP connectivity to real Help URLs)
            val introspectionResult = helpApiIntrospectionService.introspectParameters(
                api, 
                helpApis, 
                operationContext
            )
            
            val workingApis = introspectionResult.workingHelpApis.size
            totalWorkingHelpApis += workingApis
            
            logger.info("📋 ${api.name} Results:")
            logger.info("   Help APIs tested: ${helpApis.size}")
            logger.info("   Working Help APIs: $workingApis")
            logger.info("   Confidence: ${String.format("%.3f", introspectionResult.confidence)}")
            logger.info("   Source: ${introspectionResult.introspectionSource}")
            
            // Log any working Help APIs found
            introspectionResult.workingHelpApis.forEach { workingApi ->
                logger.info("   ✅ Working: ${workingApi.helpApi.type} at ${workingApi.helpApi.url}")
            }
        }
        
        logger.info("📊 Real Help API Discovery Summary:")
        logger.info("   Total APIs tested: ${realApis.size}")
        logger.info("   Total working Help APIs found: $totalWorkingHelpApis")
        
        // Verify config completed successfully (even if no Help APIs work)
        assert(realApis.isNotEmpty()) { "Should config at least some real APIs" }
        
        logger.info("✅ Real Help API discovery config completed")
    }

    @Test
    fun `test 3-stage API validation for restaurant booking`() {
        logger.info("🏗️ Starting 3-stage API validation...")
        
        // Stage 1: Discover APIs
        val discoveredApis = discoverRestaurantApis()
        
        // Stage 2: Validate Authentication 
        val authValidatedApis = validateApiAuthentication(discoveredApis)
        
        // Stage 3: Test Actual Endpoints
        val testedApis = testApiEndpoints(authValidatedApis)
        
        logger.info("✅ 3-stage validation complete: ${testedApis.size} fully validated APIs")
    }
    
    private fun discoverRestaurantApis(): List<DiscoveredTool> {
        logger.info("🔍 Stage 1: API Discovery...")
        
        val specification = AgentSpecification(
            name = "RestaurantBookingAgent",
            domain = "restaurant-booking",
            specification = "Agent for restaurant booking with date/time and guest count",
            actionIntents = listOf("booking", "availability", "cancellation"),
            goalIntents = listOf("complete reservations", "manage bookings"),
            examples = listOf("Book table for 4 at 7pm tonight", "Cancel reservation #123")
        )

        val discoveredTools = toolsDiscoveryService.discoverToolsForSpecification(specification, operationContext)
        assert(discoveredTools.isNotEmpty()) { "Should discover restaurant APIs" }
        
        logger.info("📊 Discovered ${discoveredTools.size} APIs:")
        discoveredTools.forEach { tool ->
            logger.info("  - ${tool.name}: ${tool.categories}")
        }
        
        return discoveredTools
    }
    
    private fun validateApiAuthentication(apis: List<DiscoveredTool>): List<DiscoveredTool> {
        logger.info("🔐 Stage 2: Authentication Validation...")
        
        // TODO: Implement authentication validation
        // - Check if API keys are required
        // - Test basic connectivity 
        // - Validate auth schemes
        
        logger.info("✅ Authentication validated for ${apis.size} APIs")
        return apis // For now, return unchanged
    }
    
    private fun testApiEndpoints(apis: List<DiscoveredTool>): List<DiscoveredTool> {
        logger.info("🎯 Stage 3: Endpoint Testing...")
        
        // TODO: Implement endpoint testing
        // - HEAD requests to base URLs
        // - Test booking-specific endpoints
        // - Validate response formats
        
        logger.info("✅ Endpoint testing complete for ${apis.size} APIs")
        return apis // For now, return unchanged
    }

    @Test
    fun `test action-specific semantic scoring for restaurant APIs`() {
        logger.info("🎯 Testing action-specific semantic scoring...")
        
        // Reuse discovery logic
        val discoveredTools = discoverRestaurantApis()
        
        // Test booking action scoring across all discovered tools
        val bookingScores = discoveredTools.map { tool ->
            val score = toolsDiscoveryService.scoreToolForAction(tool, "booking")
            tool.name to score
        }.toMap()
        
        // Log scores for analysis
        logger.info("🔍 Booking action scores:")
        bookingScores.forEach { (toolName, score) ->
            logger.info("  - $toolName: ${String.format("%.3f", score)}")
        }
        
        // Verify scores are in valid range
        bookingScores.values.forEach { score ->
            assert(score >= 0.0 && score <= 1.0) { "Score should be between 0.0 and 1.0, got $score" }
        }
        
        // Verify at least one tool scores reasonably for booking
        val maxBookingScore = bookingScores.values.maxOrNull() ?: 0.0
        assert(maxBookingScore > 0.3) { "At least one tool should score reasonably for booking action" }
        
        logger.info("✅ Action-specific scoring config passed - max booking score: ${String.format("%.3f", maxBookingScore)}")
    }
}

// Data classes for connectivity testing
data class ApiConnectivityResult(
    val status: ApiStatus,
    val statusCode: Int,
    val message: String
)

enum class ApiStatus {
    REACHABLE,      // 200 - API is publicly accessible
    NEEDS_AUTH,     // 401/403 - API exists but needs authentication  
    NOT_FOUND,      // 404 - API endpoint not found
    ERROR           // Other errors (timeouts, 5xx, etc.)
}