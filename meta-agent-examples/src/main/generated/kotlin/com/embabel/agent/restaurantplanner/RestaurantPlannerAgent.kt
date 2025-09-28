package com.embabel.agent.restaurantplanner

import com.embabel.agent.api.annotation.*

@Agent(description = "Create an agent using MetaAgent framework that plans restaurant visits by leveraging OpenTable APIs for searching, booking, and managing restaurant reservations.")
class RestaurantPlannerAgent {

    @Action(cost = 1.0)
    @AchievesGoal(description = "assist users in planning restaurant visits")
    fun searchRestaurants(location: String, cuisine: String?, date: String?, time: String?, partySize: Int): List<Restaurant> {
        // Implementation to call OpenTable API for searching restaurants
        TODO()
    }

    @Action(cost = 1.5)
    @AchievesGoal(description = "provide real-time restaurant availability")
    fun checkAvailability(restaurantId: String, date: String, time: String, partySize: Int): AvailabilityStatus {
        // Implementation to check availability via OpenTable API
        TODO()
    }

    @Action(cost = 2.0)
    @AchievesGoal(description = "facilitate reservation management")
    fun makeReservation(restaurantId: String, date: String, time: String, partySize: Int, customerName: String, customerContact: String): ReservationConfirmation {
        // Implementation to make reservation via OpenTable API
        TODO()
    }

    @Action(cost = 1.5)
    @AchievesGoal(description = "facilitate reservation management")
    fun modifyReservation(reservationId: String, newDate: String?, newTime: String?, newPartySize: Int?): ReservationModificationConfirmation {
        // Implementation to modify reservation via OpenTable API
        TODO()
    }

    @Action(cost = 1.2)
    @AchievesGoal(description = "facilitate reservation management")
    fun cancelReservation(reservationId: String): CancellationConfirmation {
        // Implementation to cancel reservation via OpenTable API
        TODO()
    }

    @Action(cost = 1.0)
    @AchievesGoal(description = "assist users in planning restaurant visits")
    fun retrieveRestaurantDetails(restaurantId: String): RestaurantDetails {
        // Implementation to retrieve restaurant details via OpenTable API
        TODO()
    }
}

data class Restaurant(
    val id: String,
    val name: String,
    val cuisine: String,
    val location: String,
    val rating: Double
)

data class AvailabilityStatus(
    val isAvailable: Boolean,
    val availableTimes: List<String>
)

data class ReservationConfirmation(
    val reservationId: String,
    val restaurantId: String,
    val date: String,
    val time: String,
    val partySize: Int
)

data class ReservationModificationConfirmation(
    val reservationId: String,
    val modifiedDate: String?,
    val modifiedTime: String?,
    val modifiedPartySize: Int?
)

data class CancellationConfirmation(
    val reservationId: String,
    val cancelled: Boolean
)

data class RestaurantDetails(
    val id: String,
    val name: String,
    val cuisine: String,
    val location: String,
    val phone: String,
    val website: String,
    val openingHours: String,
    val menu: List<String>
)