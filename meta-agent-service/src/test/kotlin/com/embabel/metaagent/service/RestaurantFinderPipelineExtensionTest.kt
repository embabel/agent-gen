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

import com.embabel.agent.api.annotation.LlmTool
import com.embabel.agent.api.common.Ai
import com.embabel.metaagent.search.BraveWebSearchService
import com.embabel.metaagent.search.WebSearchRequest
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
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
import org.jsoup.Jsoup
import java.net.URLEncoder
import com.fasterxml.jackson.module.kotlin.readValue

/**
 * Pipeline Extension Test for Restaurant Finder
 * Step 1: Foursquare search → restaurant website URL
 */
@SpringBootTest(classes = [MetaAgentApplication::class])
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
open class RestaurantFinderPipelineExtensionTest {

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

    @Autowired
    lateinit var ai: Ai

    private val logger = LoggerFactory.getLogger(RestaurantFinderPipelineExtensionTest::class.java)

    // ========== MENU TOOLS FOR LLM ==========

    /**
     * Tool class for menu fetching - methods annotated with @LlmTool become tools for the LLM.
     */
    inner class MenuTools {
        private val toolLogger = LoggerFactory.getLogger(MenuTools::class.java)

        @LlmTool(description = "Fetch menu JSON-LD from a URL. Returns structured menu data or error message.")
        fun fetchMenuJson(
            @LlmTool.Param(description = "The URL to fetch menu from (e.g., allmenus.com URL)") url: String
        ): String {
            toolLogger.info("fetchMenuJson called with URL: $url")
            val startTime = System.currentTimeMillis()
            val result = extractMenuJson(url) ?: "No menu JSON-LD found at this URL"
            val elapsed = System.currentTimeMillis() - startTime
            toolLogger.info("fetchMenuJson completed in ${elapsed}ms, result: ${result.take(100)}...")
            return result
        }
    }

    // ========== RESULT DATA CLASSES FOR LLM ==========

    @JsonClassDescription("Comparison of restaurant menus")
    data class MenuComparisonResult(
        @get:JsonPropertyDescription("Summary of all menus analyzed")
        val summary: String,
        @get:JsonPropertyDescription("Number of menus successfully fetched")
        val menusAnalyzed: Int,
        @get:JsonPropertyDescription("Key findings from comparing the menus")
        val keyFindings: List<String>,
    )

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

    protected fun searchFoursquare(query: String, near: String): FoursquareSearchResponse {
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

    // ========== DIFFBOT API (DEPRECATED - low value) ==========

    @Deprecated("Diffbot provides limited value for restaurant details extraction")
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
    @Order(1)
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

    // ========== STEP 2 TEST (DEPRECATED) ==========

    @Test
    @Order(2)
    @Deprecated("Diffbot provides limited value - disabled")
    @org.junit.jupiter.api.Disabled("Diffbot provides limited value for restaurant details")
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

    // ========== STEP 3 TEST (superseded by Step 4) ==========

    @Test
    @Order(3)
    @org.junit.jupiter.api.Disabled("Superseded by Step 4 which extracts menu snippets")
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

    // ========== REUSABLE FUNCTIONS ==========

    /**
     * Build search query for finding menu URL.
     * Uses website URL + geolocation for precision.
     */
    protected fun buildMenuSearchQuery(restaurant: FoursquarePlace): String {
        val websitePart = restaurant.website?.let {
            it.removePrefix("http://").removePrefix("https://").removeSuffix("/")
        } ?: restaurant.name

        val locationPart = listOfNotNull(
            restaurant.location?.locality,
            restaurant.location?.region
        ).joinToString(" ")

        return "$websitePart $locationPart menu site:allmenus.com"
    }

    /**
     * Menu search result containing URL and menu snippets from BraveSearch.
     */
    data class MenuSearchResult(
        val url: String,
        val description: String,
        val title: String,
    )

    /**
     * Find menu info for a restaurant via BraveSearch.
     * Returns MenuSearchResult with URL and description snippets, or null if not found.
     */
    protected fun findMenuInfo(restaurant: FoursquarePlace): MenuSearchResult? {
        val query = buildMenuSearchQuery(restaurant)
        logger.info("Menu search query: $query")

        val rawResponse = braveWebSearchService?.searchRaw(
            WebSearchRequest(query = query, count = 5)  // Get more results to filter
        ) ?: return null

        logger.info("BraveSearch response received: ${rawResponse.length} chars")

        // Parse response to extract menu info
        val responseNode = objectMapper.readTree(rawResponse)
        val webResults = responseNode.path("web").path("results")

        for (result in webResults) {
            val url = result.path("url").asText()
            // Check for specific restaurant menu URLs (not generic city listings)
            // Good: https://www.allmenus.com/nc/greensboro/10813-bravo-italian-kitchen/menu/
            // Bad:  https://www.allmenus.com/ny/new-york/
            val isSpecificRestaurant = url.contains("/menu/") ||
                url.matches(Regex(".*/\\d+-[^/]+/?$"))  // ends with restaurant-id-name pattern

            if ((url.contains("allmenus.com") || url.contains("menupages.com")) && isSpecificRestaurant) {
                val description = result.path("description").asText()
                val title = result.path("title").asText()
                logger.info("Found menu result: $title")
                logger.info("Menu snippets: $description")
                return MenuSearchResult(url = url, description = description, title = title)
            } else if (url.contains("allmenus.com") || url.contains("menupages.com")) {
                logger.debug("Skipping generic listing URL: $url")
            }
        }
        logger.warn("No menu URL found in search results")
        return null
    }

    /**
     * Extract menu JSON-LD from allmenus.com page using Jsoup.
     * Parses HTML and extracts <script type="application/ld+json"> content.
     */
    protected fun extractMenuJson(url: String): String? {
        return try {
            logger.info("Fetching HTML from: $url")
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .timeout(10000)
                .get()

            logger.info("Fetched HTML: ${doc.html().length} chars")

            // Find JSON-LD script tags using Jsoup selectors
            val jsonLdScripts = doc.select("script[type=application/ld+json]")
            logger.info("Found ${jsonLdScripts.size} JSON-LD script tags")

            for ((index, script) in jsonLdScripts.withIndex()) {
                val jsonContent = script.html().trim()
                logger.info("JSON-LD #$index: ${jsonContent.take(200)}...")

                // Check if it contains Menu or Restaurant schema
                if (jsonContent.contains("\"@type\"") &&
                    (jsonContent.contains("\"Menu\"") || jsonContent.contains("\"Restaurant\""))) {
                    logger.info("Found matching JSON-LD with Menu/Restaurant schema")
                    return jsonContent
                }
            }
            logger.warn("No JSON-LD with Menu/Restaurant schema found")
            null
        } catch (e: Exception) {
            logger.warn("Failed to extract menu from $url: ${e.message}")
            null
        }
    }

    /**
     * Parse menu JSON-LD and extract menu items for display.
     * Returns a summary of menu sections and items.
     */
    private fun parseMenuItems(menuJson: String): String {
        return try {
            val root = objectMapper.readTree(menuJson)
            val sb = StringBuilder()

            // Get restaurant name
            val restaurantName = root.path("name").asText("Unknown Restaurant")
            sb.appendLine("Restaurant: $restaurantName")

            // Find menu - could be in "hasMenu" or "menu" property
            val menu = root.path("hasMenu").takeIf { !it.isMissingNode }
                ?: root.path("menu").takeIf { !it.isMissingNode }

            if (menu == null || menu.isMissingNode) {
                sb.appendLine("  (No structured menu found)")
                return sb.toString()
            }

            // Handle array of menus or single menu
            val menus = if (menu.isArray) menu.toList() else listOf(menu)

            for (menuNode in menus) {
                val menuName = menuNode.path("name").asText("Menu")
                sb.appendLine("\n📋 $menuName")

                // Get menu sections
                val sections = menuNode.path("hasMenuSection")
                if (sections.isArray) {
                    for (section in sections.take(5)) {  // Limit to first 5 sections
                        val sectionName = section.path("name").asText("Section")
                        sb.appendLine("  📂 $sectionName")

                        // Get menu items in section
                        val items = section.path("hasMenuItem")
                        if (items.isArray) {
                            for (item in items.take(3)) {  // Limit to first 3 items per section
                                val itemName = item.path("name").asText("Item")
                                val price = item.path("offers").path("price").asText("")
                                    .takeIf { it.isNotEmpty() }
                                    ?: item.path("offers").path("lowPrice").asText("")
                                val priceStr = if (price.isNotEmpty()) " - $$price" else ""
                                sb.appendLine("    • $itemName$priceStr")
                            }
                            val remainingItems = items.size() - 3
                            if (remainingItems > 0) {
                                sb.appendLine("    ... and $remainingItems more items")
                            }
                        }
                    }
                    val remainingSections = sections.size() - 5
                    if (remainingSections > 0) {
                        sb.appendLine("  ... and $remainingSections more sections")
                    }
                }
            }

            sb.toString()
        } catch (e: Exception) {
            "Failed to parse menu: ${e.message}"
        }
    }

    // ========== STEP 4 TEST ==========

    @Test
    @Order(4)
    fun `step 4 - extract menu snippets from BraveSearch`() {
        if (foursquareApiKey.isBlank()) {
            logger.warn("⚠️ FOURSQUARE_API_KEY not set - skipping test")
            return
        }
        if (braveWebSearchService == null) {
            logger.warn("⚠️ BRAVE_API_KEY not set - skipping test")
            return
        }

        logger.info("""
🍽️ ========== STEP 4: EXTRACT MENU SNIPPETS ==========
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

        // Pick random restaurant
        val randomIndex = Random.nextInt(foursquareResponse.results.size)
        val restaurant = foursquareResponse.results[randomIndex]

        logger.info("""
📍 INPUT: Selected Restaurant
   Name: ${restaurant.name}
   Index: ${randomIndex + 1}/${foursquareResponse.results.size}
   Website: ${restaurant.website ?: "N/A"}
   Location: ${restaurant.location?.formatted_address ?: "N/A"}
        """.trimIndent())

        // Step 2: Find menu info via BraveSearch (URL + description snippets)
        logger.info("""
🔎 INTENT: Find menu info via BraveSearch
        """.trimIndent())

        val menuInfo = findMenuInfo(restaurant)
        if (menuInfo == null) {
            logger.warn("❌ No menu info found for ${restaurant.name}")
            return
        }

        logger.info("""
   ✅ Found menu URL: ${menuInfo.url}
        """.trimIndent())

        // Step 3: Extract full menu JSON-LD from the page
        logger.info("""
🔎 INTENT: Extract menu JSON-LD from page
   URL: ${menuInfo.url}
        """.trimIndent())

        val menuJson = extractMenuJson(menuInfo.url)
        if (menuJson == null) {
            logger.warn("❌ No menu JSON-LD found at ${menuInfo.url}")
            return
        }

        val prettyJson = prettyPrintJson(menuJson)
        logger.info("📤 OUTPUT: Menu JSON (${menuJson.length} chars)")
        logger.info("----------------------------------------")
        logger.info(prettyJson.take(3000) + if (prettyJson.length > 3000) "\n... (truncated)" else "")
        logger.info("----------------------------------------")

        assert(menuJson.isNotEmpty()) { "Should extract menu JSON from page" }
    }

    // ========== STEP 5 TEST - MULTIPLE RESTAURANTS ==========

    @Test
    @Order(5)
    fun `step 5 - extract menus for all restaurants`() {
        if (foursquareApiKey.isBlank()) {
            logger.warn("⚠️ FOURSQUARE_API_KEY not set - skipping test")
            return
        }
        if (braveWebSearchService == null) {
            logger.warn("⚠️ BRAVE_API_KEY not set - skipping test")
            return
        }

        logger.info("""
🍽️ ========== STEP 5: EXTRACT MENUS FOR ALL RESTAURANTS ==========
        """.trimIndent())

        // Step 1: Get 5 restaurants from Foursquare
        val foursquareResponse = searchFoursquare(
            query = "italian restaurant",
            near = "Upper East Side, New York, NY"
        )

        if (foursquareResponse.results.isEmpty()) {
            logger.warn("No restaurants found from Foursquare")
            return
        }

        logger.info("Found ${foursquareResponse.results.size} restaurants")

        // Step 2: For each restaurant, find menu URL via BraveSearch (rate-limited)
        val menuResults = mutableListOf<Pair<FoursquarePlace, String?>>()

        for ((index, restaurant) in foursquareResponse.results.withIndex()) {
            logger.info("""
📍 [${index + 1}/${foursquareResponse.results.size}] ${restaurant.name}
   Website: ${restaurant.website ?: "N/A"}
   Location: ${restaurant.location?.formatted_address ?: "N/A"}
            """.trimIndent())

            // Find menu URL (BraveSearch - rate limited to 1 req/sec)
            val menuInfo = findMenuInfo(restaurant)
            if (menuInfo == null) {
                logger.warn("   ❌ No menu URL found")
                menuResults.add(restaurant to null)
                continue
            }
            logger.info("   ✅ Menu URL: ${menuInfo.url}")

            // Extract menu JSON-LD (Jsoup - no rate limit)
            val menuJson = extractMenuJson(menuInfo.url)
            if (menuJson == null) {
                logger.warn("   ❌ No menu JSON-LD found")
                menuResults.add(restaurant to null)
                continue
            }
            logger.info("   ✅ Menu JSON: ${menuJson.length} chars")

            // Parse and display menu items
            val menuSummary = parseMenuItems(menuJson)
            logger.info("   Menu preview:\n$menuSummary")

            menuResults.add(restaurant to menuJson)
        }

        // Summary
        val successCount = menuResults.count { it.second != null }
        logger.info("""
📤 SUMMARY: Extracted $successCount/${menuResults.size} menus
        """.trimIndent())

        menuResults.filter { it.second != null }.forEach { (restaurant, json) ->
            logger.info("   ✅ ${restaurant.name}: ${json!!.length} chars")
        }

        assert(successCount > 0) { "Should extract at least one menu" }
    }

    // ========== STEP 6a: LLM WITH TOOLS (SEQUENTIAL) ==========

    @Test
    @Order(6)
    fun `step 6a - LLM fetches menus using tool (sequential)`() {
        if (foursquareApiKey.isBlank()) {
            logger.warn("⚠️ FOURSQUARE_API_KEY not set - skipping test")
            return
        }
        if (braveWebSearchService == null) {
            logger.warn("⚠️ BRAVE_API_KEY not set - skipping test")
            return
        }

        logger.info("""
🤖 ========== STEP 6a: LLM FETCHES MENUS (SEQUENTIAL) ==========
        """.trimIndent())

        // Step 1: Get restaurants and their menu URLs
        val foursquareResponse = searchFoursquare(
            query = "italian restaurant",
            near = "Upper East Side, New York, NY"
        )

        if (foursquareResponse.results.isEmpty()) {
            logger.warn("No restaurants found from Foursquare")
            return
        }

        // Step 2: Find menu URLs for each restaurant (rate-limited BraveSearch)
        val menuUrls = mutableListOf<Pair<String, String>>()  // name to URL
        for (restaurant in foursquareResponse.results.take(3)) {  // Limit to 3 for testing
            val menuInfo = findMenuInfo(restaurant)
            if (menuInfo != null) {
                menuUrls.add(restaurant.name to menuInfo.url)
                logger.info("Found menu URL for ${restaurant.name}: ${menuInfo.url}")
            }
        }

        if (menuUrls.isEmpty()) {
            logger.warn("No menu URLs found - skipping LLM step")
            return
        }

        logger.info("Found ${menuUrls.size} menu URLs, invoking LLM with tools...")

        // Step 3: Invoke LLM with MenuTools to fetch and compare menus
        val menuTools = MenuTools()
        val urlList = menuUrls.joinToString("\n") { "- ${it.first}: ${it.second}" }

        val startTime = System.currentTimeMillis()

        val result = ai.withDefaultLlm()
            .withToolObject(menuTools)
            .creating(MenuComparisonResult::class.java)
            .fromPrompt("""
                You have access to a tool called fetchMenuJson that fetches menu JSON-LD from a URL.

                Fetch menus from these URLs by calling fetchMenuJson for each one.
                IMPORTANT: Call fetchMenuJson for ALL URLs in a single response (multiple parallel tool calls).

                $urlList

                After getting all menu data, provide:
                1. A summary of cuisine/dishes each restaurant offers
                2. Key findings comparing menus (price ranges, variety, specialties)
            """.trimIndent())

        val elapsed = System.currentTimeMillis() - startTime
        logger.info("""
📤 OUTPUT: LLM Menu Comparison (${elapsed}ms)
   Menus analyzed: ${result.menusAnalyzed}
   Summary: ${result.summary}
   Key findings:
${result.keyFindings.joinToString("\n") { "   - $it" }}
        """.trimIndent())

        assert(result.menusAnalyzed > 0) { "Should analyze at least one menu" }
    }
}
