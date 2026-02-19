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

import com.embabel.agent.api.tool.callback.ToolLoopLoggingInspector
import com.embabel.agent.api.tool.callback.ToolResultTruncatingTransformer
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Integration tests for ToolLoop callbacks (inspectors and transformers).
 * Extends RestaurantFinderPipelineExtensionTest to reuse:
 * - searchFoursquare() for restaurant discovery
 * - findMenuInfo() for menu URL lookup via BraveSearch
 * - MenuTools inner class (uses extractMenuJson)
 *
 *  HOWTO run:
 *  mvn test -Dtest="ToolLoopCallbacksIntegrationTest\$InspectorTest"
 *
 */
@SpringBootTest(classes = [MetaAgentApplication::class])
@ActiveProfiles("test")
class ToolLoopCallbacksIntegrationTest : RestaurantFinderPipelineExtensionTest() {

    private val logger = LoggerFactory.getLogger(ToolLoopCallbacksIntegrationTest::class.java)

    /**
     * Gets menu URLs using parent's searchFoursquare + findMenuInfo.
     */
    private fun getMenuUrls(): List<Pair<String, String>> {
        val foursquareResponse = searchFoursquare(
            query = "italian restaurant",
            near = "Upper East Side, New York, NY"
        )

        if (foursquareResponse.results.isEmpty()) {
            logger.warn("No restaurants found from Foursquare")
            return emptyList()
        }

        return foursquareResponse.results.take(3).mapNotNull { restaurant ->
            findMenuInfo(restaurant)?.let { menuInfo ->
                logger.info("Found menu URL for ${restaurant.name}: ${menuInfo.url}")
                restaurant.name to menuInfo.url
            }
        }
    }

    @Nested
    inner class InspectorTest {

        @Test
        fun `ToolLoopLoggingInspector receives callbacks during sequential tool loop`() {
            if (foursquareApiKey.isBlank()) {
                logger.warn("⚠️ FOURSQUARE_API_KEY not set - skipping test")
                return
            }
            if (braveWebSearchService == null) {
                logger.warn("⚠️ BRAVE_API_KEY not set - skipping test")
                return
            }

            logger.info("🔍 ========== INSPECTOR TEST: SEQUENTIAL TOOL LOOP ==========")

            val menuUrls = getMenuUrls()
            if (menuUrls.isEmpty()) {
                logger.warn("No menu URLs found - skipping test")
                return
            }

            val loggingInspector = ToolLoopLoggingInspector(logLevel = ToolLoopLoggingInspector.LogLevel.INFO)
            val truncatingTransformer = ToolResultTruncatingTransformer(
                maxLength = 5000,
                logLevel = ToolLoopLoggingInspector.LogLevel.INFO
            )
            val menuTools = MenuTools()
            val urlList = menuUrls.joinToString("\n") { "- ${it.first}: ${it.second}" }

            logger.info("Invoking LLM with ${menuUrls.size} menu URLs, inspector and truncating transformer (5K chars)...")

            val startTime = System.currentTimeMillis()

            val result = ai.withDefaultLlm()
                .withToolObject(menuTools)
                .withToolLoopInspectors(loggingInspector)
                .withToolLoopTransformers(truncatingTransformer)
                .creating(MenuComparisonResult::class.java)
                .fromPrompt(
                    """
                    You have access to a tool called fetchMenuJson that fetches menu JSON-LD from a URL.

                    Fetch menus from these URLs by calling fetchMenuJson for each one:
                    $urlList

                    After getting all menu data, provide a brief comparison.
                    """.trimIndent()
                )

            val elapsed = System.currentTimeMillis() - startTime
            logger.info("""
📤 OUTPUT: LLM Menu Comparison (${elapsed}ms)
   Menus analyzed: ${result.menusAnalyzed}
   Summary: ${result.summary}
   Key findings:
${result.keyFindings.joinToString("\n") { "   - $it" }}
            """.trimIndent())

            assertTrue(result.menusAnalyzed > 0) { "Should analyze at least one menu" }
        }
    }
}
