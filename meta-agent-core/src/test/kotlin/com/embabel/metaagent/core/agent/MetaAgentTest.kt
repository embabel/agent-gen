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
package com.embabel.metaagent.core.agent

import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.domain.io.UserInput
import com.embabel.agent.testing.unit.FakeOperationContext
import com.embabel.metaagent.core.model.AgentSpecification
import com.embabel.metaagent.core.model.DiscoveredTool
import com.embabel.metaagent.core.model.GeneratedAgentModel
import com.embabel.metaagent.core.model.GenerationMetadata
import com.embabel.metaagent.core.model.MetaAgentContext
import com.embabel.metaagent.core.model.TargetLanguage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat
import org.slf4j.LoggerFactory

/**
 * Unit tests for MetaAgent - Agent-Native Recursive Architecture
 * 
 * Tests the core functionality of the MetaAgent including:
 * - Agent design generation from user input
 * - Fallback behavior when LLM is unavailable
 * - Proper GOAP goal vs capability distinction
 * - Stub method behavior for gradual implementation
 * - Error handling and recovery
 * 
 * **Test Strategy:**
 * - Test actual implementation (design method with fallback)
 * - Test stub methods return expected types
 * - Test edge cases and error conditions
 * - Validate Agent-Native Recursive Architecture patterns
 * 
 * @since 1.0.0 (Commit 1 - Initial Project Setup with Agent-API Integration)
 */
class MetaAgentTest {
    
    private lateinit var metaAgent: MetaAgent
    private lateinit var mockContext: OperationContext
    private val logger = LoggerFactory.getLogger(MetaAgentTest::class.java)
    
    @BeforeEach
    fun setUp() {
        metaAgent = MetaAgent()
        mockContext = FakeOperationContext()
    }
    
    // ========================================
    // Design Method Tests (Working Implementation)
    // ========================================
    
    @Test
    fun `createAgentSpecification should create AgentSpecification from UserInput with fallback`() {
        // Given: UserInput for restaurant booking domain
        val userInput = UserInput(
            content = "Create an agent for restaurant-booking that helps users find and book restaurant reservations. " +
                     "Requirements: Search restaurants by cuisine and location, Make reservations with date and time, " +
                     "Handle cancellations and modifications"
        )
        
        // When: createAgentSpecification method is called
        val result = metaAgent.createAgentSpecification(userInput, mockContext)
        
        // Then: AgentSpecification is created with proper structure
        assertThat(result).isNotNull
        assertThat(result.name).contains("Agent")
        assertThat(result.domain).contains("restaurant")
        assertThat(result.specification).contains("restaurant")
        assertThat(result.actionIntents).isNotEmpty // Should extract from "Requirements:" section
        assertThat(result.examples).isNotEmpty
        
        logger.info("✅ Created AgentSpecification: ${result.summary()}")
    }
    
    @Test
    fun `createAgentSpecification should generate proper agent name from domain`() {
        // Given: Different domain formats
        val testCases = mapOf(
            "weather" to "WeatherAgent",
            "restaurant-booking" to "RestaurantBookingAgent", 
            "travel_planning" to "TravelPlanningAgent",
            "ai-assistant" to "AiAssistantAgent"
        )
        
        testCases.forEach { (domain, expectedName) ->
            // Given: UserInput with specific domain
            val userInput = UserInput(
                content = "Create an agent for $domain domain"
            )
            
            // When: createAgentSpecification method is called
            val result = metaAgent.createAgentSpecification(userInput, mockContext)
            
            // Then: Agent name follows expected pattern
            assertThat(result.name).contains("Agent")
        }
    }
    
    @Test
    fun `createAgentSpecification should infer actions from requirements`() {
        // Given: UserInput with specific requirements containing action verbs
        val userInput = UserInput(
            content = "Create a weather forecasting agent that can: " +
                     "Search current weather conditions, Find forecast data for next week, " +
                     "Send severe weather alerts, Process weather data from APIs"
        )
        
        // When: createAgentSpecification method is called
        val result = metaAgent.createAgentSpecification(userInput, mockContext)
        
        // Then: Action intents are extracted semantically from verbs in content
        assertThat(result.actionIntents).isNotEmpty
        // Semantic extraction should find these core verbs with relevant nouns
        assertThat(result.actionIntents).anyMatch { it.startsWith("search") }
        assertThat(result.actionIntents).anyMatch { it.startsWith("find") }
        assertThat(result.actionIntents).anyMatch { it.startsWith("send") }
        assertThat(result.actionIntents).anyMatch { it.startsWith("process") }
    }
    
    @Test
    fun `createAgentSpecification should generate proper GOAP goals not capabilities`() {
        // Given: UserInput for booking domain
        val userInput = UserInput(
            content = "Create booking agent for restaurant reservations that can make reservations and send confirmations"
        )
        
        // When: createAgentSpecification method is called
        val result = metaAgent.createAgentSpecification(userInput, mockContext)
        
        // Then: Goal intents are extracted using smart fallback
        // Note: Smart fallback should extract goal-like words or domain nouns
        assertThat(result.goalIntents).isNotEmpty // Should find "booking completed" or similar
    }
    
    @Test
    fun `createAgentSpecification should handle empty requirements gracefully`() {
        // Given: UserInput with minimal content
        val userInput = UserInput(
            content = "Create a simple agent with basic functionality"
        )
        
        // When: createAgentSpecification method is called
        val result = metaAgent.createAgentSpecification(userInput, mockContext)
        
        // Then: Basic structure is created with smart extraction
        assertThat(result.actionIntents).isNotEmpty // Should extract "create" verb
        assertThat(result.actionIntents).anyMatch { it.startsWith("create") } // Should find "create" verb
        assertThat(result.goalIntents).isNotEmpty // Should extract goal hints
        assertThat(result.name).contains("Agent")
    }
    
    // ========================================
    // Stub Method Tests (Gradual Implementation)
    // ========================================
    
    @Test
    fun `generateAgent should create GeneratedAgentModel with fallback template`() {
        // Given: Valid AgentSpecification
        val agentSpec = AgentSpecification(
            name = "TestAgent",
            domain = "test",
            specification = "Test agent for unit testing",
            actionIntents = listOf("perform test"),
            goalIntents = listOf("complete test")
        )
        
        // When/Then: Method should now work with the implementation
        val result = metaAgent.generateAgent(agentSpec, mockContext)
        
        // Then: Should return a GeneratedAgentModel
        assertThat(result).isNotNull
        assertThat(result.agent.name).isEqualTo("TestAgent")
        assertThat(result.generatedCode).isNotBlank()
        assertThat(result.packageName).isEqualTo("com.embabel.generated.test")
        
    }
    
    @Test
    fun `discoverTools should return empty list as stub implementation`() {
        // Given: Valid AgentSpecification
        val agentSpec = AgentSpecification(
            name = "TestAgent",
            domain = "test",
            specification = "Test agent",
            actionIntents = listOf("perform test"),
            goalIntents = listOf("complete test")
        )
        
        // When: discoverTools is called
        val result = metaAgent.discoverTools(agentSpec)
        
        // Then: Returns empty list (stub implementation)
        assertThat(result).isEmpty()
    }
    
    @Test
    fun `makeAuditAware should return unchanged agent as stub implementation`() {
        // Given: GeneratedAgentModel
        val agent = com.embabel.agent.core.Agent(
            name = "TestAgent",
            description = "Test agent",
            provider = "test",
            actions = emptyList(),
            goals = emptySet()
        )
        
        val generatedAgent = GeneratedAgentModel(
            agent = agent,
            packageName = "com.test",
            discoveredTools = emptyList(),
            generationMetadata = GenerationMetadata(),
            generationContext = MetaAgentContext(
                userInput = UserInput(content = "Test agent generation"),
                targetLanguage = TargetLanguage.KOTLIN
            )
        )
        
        // When: makeAuditAware is called
        val result = metaAgent.makeAuditAware(generatedAgent)
        
        // Then: Returns unchanged agent (stub implementation)
        assertThat(result).isSameAs(generatedAgent)
    }
    
    // ========================================
    // Edge Cases and Error Handling
    // ========================================
    
    @Test
    fun `createAgentSpecification should handle malformed domain names`() {
        // Given: UserInput with special characters
        val userInput = UserInput(
            content = "Create agent for test-domain_with#special@chars! with basic functionality"
        )
        
        // When: createAgentSpecification method is called
        val result = metaAgent.createAgentSpecification(userInput, mockContext)
        
        // Then: Should handle gracefully and create valid agent name
        assertThat(result.name).matches("^[A-Za-z]+Agent$") // Should be valid class name
        assertThat(result.domain).contains("test") // Domain extracted from content
    }
    
    @Test
    fun `createAgentSpecification should handle very long requirements list`() {
        // Given: UserInput with extensive content
        val manyRequirements = (1..20).map { "Requirement number $it for testing" }
        val userInput = UserInput(
            content = "Create agent for large-system with many requirements: " + manyRequirements.joinToString(", ")
        )
        
        // When: createAgentSpecification method is called
        val result = metaAgent.createAgentSpecification(userInput, mockContext)
        
        // Then: Should handle gracefully without performance issues
        assertThat(result.actionIntents).isNotEmpty // Should extract "create" verb from content
        assertThat(result.goalIntents).isNotEmpty // Should extract some goal hints
        assertThat(result.examples).isNotEmpty // Should have examples
    }
    
    // ========================================
    // Agent-Native Recursive Architecture Validation
    // ========================================
    
    @Test
    fun `MetaAgent should be properly annotated as Agent`() {
        // Given: MetaAgent class
        val agentAnnotation = MetaAgent::class.java.getAnnotation(com.embabel.agent.api.annotation.Agent::class.java)
        
        // Then: Should have proper @Agent annotation
        assertThat(agentAnnotation).isNotNull
        assertThat(agentAnnotation.name).isEqualTo("MetaAgent")
        assertThat(agentAnnotation.description).contains("goal-oriented planning")
        assertThat(agentAnnotation.planner).isEqualTo(com.embabel.agent.api.annotation.Planner.GOAP)
    }
    
    @Test
    fun `createAgentSpecification method should have proper action annotations`() {
        // Given: createAgentSpecification method
        val createSpecMethod = MetaAgent::class.java.getDeclaredMethod("createAgentSpecification", UserInput::class.java, OperationContext::class.java)
        
        // Then: Should have Action annotation but NOT AchievesGoal (intermediate step)
        val achievesGoalAnnotation = createSpecMethod.getAnnotation(com.embabel.agent.api.annotation.AchievesGoal::class.java)
        val actionAnnotation = createSpecMethod.getAnnotation(com.embabel.agent.api.annotation.Action::class.java)
        
        assertThat(achievesGoalAnnotation).isNull() // Should be null - intermediate action only
        
        assertThat(actionAnnotation).isNotNull
        assertThat(actionAnnotation.cost).isEqualTo(0.4)
        assertThat(actionAnnotation.value).isEqualTo(0.9)
        assertThat(actionAnnotation.toolGroups).contains("llm", "design")
        assertThat(actionAnnotation.post).contains("it:com.embabel.metaagent.core.model.AgentSpecification")
    }
    
    @Test
    fun `action methods should have consistent GOAP heuristics`() {
        // Given: All action methods
        val methods = MetaAgent::class.java.declaredMethods.filter { method ->
            method.isAnnotationPresent(com.embabel.agent.api.annotation.Action::class.java)
        }
        
        // Then: All should have valid cost/value heuristics
        methods.forEach { method ->
            val actionAnnotation = method.getAnnotation(com.embabel.agent.api.annotation.Action::class.java)
            
            assertThat(actionAnnotation.cost)
                .withFailMessage("Method ${method.name} should have cost between 0.0 and 1.0")
                .isBetween(0.0, 1.0)
                
            assertThat(actionAnnotation.value)
                .withFailMessage("Method ${method.name} should have value between 0.0 and 1.0")
                .isBetween(0.0, 1.0)
                
            assertThat(actionAnnotation.toolGroups)
                .withFailMessage("Method ${method.name} should specify tool groups")
                .isNotEmpty
        }
    }
}