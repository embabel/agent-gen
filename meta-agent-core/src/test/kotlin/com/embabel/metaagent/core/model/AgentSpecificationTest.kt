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

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import jakarta.validation.Validation
import jakarta.validation.Validator
import java.time.Instant

/**
 * Unit tests for AgentSpecification data class
 * 
 * Tests the core AgentSpecification model including:
 * - Iterative specification gathering validation
 * - Jakarta validation constraints
 * - Utility methods (summary, isReadyForFinalization, etc.)
 * - Package name generation
 * - LLM finalization process
 * 
 * @since 1.0.0 (Commit 1 - Initial Project Setup with Agent-API Integration)
 */
class AgentSpecificationTest {
    
    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator
    
    // ========================================
    // Basic Construction and Validation Tests
    // ========================================
    
    @Test
    fun `should create valid AgentSpecification with required fields`() {
        // Given: Valid AgentSpecification parameters
        val agentSpec = AgentSpecification(
            name = "RestaurantBookingAgent",
            domain = "restaurant-booking",
            specification = "Helps users find and book restaurant reservations",
            actionIntents = listOf("search restaurants", "make reservation", "cancel booking"),
            goalIntents = listOf("find suitable restaurant", "complete booking"),
            examples = listOf("Book dinner for 4 people", "Find Italian restaurants nearby")
        )
        
        // Then: All fields should be properly set
        assertThat(agentSpec.name).isEqualTo("RestaurantBookingAgent")
        assertThat(agentSpec.domain).isEqualTo("restaurant-booking")
        assertThat(agentSpec.specification).contains("restaurant reservations")
        assertThat(agentSpec.actionIntents).hasSize(3)
        assertThat(agentSpec.goalIntents).hasSize(2)
        assertThat(agentSpec.examples).hasSize(2)
        assertThat(agentSpec.specificationTimestamp).isBeforeOrEqualTo(Instant.now())
        assertThat(agentSpec.userConfirmed).isFalse
    }
    
    @Test
    fun `should create AgentSpecification with minimal required fields`() {
        // Given: Minimal AgentSpecification
        val agentSpec = AgentSpecification(
            name = "SimpleAgent",
            domain = "test",
            specification = "Simple test agent for basic functionality",
            actionIntents = listOf("perform action")
        )
        
        // Then: Optional fields should have defaults
        assertThat(agentSpec.goalIntents).isEmpty()
        assertThat(agentSpec.examples).isEmpty()
        assertThat(agentSpec.finalizedActions).isEmpty()
        assertThat(agentSpec.finalizedGoals).isEmpty()
        assertThat(agentSpec.userConfirmed).isFalse
        assertThat(agentSpec.specificationTimestamp).isNotNull
    }
    
    // ========================================
    // Jakarta Validation Tests
    // ========================================
    
    @Test
    fun `should pass validation with valid data`() {
        // Given: Valid AgentSpecification
        val agentSpec = AgentSpecification(
            name = "WeatherAgent",
            domain = "weather",
            specification = "Provides weather forecasting and alerts for users worldwide",
            actionIntents = listOf("get current weather", "get forecast")
        )
        
        // When: Validation is performed
        val violations = validator.validate(agentSpec)
        
        // Then: No validation errors
        assertThat(violations).isEmpty()
    }
    
    @Test
    fun `should fail validation with blank name`() {
        // Given: AgentSpecification with blank name
        val agentSpec = AgentSpecification(
            name = "",
            domain = "test",
            specification = "Test agent with blank name for validation testing",
            actionIntents = listOf("test")
        )
        
        // When: Validation is performed
        val violations = validator.validate(agentSpec)
        
        // Then: Should have validation error for name
        assertThat(violations).isNotEmpty
        assertThat(violations).anyMatch { it.propertyPath.toString() == "name" }
    }
    
    @Test
    fun `should fail validation with too short specification`() {
        // Given: AgentSpecification with too short specification
        val agentSpec = AgentSpecification(
            name = "TestAgent",
            domain = "test",
            specification = "Short", // Less than 20 characters
            actionIntents = listOf("test")
        )
        
        // When: Validation is performed
        val violations = validator.validate(agentSpec)
        
        // Then: Should have validation error for specification
        assertThat(violations).isNotEmpty
        assertThat(violations).anyMatch { 
            it.propertyPath.toString() == "specification" && it.message.contains("20")
        }
    }
    
    @Test
    fun `should fail validation with too many action intents`() {
        // Given: AgentSpecification with too many action intents
        val tooManyActions = (1..15).map { "action intent $it" } // More than 10
        val agentSpec = AgentSpecification(
            name = "OverloadedAgent",
            domain = "test",
            specification = "Agent with too many action intents for testing validation",
            actionIntents = tooManyActions
        )
        
        // When: Validation is performed
        val violations = validator.validate(agentSpec)
        
        // Then: Should have validation error for actionIntents
        assertThat(violations).isNotEmpty
        assertThat(violations).anyMatch { 
            it.propertyPath.toString() == "actionIntents" && it.message.contains("10")
        }
    }
    
    // ========================================
    // Utility Method Tests
    // ========================================
    
    @Test
    fun `summary should provide concise information`() {
        // Given: AgentSpecification with specific data
        val agentSpec = AgentSpecification(
            name = "TravelAgent",
            domain = "travel",
            specification = "Helps with travel planning and booking arrangements",
            actionIntents = listOf("search flights", "book hotels", "plan itinerary"),
            goalIntents = listOf("trip planned", "bookings confirmed"),
            userConfirmed = true
        )
        
        // When: Summary is generated
        val summary = agentSpec.summary()
        
        // Then: Summary should contain key information
        assertThat(summary).contains("TravelAgent")
        assertThat(summary).contains("travel")
        assertThat(summary).contains("actionIntents=3")
        assertThat(summary).contains("goalIntents=2")
        assertThat(summary).contains("confirmed=true")
    }
    
    @Test
    fun `suggestedPackageName should generate valid package names`() {
        // Given: Different domain formats
        val testCases = mapOf(
            "weather" to "com.embabel.generated.weather",
            "restaurant-booking" to "com.embabel.generated.restaurantbooking",
            "travel_planning" to "com.embabel.generated.travelplanning",
            "AI-Assistant" to "com.embabel.generated.aiassistant",
            "complex-domain_with#chars!" to "com.embabel.generated.complexdomainwithchars"
        )
        
        testCases.forEach { (domain, expectedPackage) ->
            // Given: AgentSpecification with specific domain
            val agentSpec = AgentSpecification(
                name = "TestAgent",
                domain = domain,
                specification = "Test agent for package name generation testing",
                actionIntents = listOf("test")
            )
            
            // When: Package name is generated
            val packageName = agentSpec.suggestedPackageName()
            
            // Then: Should generate valid package name
            assertThat(packageName).isEqualTo(expectedPackage)
            assertThat(packageName).matches("^[a-z0-9.]+$") // Valid Java package name
        }
    }
    
    // ========================================
    // Iterative Specification Process Tests
    // ========================================
    
    @Test
    fun `isReadyForFinalization should return true when all required fields are present`() {
        // Given: Complete specification ready for LLM finalization
        val agentSpec = AgentSpecification(
            name = "ReadyAgent",
            domain = "test",
            specification = "Agent ready for LLM finalization processing",
            actionIntents = listOf("perform primary action", "handle secondary task")
        )
        
        // When: Checking if ready for finalization
        val isReady = agentSpec.isReadyForFinalization()
        
        // Then: Should be ready
        assertThat(isReady).isTrue
    }
    
    @Test
    fun `isReadyForFinalization should return false when missing required fields`() {
        // Given: Incomplete specification
        val agentSpec = AgentSpecification(
            name = "IncompleteAgent",
            domain = "test",
            specification = "Incomplete agent for testing readiness",
            actionIntents = emptyList() // Missing action intents
        )
        
        // When: Checking if ready for finalization
        val isReady = agentSpec.isReadyForFinalization()
        
        // Then: Should not be ready
        assertThat(isReady).isFalse
    }
    
    @Test
    fun `isFinalized should return true when LLM has processed intents`() {
        // Given: Specification with finalized actions and goals
        val agentSpec = AgentSpecification(
            name = "FinalizedAgent",
            domain = "test",
            specification = "Agent with finalized actions and goals",
            actionIntents = listOf("handle requests"),
            finalizedActions = listOf("handleRequest", "processResponse"),
            finalizedGoals = listOf("request_processed", "response_sent")
        )
        
        // When: Checking if finalized
        val isFinalized = agentSpec.isFinalized()
        
        // Then: Should be finalized
        assertThat(isFinalized).isTrue
    }
    
    @Test
    fun `isReadyForGeneration should return true when finalized and confirmed`() {
        // Given: Complete and confirmed specification
        val agentSpec = AgentSpecification(
            name = "GenerationReadyAgent",
            domain = "test",
            specification = "Agent ready for code generation",
            actionIntents = listOf("handle requests"),
            finalizedActions = listOf("handleRequest"),
            finalizedGoals = listOf("request_handled"),
            userConfirmed = true
        )
        
        // When: Checking if ready for generation
        val isReady = agentSpec.isReadyForGeneration()
        
        // Then: Should be ready
        assertThat(isReady).isTrue
    }
    
    @Test
    fun `buildFinalizationPrompt should create structured LLM prompt`() {
        // Given: Specification ready for LLM finalization
        val agentSpec = AgentSpecification(
            name = "PromptTestAgent",
            domain = "test",
            specification = "Agent for testing prompt generation",
            actionIntents = listOf("process data", "send notifications"),
            goalIntents = listOf("complete processing", "notify users"),
            examples = listOf("Process user data", "Send email notification")
        )
        
        // When: Building finalization prompt
        val prompt = agentSpec.buildFinalizationPrompt()
        
        // Then: Prompt should contain all relevant information
        assertThat(prompt).contains("Domain: test")
        assertThat(prompt).contains("Specification: Agent for testing prompt generation")
        assertThat(prompt).contains("Action Intents: process data, send notifications")
        assertThat(prompt).contains("Goal Intents: complete processing, notify users")
        assertThat(prompt).contains("Examples: Process user data, Send email notification")
        assertThat(prompt).contains("camelCase method names")
        assertThat(prompt).contains("snake_case GOAP world states")
    }
    
    // ========================================
    // Finalization Process Tests
    // ========================================
    
    @Test
    fun `withFinalization should create copy with finalized data`() {
        // Given: Original specification
        val original = AgentSpecification(
            name = "OriginalAgent",
            domain = "test",
            specification = "Original specification for finalization testing",
            actionIntents = listOf("handle requests")
        )
        
        // When: Adding finalization data
        val finalized = original.withFinalization(
            finalizedActions = listOf("handleRequest", "processResponse"),
            finalizedGoals = listOf("request_handled", "response_sent")
        )
        
        // Then: Should have finalized data while preserving original
        assertThat(finalized.finalizedActions).containsExactly("handleRequest", "processResponse")
        assertThat(finalized.finalizedGoals).containsExactly("request_handled", "response_sent")
        assertThat(finalized.name).isEqualTo(original.name) // Original data preserved
        assertThat(finalized.specification).isEqualTo(original.specification)
        assertThat(original.finalizedActions).isEmpty() // Original unchanged
    }
    
    @Test
    fun `withConfirmation should create copy with confirmation status`() {
        // Given: Unconfirmed specification
        val original = AgentSpecification(
            name = "UnconfirmedAgent",
            domain = "test",
            specification = "Specification for confirmation testing",
            actionIntents = listOf("handle requests"),
            finalizedActions = listOf("handleRequest"),
            finalizedGoals = listOf("request_handled")
        )
        
        // When: Confirming the specification
        val confirmed = original.withConfirmation(true)
        
        // Then: Should be confirmed while preserving original
        assertThat(confirmed.userConfirmed).isTrue
        assertThat(confirmed.name).isEqualTo(original.name) // Original data preserved
        assertThat(original.userConfirmed).isFalse // Original unchanged
    }
    
    // ========================================
    // Thread Safety and Immutability Tests
    // ========================================
    
    @Test
    fun `AgentSpecification should be immutable`() {
        // Given: AgentSpecification with lists
        val originalActionIntents = listOf("action1", "action2")
        val originalGoalIntents = listOf("goal1", "goal2")
        val originalExamples = listOf("example1", "example2")
        
        val agentSpec = AgentSpecification(
            name = "ImmutableAgent",
            domain = "test",
            specification = "Testing immutability of agent specification",
            actionIntents = originalActionIntents,
            goalIntents = originalGoalIntents,
            examples = originalExamples
        )
        
        // When: Attempting to verify immutability
        // Note: Lists are immutable by design, but this tests the pattern
        
        // Then: AgentSpecification data should remain unchanged
        assertThat(agentSpec.actionIntents).containsExactlyElementsOf(originalActionIntents)
        assertThat(agentSpec.goalIntents).containsExactlyElementsOf(originalGoalIntents)
        assertThat(agentSpec.examples).containsExactlyElementsOf(originalExamples)
    }
    
    @Test
    fun `should handle concurrent access safely`() {
        // Given: AgentSpecification instance
        val agentSpec = AgentSpecification(
            name = "ConcurrentAgent",
            domain = "test",
            specification = "Testing thread safety of agent specification",
            actionIntents = listOf("action1", "action2", "action3"),
            goalIntents = listOf("goal1", "goal2")
        )
        
        // When: Multiple threads access the same instance
        val results = (1..10).toList().parallelStream().map {
            agentSpec.summary() + agentSpec.suggestedPackageName() + agentSpec.buildFinalizationPrompt()
        }.toList()
        
        // Then: All results should be identical (thread-safe immutable access)
        assertThat(results).allMatch { it == results[0] }
    }
}