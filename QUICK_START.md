# QUICK HOW-TO-RUN EXAMPLE

```
bash

cd meta-agent
mvn clean install
cd meta-agent-service
mvn spring-boot:run
.....................
on shell prompt enter:
design --intent "create restaurant planner agent using opentableAPIs"
```

you will see output:
```
kotlin

package com.example.restaurantplanner
import org.springframework.stereotype.Component
import embabel.agent.annotation.Agent
import embabel.agent.annotation.Action
import embabel.agent.annotation.AchievesGoal
@Agent(name = "restaurant planner agent", domain = "restaurant reservation and planning", description = "Create an agent that uses OpenTable APIs to assist users in planning restaurant visits, including searching for restaurants, checking availability, and booking reservations.")
@Component
class RestaurantPlannerAgent {
    @Action("search restaurants")
    @AchievesGoal("resource_managed")
    fun searchRestaurants(
        location: String,
        cuisine: String?,
        date: String,
        time: String,
        partySize: Int
    ): List<Restaurant> {
        // Implementation would call OpenTable API to search restaurants matching criteria
        TODO("Call OpenTable API to search restaurants")
    }
    @Action("check restaurant availability")
    @AchievesGoal("resource_managed")
    fun checkRestaurantAvailability(
        restaurantId: String,
        date: String,
        time: String,
        partySize: Int
    ): Boolean {
        // Implementation would call OpenTable API to confirm availability
        TODO("Call OpenTable API to check availability")
    }
    @Action("make restaurant reservations")
    @AchievesGoal("schedule_created")
    fun makeReservation(
        restaurantId: String,
        date: String,
        time: String,
        partySize: Int,
        customerName: String,
        customerContact: String
    ): ReservationConfirmation {
        // Implementation would call OpenTable API to book reservation
        TODO("Call OpenTable API to make reservation")
    }
}
data class Restaurant(
    val id: String,
    val name: String,
    val address: String,
    val cuisine: String,
    val rating: Double
)
data class ReservationConfirmation(
    val confirmationId: String,
    val restaurantId: String,
    val date: String,
    val time: String,
    val partySize: Int
)
```
```