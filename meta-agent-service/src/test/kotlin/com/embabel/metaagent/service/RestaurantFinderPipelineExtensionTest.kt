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

import com.embabel.metaagent.search.BraveWebSearchService
import com.embabel.metaagent.search.WebSearchRequest
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.random.Random
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import java.net.URLEncoder

/**
 * Pipeline Extension Test for Restaurant Finder
 * Step 1: Foursquare search → restaurant website URL
 */
@SpringBootTest(classes = [MetaAgentApplication::class])
@ActiveProfiles("test")
class RestaurantFinderPipelineExtensionTest {

    @Autowired
    lateinit var restTemplate: RestTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private val prettyPrinter: ObjectMapper by lazy {
        objectMapper.copy().enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT)
    }

    @Value("\${FOURSQUARE_API_KEY:}")
    lateinit var foursquareApiKey: String

    @Value("\${DIFFBOT_TOKEN:}")
    lateinit var diffbotToken: String

    @Autowired(required = false)
    var braveWebSearchService: BraveWebSearchService? = null

    private val logger = LoggerFactory.getLogger(RestaurantFinderPipelineExtensionTest::class.java)

    companion object {
        private const val FOURSQUARE_BASE_URL = "https://places-api.foursquare.com/places/search"
        private const val FOURSQUARE_API_VERSION = "2025-06-17"
        private const val DIFFBOT_API_URL = "https://api.diffbot.com/v3/analyze"

        @BeforeAll
        @JvmStatic
        fun setUp() {
            System.setProperty("embabel.agent.shell.interactive.enabled", "false")
        }
    }

    // ========== FOURSQUARE DATA CLASSES ==========

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class FoursquareSearchResponse(
        val results: List<FoursquarePlace> = emptyList(),
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class FoursquarePlace(
        val fsq_place_id: String,
        val name: String,
        val location: FoursquareLocation? = null,
        val categories: List<FoursquareCategory>? = null,
        val website: String? = null,
        val menu: String? = null,
        val link: String? = null,
        val tel: String? = null,
        val rating: Double? = null,
        val price: Int? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class FoursquareLocation(
        val address: String? = null,
        val locality: String? = null,
        val region: String? = null,
        val formatted_address: String? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class FoursquareCategory(
        val id: Int? = null,
        val name: String? = null,
    )

    // ========== FOURSQUARE SEARCH ==========

    private fun searchFoursquare(query: String, near: String): FoursquareSearchResponse {
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $foursquareApiKey")
            set("Accept", "application/json")
            set("X-Places-Api-Version", FOURSQUARE_API_VERSION)
        }

        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val encodedNear = URLEncoder.encode(near, "UTF-8")
        val url = "$FOURSQUARE_BASE_URL?query=$encodedQuery&near=$encodedNear&limit=5"

        logger.info("Foursquare request: $url")

        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            HttpEntity<String>(headers),
            String::class.java,
        )

        logger.debug("Foursquare raw response: ${response.body}")

        return objectMapper.readValue(response.body, FoursquareSearchResponse::class.java)
    }

    private fun prettyPrintJson(rawJson: String): String {
        return try {
            val jsonNode = objectMapper.readTree(rawJson)
            prettyPrinter.writeValueAsString(jsonNode)
        } catch (e: Exception) {
            rawJson // fallback to raw if parsing fails
        }
    }

    // ========== DIFFBOT API ==========

    private fun analyzeDiffbot(url: String): String {
        logger.info("Diffbot URL: $url")
        logger.info("Diffbot token length: ${diffbotToken.length}")

        val headers = HttpHeaders().apply {
            set("Accept", "application/json")
        }

        val response = restTemplate.exchange(
            "$DIFFBOT_API_URL?url={url}&token={token}",
            HttpMethod.GET,
            HttpEntity<String>(headers),
            String::class.java,
            mapOf("url" to url, "token" to diffbotToken)
        )

        logger.info("Diffbot response status: ${response.statusCode}")
        return response.body ?: "{}"
    }

    // ========== STEP 1 TEST ==========

    @Test
    fun `step 1 - foursquare search returns restaurant with website`() {
        if (foursquareApiKey.isBlank()) {
            logger.warn("⚠️ FOURSQUARE_API_KEY not set - skipping test")
            return
        }

        logger.info("""
🍝 ========== STEP 1: FOURSQUARE SEARCH ==========
🎯 Query: italian restaurant near Upper East Side, New York, NY
        """.trimIndent())

        val response = searchFoursquare(
            query = "italian restaurant",
            near = "Upper East Side, New York, NY"
        )

        logger.info("Found ${response.results.size} restaurants:")
        response.results.forEachIndexed { index, place ->
            logger.info("   ${index + 1}. ${place.name}")
            logger.info("      ID: ${place.fsq_place_id}")
            logger.info("      Address: ${place.location?.formatted_address}")
            logger.info("      Website: ${place.website ?: "N/A"}")
            logger.info("      Menu: ${place.menu ?: "N/A"}")
            logger.info("      Link: ${place.link ?: "N/A"}")
            logger.info("      Rating: ${place.rating ?: "N/A"}")
        }

        assert(response.results.isNotEmpty()) { "Should find Italian restaurants in Upper East Side" }
    }

    // ========== STEP 2 TEST ==========

    @Test
    fun `step 2 - diffbot returns restaurant details as JSON`() {
        if (foursquareApiKey.isBlank()) {
            logger.warn("⚠️ FOURSQUARE_API_KEY not set - skipping test")
            return
        }
        if (diffbotToken.isBlank()) {
            logger.warn("⚠️ DIFFBOT_TOKEN not set - skipping test")
            return
        }

        logger.info("""
🔍 ========== STEP 2: DIFFBOT FOR RESTAURANT DETAILS ==========
        """.trimIndent())

        // Step 1: Get restaurants from Foursquare
        val foursquareResponse = searchFoursquare(
            query = "italian restaurant",
            near = "Upper East Side, New York, NY"
        )

        if (foursquareResponse.results.isEmpty()) {
            logger.warn("No restaurants found from Foursquare")
            return
        }

        // Pick random restaurant from results
        val randomIndex = Random.nextInt(foursquareResponse.results.size)
        val restaurant = foursquareResponse.results[randomIndex]

        logger.info("""
📍 INPUT: Selected Restaurant
   Name: ${restaurant.name}
   Index: ${randomIndex + 1}/${foursquareResponse.results.size}
   Website: ${restaurant.website ?: "N/A"}
   Menu: ${restaurant.menu ?: "N/A"}
   Address: ${restaurant.location?.formatted_address ?: "N/A"}
        """.trimIndent())

        // Step 2: Use Diffbot to analyze restaurant website
        val websiteUrl = restaurant.website
        if (websiteUrl.isNullOrBlank()) {
            logger.warn("⚠️ No website URL for restaurant - skipping Diffbot analysis")
            return
        }

        logger.info("""
🔎 INTENT: Extract restaurant details via Diffbot
   URL: $websiteUrl
        """.trimIndent())

        val rawJsonResponse = analyzeDiffbot(websiteUrl)

        val prettyJson = prettyPrintJson(rawJsonResponse)
        logger.info("📤 OUTPUT: Diffbot JSON Response")
        logger.info("----------------------------------------")
        logger.info(prettyJson)
        logger.info("----------------------------------------")

        assert(rawJsonResponse.isNotEmpty()) { "Should get JSON response from Diffbot" }
    }

    // ========== STEP 3 TEST ==========

    @Test
    fun `step 3 - brave search finds menu URLs`() {
        if (foursquareApiKey.isBlank()) {
            logger.warn("⚠️ FOURSQUARE_API_KEY not set - skipping test")
            return
        }
        if (braveWebSearchService == null) {
            logger.warn("⚠️ BRAVE_API_KEY not set - skipping test")
            return
        }

        logger.info("""
📋 ========== STEP 3: BRAVE SEARCH FOR MENU URLs ==========
        """.trimIndent())

        // Get restaurants from Foursquare
        val foursquareResponse = searchFoursquare(
            query = "italian restaurant",
            near = "Upper East Side, New York, NY"
        )

        if (foursquareResponse.results.isEmpty()) {
            logger.warn("No restaurants found from Foursquare")
            return
        }

        // Pick random restaurant
        val randomIndex = Random.nextInt(foursquareResponse.results.size)
        val restaurant = foursquareResponse.results[randomIndex]

        logger.info("""
📍 INPUT: Selected Restaurant
   Name: ${restaurant.name}
   Index: ${randomIndex + 1}/${foursquareResponse.results.size}
   Website: ${restaurant.website ?: "N/A"}
   Menu (from Foursquare): ${restaurant.menu ?: "N/A"}
        """.trimIndent())

        // Search for menu URL
        val menuQuery = "${restaurant.name} menu site:menupages.com OR site:allmenus.com"

        logger.info("""
🔎 INTENT: Find menu URL for restaurant
   Query: $menuQuery
        """.trimIndent())

        val rawJsonResponse = braveWebSearchService!!.searchRaw(
            WebSearchRequest(query = menuQuery, count = 3)
        )

        val prettyJson = prettyPrintJson(rawJsonResponse)
        logger.info("📤 OUTPUT: Brave Search JSON Response (Menu URLs)")
        logger.info("----------------------------------------")
        logger.info(prettyJson)
        logger.info("----------------------------------------")

        assert(rawJsonResponse.isNotEmpty()) { "Should get raw JSON response for menu search" }
    }
}
