package com.embabel.metaagent.restaurantfinder

import com.embabel.agent.api.annotation.*
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.domain.io.UserInput
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * RestaurantFinder agent - finds nearby restaurants, retrieves menus, and compares them.
 *
 * Uses RestaurantFinderTools:
 * - findRestaurantUrls (Tool 1): Called directly to find restaurants via FourSquare
 * - getMenuUrls (Tool 2): Called directly to find menu URLs
 * - readMenuAsJson (Tool 3): LLM tool used during compareMenus for menu comparison
 */
@Component
@Agent(description = "This agent finds nearby restaurants, retrieves and compares their menus to help selection")
class RestaurantFinder(
    private val tools: RestaurantFinderTools
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Action(description = "Find nearby restaurants based on user input", cost = 1.0, value = 0.8)
    @ToolGroup("default")
    fun findRestaurants(input: UserInput, context: OperationContext): NearbyRestaurants {
        logger.info("findRestaurants called with: input=${input.content}")

        // Parse query and location from user input
        val query = extractQuery(input.content)
        val location = extractLocation(input.content)

        // Call Tool 1 directly (not via LLM)
        return tools.findRestaurantUrls(query, location)
    }

    @Action(description = "Retrieve menu URLs for the given nearby restaurants", cost = 1.0, value = 0.8)
    @ToolGroup("default")
    fun getMenus(nearbyRestaurants: NearbyRestaurants): RestaurantMenus {
        logger.info("getMenus called with: ${nearbyRestaurants.restaurants.size} restaurants")

        // Call Tool 2 directly (not via LLM)
        return tools.getMenuUrls(nearbyRestaurants)
    }

    @AchievesGoal(description = "Help user find nearby restaurants, provide menu info, and compare menus")
    @Action(description = "Compare menus of top restaurants to aid selection", cost = 1.0, value = 0.8)
    @ToolGroup("default")
    fun compareMenus(restaurantMenus: RestaurantMenus, context: OperationContext): MenuComparisonResult {
        logger.info("compareMenus called with: ${restaurantMenus.menus.size} menus")

        // Create dynamic tools for each menu - LLM will call these
        val menuTools = restaurantMenus.menus.map { menu ->
            val safeName = menu.restaurantName.replace(Regex("[^a-zA-Z0-9]"), "_").lowercase()
            com.embabel.agent.api.tool.Tool.of(
                name = "read_${safeName}_menu",
                description = "Read menu JSON for ${menu.restaurantName}"
            ) { _ ->
                logger.info("Reading menu for ${menu.restaurantName}: ${menu.url}")
                com.embabel.agent.api.tool.Tool.Result.text(tools.readMenuAsJson(menu.url) ?: "")
            }
        }

        val toolNames = menuTools.map { it.definition.name }

        // LLM calls tools to read menus, then compares
        val comparison = context.promptRunner()
            .withTools(menuTools)
            .generateText(
                """
                You have ${menuTools.size} tools to read restaurant menus: ${toolNames.joinToString(", ")}

                Call ALL tools to fetch menus, then compare them.

                Provide comparison summary.
            """.trimIndent()
            )

        logger.info(comparison);
        return MenuComparisonResult(comparison)
    }

    private fun extractQuery(input: String): String {
        // Extract what comes before "restaurant" - e.g., "find italian restaurant" -> "italian restaurant"
        val pattern = Regex("(?:find\\s+)?(\\w+)\\s+restaurant", RegexOption.IGNORE_CASE)
        return pattern.find(input)?.let { "${it.groupValues[1]} restaurant" }
            ?: "restaurant"
    }

    private fun extractLocation(input: String): String {
        // Required - look for "near" or "in" followed by location
        val nearPattern = Regex("(?:near|in|at)\\s+([^,]+)", RegexOption.IGNORE_CASE)
        return nearPattern.find(input)?.groupValues?.get(1)?.trim()
            ?: throw IllegalArgumentException("Location is required. Please specify 'near <location>' in your request.")
    }
}

data class Restaurant(val name: String, val address: String, val menuUrl: String)
data class NearbyRestaurants(val restaurants: List<Restaurant>)
data class MenuLink(val restaurantName: String, val url: String, val content: String)
data class RestaurantMenus(val menus: List<MenuLink>)
data class MenuComparisonResult(val comparisonSummary: String)