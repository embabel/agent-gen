/*
 * Copyright 2024-2025 Embabel Software, Inc.
 */
package com.embabel.metaagent.service

import com.embabel.agent.restaurantplanner.RestaurantPlannerAgent
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Simple test for ToolsMatchingService - baby steps approach.
 */
@SpringBootTest(classes = [MetaAgentApplication::class])
@ActiveProfiles("test")
class SimpleToolsMatchingTest {

    @Autowired
    lateinit var toolsMatchingService: ToolsMatchingService

    private val logger = LoggerFactory.getLogger(SimpleToolsMatchingTest::class.java)

    @Test
    fun `basic tools matching - just log results`() {
        logger.info("🧪 Basic Tools Matching Test")
        
        // Simple OpenTable API mock
        val openTableAPI = ApiDocumentation(
            title = "OpenTable API",
            url = "https://docs.opentable.com/",
            endpoints = listOf(
                ApiEndpoint("GET", "/restaurants", 
                    listOf(ApiParameter("location", "string", true)), 
                    "Search restaurants"),
                ApiEndpoint("POST", "/reservations", 
                    listOf(ApiParameter("restaurant_id", "string", true)), 
                    "Make reservation")
            )
        )
        
        val result = toolsMatchingService.matchToolsWithAgent(
            agentClass = RestaurantPlannerAgent::class,
            extractedAPIs = listOf(openTableAPI)
        )
        
        logger.info("""
📊 ToolsMatchingService Results:
   Agent: ${result.agentClass}
   Provider: ${result.selectedProvider} (score: ${String.format("%.2f", result.providerScore)})
   Methods: ${result.matchedMethods}/${result.totalMethods} matched
   
🔧 Generated TOOLING Class (NOT Agent):
${"-".repeat(80)}
${result.enhancedSourceCode}
${"-".repeat(80)}
        """.trimIndent())
    }
}