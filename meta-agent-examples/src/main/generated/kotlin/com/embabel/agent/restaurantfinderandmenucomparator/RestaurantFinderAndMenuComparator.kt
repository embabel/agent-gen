package com.embabel.agent.restaurantfinderandmenucomparator

import com.embabel.agent.api.annotation.*

data class Restaurant(val name: String, val address: String, val rating: Double, val menu: List<MenuItem>)
data class MenuItem(val name: String, val price: Double, val description: String?)

/**
 * find restaurants near me using natural language and compare menus for top 2 restaurants
 */
@Agent(description = "This agent should utilize MetaAgent capabilities to locate restaurants near the user's location through natural language queries, then identify the top 2 restaurants based on relevance or ranking, and subsequently compare their menus to aid the user in decision making.")
class RestaurantFinderAndMenuComparatorAgent {

    @Action(cost = 1.0)
    @AchievesGoal(description = "restaurants_found")
    fun findRestaurantsNearLocation(naturalLanguageQuery: String, userLocation: String): List<Restaurant> {
        // Implementation would use natural language query and location to find restaurants
        return emptyList()
    }

    @Action(cost = 0.5)
    @AchievesGoal(description = "top_restaurants_identified")
    fun identifyTopTwoRestaurants(restaurants: List<Restaurant>): List<Restaurant> {
        // Implementation would select top 2 restaurants based on rating or relevance
        return restaurants.sortedByDescending { it.rating }.take(2)
    }

    @Action(cost = 1.5)
    @AchievesGoal(description = "menus_compared")
    fun retrieveAndCompareMenus(topRestaurants: List<Restaurant>): Map<Restaurant, List<MenuItem>> {
        // Implementation would retrieve menus and prepare a comparison
        return topRestaurants.associateWith { it.menu }
    }

}