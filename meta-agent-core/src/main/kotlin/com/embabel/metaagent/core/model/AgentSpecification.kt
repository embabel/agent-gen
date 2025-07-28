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
package com.embabel.metaagent.core.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

/**
 * AgentSpecification - Comprehensive Agent Architecture Specification
 * 
 * This data class represents a complete specification for generating an agent,
 * gathered through iterative user input and LLM processing. It serves as the
 * definitive blueprint for agent generation.
 * 
 * ## Iterative Gathering Process:
 * 
 * 1. **Domain Discovery**: "What domain should this agent work in?"
 * 2. **Specification Gathering**: "What should this agent accomplish?"
 * 3. **Action Intent Collection**: "What actions should it perform?"
 * 4. **Goal Intent Definition**: "What goals should it achieve?"
 * 5. **Example Provision**: "Can you provide examples or use cases?"
 * 6. **LLM Processing**: Transform intents into concrete actions/goals
 * 7. **User Confirmation**: Present final specification for approval
 * 
 * ## Dual Input Modes:
 * 
 * **Mode A: Free Text Processing**
 * ```kotlin
 * val userInput = UserInput(content = "Create an agent that helps users book restaurants")
 * val spec = metaAgent.processContent(userInput) // Extract structure from free text
 * ```
 * 
 * **Mode B: Structured Input**
 * ```kotlin
 * val spec = metaAgent.processStructuredInput(
 *     domain = "restaurant-booking",
 *     specification = "Help users find and book restaurant reservations",
 *     actionIntents = listOf("search restaurants", "make reservations"),
 *     goalIntents = listOf("find suitable restaurant", "complete booking"),
 *     examples = listOf("Book dinner for 4 people", "Find Italian restaurants nearby")
 * )
 * ```
 * 
 * ## LLM Integration Points:
 * 
 * The AgentSpecification is designed for optimal LLM processing:
 * - **Structured prompt**: Domain, specification, intents, examples
 * - **Action finalization**: LLM converts intents to concrete method names
 * - **Goal finalization**: LLM converts intents to GOAP world states
 * - **User confirmation**: Present finalized design for approval
 * 
 * @property name Agent class name (e.g., "RestaurantBookingAgent")
 * @property domain Target domain for specialization
 * @property specification Detailed description of agent purpose and capabilities
 * @property actionIntents High-level action intentions (before LLM finalization)
 * @property finalizedActions Concrete action method names (after LLM processing)
 * @property goalIntents High-level goal intentions (before LLM finalization)
 * @property finalizedGoals GOAP world states (after LLM processing)
 * @property examples Use cases and example scenarios
 * @property userConfirmed Whether user has confirmed this specification
 * @property specificationTimestamp When this specification was created
 * 
 * @author Meta-Agent Framework
 * @since 1.0.0 (Commit 1 - Initial Project Setup with Agent-API Integration)
 * @see com.embabel.agent.domain.io.UserInput
 * @see com.embabel.metaagent.core.model.GeneratedAgentModel
 * @see com.embabel.metaagent.core.agent.MetaAgent.design
 */
data class AgentSpecification(
    
    /**
     * Generated agent class name.
     * 
     * Examples: "RestaurantBookingAgent", "WeatherAgent", "TravelPlannerAgent"
     */
    @field:NotBlank(message = "Agent name is required")
    @field:Size(min = 3, max = 50, message = "Agent name must be between 3 and 50 characters")
    val name: String,
    
    /**
     * Target domain for agent specialization.
     * 
     * Examples: "restaurant-booking", "weather", "travel-planning"
     */
    @field:NotBlank(message = "Domain is required for agent specialization")
    @field:Size(min = 2, max = 50, message = "Domain must be between 2 and 50 characters")
    val domain: String,
    
    /**
     * Detailed specification of agent purpose and capabilities.
     * 
     * More comprehensive than a simple description - includes specific
     * capabilities, constraints, and operational requirements.
     */
    @field:NotBlank(message = "Specification is required for agent documentation")
    @field:Size(min = 20, max = 1000, message = "Specification must be between 20 and 1000 characters")
    val specification: String,
    
    /**
     * High-level action intentions before LLM finalization.
     * 
     * Examples: ["search restaurants", "make reservations", "handle cancellations"]
     * These get converted to concrete method names by LLM processing.
     */
    @field:Size(min = 1, max = 10, message = "Must have between 1 and 10 action intents")
    val actionIntents: List<String>,
    
    /**
     * Concrete action method names after LLM finalization.
     * 
     * Examples: ["searchRestaurants", "makeReservation", "cancelBooking"]
     * These are the actual method names that will be generated.
     */
    val finalizedActions: List<String> = emptyList(),
    
    /**
     * High-level goal intentions before LLM finalization.
     * 
     * Examples: ["find suitable restaurant", "complete booking successfully"]
     * These get converted to GOAP world states by LLM processing.
     */
    @field:Size(max = 5, message = "Maximum 5 goal intents allowed")
    val goalIntents: List<String> = emptyList(),
    
    /**
     * GOAP world states after LLM finalization.
     * 
     * Examples: ["restaurant_found", "reservation_confirmed", "booking_completed"]
     * These are specific, measurable conditions for GOAP planning.
     */
    val finalizedGoals: List<String> = emptyList(),
    
    /**
     * Use cases and example scenarios.
     * 
     * Examples: ["Book dinner for 4 people on Friday", "Find Italian restaurants in downtown"]
     * Used to guide LLM understanding and action/goal generation.
     */
    @field:Size(max = 10, message = "Maximum 10 examples allowed")
    val examples: List<String> = emptyList(),
    
    /**
     * Whether user has confirmed this specification.
     * 
     * Used in iterative refinement process to track approval status.
     */
    val userConfirmed: Boolean = false,
    
    /**
     * Timestamp when this specification was created.
     */
    val specificationTimestamp: Instant = Instant.now()
) {
    
    /**
     * Generate a comprehensive summary for logging and display.
     */
    fun summary(): String = "AgentSpecification(name='$name', domain='$domain', " +
                          "actionIntents=${actionIntents.size}, goalIntents=${goalIntents.size}, " +
                          "confirmed=$userConfirmed)"
    
    /**
     * Check if specification is ready for LLM finalization.
     */
    fun isReadyForFinalization(): Boolean = 
        name.isNotBlank() && 
        domain.isNotBlank() && 
        specification.isNotBlank() && 
        actionIntents.isNotEmpty()
    
    /**
     * Check if specification has been finalized by LLM.
     */
    fun isFinalized(): Boolean = 
        finalizedActions.isNotEmpty() && 
        finalizedGoals.isNotEmpty()
    
    /**
     * Check if specification is complete and ready for generation.
     */
    fun isReadyForGeneration(): Boolean = 
        isFinalized() && userConfirmed
    
    /**
     * Create structured prompt for LLM finalization.
     */
    fun buildFinalizationPrompt(): String = """
        Domain: $domain
        Specification: $specification
        Action Intents: ${actionIntents.joinToString(", ")}
        Goal Intents: ${goalIntents.joinToString(", ")}
        Examples: ${examples.joinToString(", ")}
        
        Please finalize this agent specification by:
        1. Converting action intents to camelCase method names
        2. Converting goal intents to snake_case GOAP world states
        3. Ensuring actions and goals are concrete and implementable
        
        Return structured data with finalizedActions and finalizedGoals arrays.
    """.trimIndent()
    
    /**
     * Generate suggested package name for the agent.
     */
    fun suggestedPackageName(): String {
        val cleanDomain = domain.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
        return "com.embabel.generated.$cleanDomain"
    }
    
    /**
     * Create a copy with finalized actions and goals.
     */
    fun withFinalization(
        finalizedActions: List<String>, 
        finalizedGoals: List<String>
    ): AgentSpecification = copy(
        finalizedActions = finalizedActions,
        finalizedGoals = finalizedGoals
    )
    
    /**
     * Create a copy with user confirmation.
     */
    fun withConfirmation(confirmed: Boolean = true): AgentSpecification = copy(
        userConfirmed = confirmed
    )
}