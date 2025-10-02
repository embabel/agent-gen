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
class ToolsDiscoveryIntegrationTest {


    @Autowired
    lateinit var operationContext: OperationContext

    @Autowired
    lateinit var toolsDiscoveryService: ToolsDiscoveryService

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
    fun `test semantic tools discovery for restaurant domain`() {
        val specification = AgentSpecification(
            name = "RestaurantBookingAgent",
            domain = "restaurant-booking",
            specification = "Agent that helps users find and book restaurants with real-time availability",
            actionIntents = listOf("search restaurants", "check availability", "make reservation", "cancel booking"),
            goalIntents = listOf("find suitable restaurant", "complete booking", "manage reservations"),
            examples = listOf("Book dinner at Italian restaurant for 4 people tonight", "Find pizza places near me")
        )

        logger.info("🔍 Testing semantic tools discovery for restaurant domain...")

        val discoveredTools = toolsDiscoveryService.discoverToolsForSpecification(specification, operationContext)

        // Verify we got some tools
        assert(discoveredTools.isNotEmpty()) { "Should discover at least some tools for restaurant domain" }
        
        logger.info("✅ Discovered ${discoveredTools.size} tools:")
        discoveredTools.forEach { tool ->
            logger.info("  - ${tool.name}: ${tool.apiUrl} (complexity: ${tool.integrationComplexity})")
        }

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
        
        logger.info("✅ Tools validation test passed")
    }
}