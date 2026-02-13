/*
 * Copyright 2024-2025 Embabel Software, Inc.
 */
package com.embabel.metaagent.restaurantfinder

import com.embabel.agent.api.annotation.LlmTool
import com.embabel.metaagent.search.BraveWebSearchService
import com.embabel.metaagent.search.WebSearchRequest
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URLEncoder

/**
 * Tools for RestaurantFinder agent.
 *
 * Tool 1 & 2: Called directly by agent (not via LLM)
 * Tool 3 (readMenuAsJson): The actual LLM tool for menu comparison
 */
@Service
class RestaurantFinderTools(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${FOURSQUARE_API_KEY:}") private val foursquareApiKey: String = "",
    private val braveWebSearchService: BraveWebSearchService? = null
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // FourSquare API response types
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class FoursquareSearchResponse(val results: List<FoursquarePlace> = emptyList())

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class FoursquarePlace(
        val fsq_place_id: String = "",
        val name: String = "",
        val location: FoursquareLocation? = null,
        val website: String? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class FoursquareLocation(
        val locality: String? = null,
        val region: String? = null,
        val formatted_address: String? = null
    )

    data class MenuSearchResult(val url: String, val title: String)

    /**
     * Tool 1: Find restaurant URLs using FourSquare API.
     * Called directly by agent, NOT via LLM.
     */
    @LlmTool(description = "Find nearby restaurant URLs using FourSquare API")
    fun findRestaurantUrls(query: String, near: String): NearbyRestaurants {
        logger.info("findRestaurantUrls called with: query=$query, near=$near")

        if (foursquareApiKey.isBlank()) {
            logger.warn("FOURSQUARE_API_KEY not set, returning empty list")
            return NearbyRestaurants(emptyList())
        }

        return try {
            val headers = HttpHeaders().apply {
                set("Authorization", "Bearer $foursquareApiKey")
                set("Accept", "application/json")
                set("X-Places-Api-Version", "2025-06-17")
            }
            val url = "https://places-api.foursquare.com/places/search?" +
                "query=${URLEncoder.encode(query, "UTF-8")}&" +
                "near=${URLEncoder.encode(near, "UTF-8")}&limit=5"

            val response = restTemplate.exchange(
                url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java
            )

            val fsResponse = objectMapper.readValue(response.body, FoursquareSearchResponse::class.java)
            val restaurants = fsResponse.results.map { place ->
                Restaurant(
                    name = place.name,
                    address = place.location?.formatted_address ?: "",
                    menuUrl = place.website ?: ""
                )
            }

            logger.info("Found ${restaurants.size} restaurants from FourSquare")
            NearbyRestaurants(restaurants)

        } catch (e: Exception) {
            logger.error("FourSquare API error: ${e.message}", e)
            NearbyRestaurants(emptyList())
        }
    }

    /**
     * Tool 2: Get menu URLs for restaurants using Brave Search.
     * Called directly by agent, NOT via LLM.
     */
    @LlmTool(description = "Get menu URLs for nearby restaurants")
    fun getMenuUrls(nearbyRestaurants: NearbyRestaurants): RestaurantMenus {
        logger.info("getMenuUrls called for ${nearbyRestaurants.restaurants.size} restaurants")

        if (braveWebSearchService == null) {
            logger.warn("BraveWebSearchService not available, using restaurant website URLs")
            val menus = nearbyRestaurants.restaurants.map { r ->
                MenuLink(r.name, r.menuUrl, "")
            }
            return RestaurantMenus(menus)
        }

        val menus = nearbyRestaurants.restaurants.mapNotNull { restaurant ->
            findMenuInfo(restaurant)?.let { menuResult ->
                MenuLink(restaurant.name, menuResult.url, "")
            } ?: if (restaurant.menuUrl.isNotBlank()) {
                MenuLink(restaurant.name, restaurant.menuUrl, "")
            } else null
        }

        logger.info("Found ${menus.size} menu URLs")
        return RestaurantMenus(menus)
    }

    /**
     * Find menu URL for a restaurant using Brave Search.
     * Pattern from RestaurantFinderParallelToolLoopTest.
     */
    private fun findMenuInfo(restaurant: Restaurant): MenuSearchResult? {
        val websitePart = restaurant.menuUrl
            .removePrefix("http://")
            .removePrefix("https://")
            .removeSuffix("/")
            .ifBlank { restaurant.name }
        val locationPart = restaurant.address
        val query = "$websitePart $locationPart menu site:allmenus.com"

        val rawResponse = braveWebSearchService?.searchRaw(
            WebSearchRequest(query = query, count = 5)
        ) ?: return null

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

    /**
     * Tool 3: Read menu by URL and extract JSON-LD.
     * Pattern from RestaurantFinderParallelToolLoopTest.
     */
    @LlmTool(description = "Read restaurant menu from URL and return as JSON for comparison")
    fun readMenuAsJson(menuUrl: String): String? {
        logger.info("readMenuAsJson called for: $menuUrl")
        return try {
            val doc = Jsoup.connect(menuUrl)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get()
            doc.select("script[type=application/ld+json]")
                .map { it.html().trim() }
                .firstOrNull { it.contains("\"Menu\"") || it.contains("\"Restaurant\"") }
        } catch (e: Exception) { null }
    }
}
