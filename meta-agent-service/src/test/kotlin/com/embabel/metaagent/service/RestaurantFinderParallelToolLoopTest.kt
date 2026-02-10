/*
 * Copyright 2024-2025 Embabel Software, Inc.
 */
package com.embabel.metaagent.service

import com.embabel.agent.api.common.Ai
import com.embabel.agent.api.tool.Tool
import com.embabel.metaagent.search.BraveWebSearchService
import com.embabel.metaagent.search.WebSearchRequest
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.RestTemplate
import org.jsoup.Jsoup
import java.net.URLEncoder

/**
 * Parallel Tool Loop Test - enables ParallelToolLoop for concurrent tool execution.
 */
@SpringBootTest(classes = [MetaAgentApplication::class])
@TestPropertySource(properties = [
    "embabel.agent.platform.toolloop.type=parallel",
    "logging.level.com.embabel.agent.spi.loop.support.ParallelToolLoop=DEBUG",
    "logging.level.com.embabel.agent.spi.loop.support.DefaultToolLoop=DEBUG"
])
@ActiveProfiles("test")
class RestaurantFinderParallelToolLoopTest {

    @Autowired lateinit var restTemplate: RestTemplate
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var ai: Ai
    @Value("\${FOURSQUARE_API_KEY:}") lateinit var foursquareApiKey: String
    @Autowired(required = false) var braveWebSearchService: BraveWebSearchService? = null

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        @BeforeAll @JvmStatic
        fun setUp() { System.setProperty("embabel.agent.shell.interactive.enabled", "false") }
    }

    // Data classes
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class FoursquareSearchResponse(val results: List<FoursquarePlace> = emptyList())
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class FoursquarePlace(val fsq_place_id: String, val name: String, val location: FoursquareLocation? = null, val website: String? = null)
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class FoursquareLocation(val locality: String? = null, val region: String? = null, val formatted_address: String? = null)
    data class MenuSearchResult(val url: String, val title: String)

    @JsonClassDescription("Comparison of restaurant menus")
    data class MenuComparisonResult(
        @get:JsonPropertyDescription("Summary of menus") val summary: String,
        @get:JsonPropertyDescription("Menus fetched") val menusAnalyzed: Int,
        @get:JsonPropertyDescription("Key findings") val keyFindings: List<String>,
    )

    /**
     * Create a dynamically-named tool for a specific restaurant.
     * Each tool has a unique name so LLM sees them as distinct tools.
     */
    private fun createMenuTool(restaurantName: String, menuUrl: String): Tool {
        val safeName = restaurantName.replace(Regex("[^a-zA-Z0-9]"), "_").lowercase()
        val toolName = "fetch_${safeName}_menu"

        return Tool.of(
            name = toolName,
            description = "Fetch menu for $restaurantName restaurant"
        ) { _ ->
            val thread = Thread.currentThread().name
            logger.info("[$thread] $toolName START: $menuUrl")
            val start = System.currentTimeMillis()
            val result = extractMenuJson(menuUrl) ?: "No menu found for $restaurantName"
            logger.info("[$thread] $toolName END: ${System.currentTimeMillis() - start}ms")
            Tool.Result.text(result)
        }
    }

    private fun searchFoursquare(query: String, near: String): FoursquareSearchResponse {
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $foursquareApiKey")
            set("Accept", "application/json")
            set("X-Places-Api-Version", "2025-06-17")
        }
        val url = "https://places-api.foursquare.com/places/search?query=${URLEncoder.encode(query, "UTF-8")}&near=${URLEncoder.encode(near, "UTF-8")}&limit=5"
        val response = restTemplate.exchange(url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java)
        return objectMapper.readValue(response.body, FoursquareSearchResponse::class.java)
    }

    private fun findMenuInfo(restaurant: FoursquarePlace): MenuSearchResult? {
        val websitePart = restaurant.website?.removePrefix("http://")?.removePrefix("https://")?.removeSuffix("/") ?: restaurant.name
        val locationPart = listOfNotNull(restaurant.location?.locality, restaurant.location?.region).joinToString(" ")
        val query = "$websitePart $locationPart menu site:allmenus.com"

        val rawResponse = braveWebSearchService?.searchRaw(WebSearchRequest(query = query, count = 5)) ?: return null
        val webResults = objectMapper.readTree(rawResponse).path("web").path("results")

        for (result in webResults) {
            val url = result.path("url").asText()
            if ((url.contains("allmenus.com") || url.contains("menupages.com")) &&
                (url.contains("/menu/") || url.matches(Regex(".*/\\d+-[^/]+/?$")))) {
                return MenuSearchResult(url, result.path("title").asText())
            }
        }
        return null
    }

    private fun extractMenuJson(url: String): String? {
        return try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get()
            doc.select("script[type=application/ld+json]")
                .map { it.html().trim() }
                .firstOrNull { it.contains("\"Menu\"") || it.contains("\"Restaurant\"") }
        } catch (e: Exception) { null }
    }

    @Test
    fun `parallel tool loop test`() {
        if (foursquareApiKey.isBlank() || braveWebSearchService == null) {
            logger.warn("⚠️ API keys not set - skipping")
            return
        }

        logger.info("🚀 ========== PARALLEL TOOL LOOP TEST ==========")

        val restaurants = searchFoursquare("italian restaurant", "Upper East Side, New York, NY").results
        if (restaurants.isEmpty()) return

        // Find menu URLs (sequential - rate limited)
        val menuUrls = restaurants.take(3).mapNotNull { r ->
            findMenuInfo(r)?.let { r.name to it.url }
        }

        if (menuUrls.isEmpty()) {
            logger.warn("No menu URLs found")
            return
        }

        logger.info("Found ${menuUrls.size} URLs, creating separate tool per restaurant...")

        // Create dynamically-named tools for each restaurant
        val tools = menuUrls.map { (name, url) -> createMenuTool(name, url) }
        val toolNames = tools.map { it.definition.name }

        logger.info("Tools created: $toolNames")
        logger.info("Invoking LLM with ${tools.size} separate tools...")

        val startTime = System.currentTimeMillis()

        // Build prompt runner with all tools
        val result = ai.withDefaultLlm()
            .withTools(tools)
            .creating(MenuComparisonResult::class.java)
            .fromPrompt("""
                You have access to ${tools.size} tools for fetching restaurant menus:
                ${toolNames.joinToString(", ")}

                Call ALL tools to fetch all menus, then compare them.

                Provide summary and key findings.
            """.trimIndent())

        val elapsed = System.currentTimeMillis() - startTime
        logger.info("""
📤 PARALLEL Result (${elapsed}ms)
   Menus: ${result.menusAnalyzed}
   Summary: ${result.summary}
        """.trimIndent())

        assert(result.menusAnalyzed > 0)
    }
}
