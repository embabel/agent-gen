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

import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.annotation.ToolGroup
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.common.PlannerType
import com.embabel.agent.api.common.createObject
import com.embabel.agent.domain.io.UserInput
import com.embabel.common.ai.model.LlmOptions
import com.embabel.common.ai.model.ModelSelectionCriteria.Companion.Auto
import com.embabel.metaagent.core.model.*
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Data class for LLM-extracted agent specification components.
 * 
 * This structure defines the expected output from LLM when extracting
 * specification components from free text user input.
 */
@JsonClassDescription("Agent specification components extracted by LLM from user input")
data class LLMAgentSpecification(
    @JsonPropertyDescription("Agent class name (e.g., 'RestaurantBookingAgent')")
    val name: String,
    
    @JsonPropertyDescription("Target domain (e.g., 'restaurant-booking')")
    val domain: String,
    
    @JsonPropertyDescription("Detailed specification of what this agent accomplishes")
    val specification: String,
    
    @JsonPropertyDescription("High-level action intentions (e.g., 'search restaurants', 'make reservations')")
    val actionIntents: List<String>,
    
    @JsonPropertyDescription("High-level goal intentions (e.g., 'find suitable restaurant', 'complete booking')")
    val goalIntents: List<String>,
    
    @JsonPropertyDescription("Examples or use cases mentioned by user")
    val examples: List<String>
)

/**
 * MetaAgent - Agent-Native Recursive Architecture Implementation
 * 
 * This is the core innovation of the Meta-Agent Framework: the meta-agent itself 
 * IS a real agent annotated with @Agent, using Goal Oriented Action Planning (GOAP) 
 * to iteratively achieve goals like "design", "discoverTools", "generateAgent", and "makeAuditAware".
 * 
 * ## Core Innovation: Agent-Native Recursive Architecture
 * 
 * Unlike traditional code generators that are services, this meta-agent is a **first-class agent**
 * that uses the same patterns, annotations, and planning algorithms as the agents it generates.
 * This creates a self-consistent, recursive architecture where:
 * - Meta-agent IS an @Agent with @Action methods
 * - Uses GOAP planning for optimal goal sequencing  
 * - Leverages type-safe implicit conditions for action chaining
 * - Integrates seamlessly with Embabel ecosystem
 * 
 * ## Comprehensive MCP Tooling Support
 * 
 * The MetaAgent leverages Embabel's sophisticated MCP (Model Context Protocol) tooling 
 * infrastructure for intelligent tool discovery and integration:
 * 
 * ### Available Tool Categories:
 * - **`llm`** - Large Language Model integration tools for intelligent analysis
 * - **`design`** - Agent architecture design and planning tools
 * - **`codegen`** - Source code generation and template processing tools
 * - **`templates`** - Code template engines and processing frameworks
 * - **`web`** - Web-based API integration and HTTP client tools
 * - **`apis`** - REST/GraphQL/SOAP API discovery and integration tools
 * - **`rag`** - Retrieval-Augmented Generation and knowledge graph tools
 * - **`containers`** - Docker Hub containerized tool discovery and integration
 * - **`files`** - File system operations and document processing tools
 * - **`databases`** - Database connectivity and query execution tools
 * - **`monitoring`** - Metrics collection and observability tools
 * 
 * ### Multi-Source Tool Discovery Strategy:
 * 1. **RAG + Vector Similarity** - Semantic tool matching using embeddings
 * 2. **Knowledge Graph Traversal** - Relationship-based tool discovery
 * 3. **LLM Semantic Discovery** - Intelligent tool suggestion based on context
 * 4. **Docker Hub Integration** - Containerized tool and service discovery
 * 5. **API Registry Crawling** - Traditional API directory integration
 * 6. **MCP Server Discovery** - Direct MCP tool server integration
 * 
 * ### Tool Integration Architecture:
 * ```kotlin
 * // Example: Multi-tool action with automatic MCP client integration
 * @Action(description = "Discover tools for agent")
 * @ToolGroup("llm, apis, rag")
 * fun discoverTools(design: AgentDesign): DiscoveredTools {
 *     // Automatically resolves and invokes MCP tools from specified groups
 *     // Combines semantic search, API introspection, and LLM reasoning
 *     // Results stored in Blackboard for subsequent action chaining
 * }
 * ```
 * 
 * ## GOAP Planning Benefits:
 * 1. **Cost-Optimized Tool Selection**: Balances tool discovery cost vs. value
 * 2. **Intelligent Action Sequencing**: Optimal goal achievement planning
 * 3. **Automatic Error Recovery**: Failed actions trigger alternative strategies
 * 4. **Type-Safe Chaining**: Implicit conditions enable seamless workflows
 * 5. **Resource Management**: Efficient tool usage based on availability and cost
 * 
 * ## Architecture Benefits:
 * 1. **Native GOAP Planning**: Uses same planning algorithm as generated agents
 * 2. **Type-Safe Chaining**: Automatic workflow orchestration via implicit conditions
 * 3. **Blackboard Collaboration**: Shared workspace with persistent state
 * 4. **Shell Integration**: Natural command-line workflow through extended shell commands
 * 5. **Error Recovery**: Failed goals trigger recovery planning through GOAP
 * 6. **MCP Tool Integration**: Seamless access to Embabel's comprehensive tooling ecosystem
 * 
 * ## Rationale for Agent-as-Agent Design:
 * 
 * **Traditional Approach (Service-Based):**
 * ```
 * User → REST API → Service → Database → Generated Code
 * ```
 * **Problems:** Disconnected from agent ecosystem, manual orchestration, no planning
 * 
 * **Agent-Native Recursive Approach:**
 * ```
 * User → Shell → GOAP → MetaAgent@Actions → Blackboard → Generated Agent
 * ```
 * **Benefits:** 
 * - Consistent architecture patterns
 * - Intelligent planning and optimization
 * - Seamless ecosystem integration
 * - Self-improving through audit feedback
 * - Type-safe workflow composition
 * 
 * This design enables the meta-agent to evolve and improve using the same mechanisms
 * as the agents it generates, creating a self-consistent, recursive architecture that
 * leverages the full power of the Embabel agent platform.
 * 
 * @author Meta-Agent Framework
 * @since 1.0.0
 * @see com.embabel.agent.api.annotation.Agent
 * @see com.embabel.agent.api.annotation.Action
 * @see com.embabel.metaagent.core.model.MetaAgentContext
 */
@Agent(
    name = "MetaAgent",
    description = "Generates other agents through goal-oriented planning using LLM intelligence and embabel-agent-api integration",
    planner = PlannerType.GOAP
)
@Component
class MetaAgent {
    
    private val logger = LoggerFactory.getLogger(MetaAgent::class.java)
    
    /**
     * Design Goal: Transform user requirements into structured agent design
     * 
     * **IMPLEMENTATION STATUS**: 🟡 STUB - Gradual implementation in progress
     * 
     * @param userInput The natural language requirements from the user
     * @return AgentSpecification Structured specification for the agent to be generated
     * 
     * @since 1.0.0 (Commit 2 - Meta-Agent as @Agent Implementation)
     */
    @Action(
        description = "Process user input and create comprehensive agent specification",
        cost = 0.4,
        value = 0.9,
        post = ["it:com.embabel.metaagent.core.model.AgentSpecification"]
    )
    @ToolGroup ("llm, design")
    fun createAgentSpecification(userInput: UserInput, context: OperationContext): AgentSpecification {
        logger.info("🎯 Starting agent specification from user input: ${userInput.content.take(50)}...")
        logger.debug("📝 Full user input: ${userInput.content}")
        
        try {
            // Dual Mode Processing: Free text OR Structured input
            val result = if (isStructuredInput(userInput)) {
                logger.info("🏗️ Processing as structured input")
                processStructuredInput(userInput, context)
            } else {
                logger.info("📝 Processing as free text input")
                processFreeTextInput(userInput, context)
            }
            
            // Log the created specification
            logger.info("✅ Successfully created AgentSpecification:")
            logger.info("   📋 Name: ${result.name}")
            logger.info("   🎯 Domain: ${result.domain}")
            logger.info("   📄 Specification: ${result.specification.take(100)}...")
            logger.info("   ⚡ Action Intents: ${result.actionIntents}")
            logger.info("   🎯 Goal Intents: ${result.goalIntents}")
            logger.info("   📚 Examples: ${result.examples}")
            
            return result
            
        } catch (e: Exception) {
            // Log without stack trace to avoid noise in tests
            // The CreateObjectPromptException is expected when LLM service is unavailable
            logger.warn("⚠️ LLM integration failed, using fallback specification: {}", e.message)
            logger.debug("🔄 Creating fallback specification...")
            
            val fallbackResult = createFallbackSpecification(userInput)
            
            // Log fallback result
            logger.info("✅ Created fallback AgentSpecification:")
            logger.info("   📋 Name: ${fallbackResult.name}")
            logger.info("   🎯 Domain: ${fallbackResult.domain}")
            logger.info("   ⚡ Action Intents: ${fallbackResult.actionIntents}")
            logger.info("   🎯 Goal Intents: ${fallbackResult.goalIntents}")
            
            return fallbackResult
        }
    }
    
    /**
     * Process free text user input using LLM extraction.
     * 
     * Analyzes natural language content to extract domain, specification,
     * action intents, and goal intents for agent specification.
     */
    private fun processFreeTextInput(userInput: UserInput, context: OperationContext): AgentSpecification {
        logger.info("📝 Processing free text input")
        
        return try {
            logger.debug("🤖 Calling LLM for specification extraction...")
            val extractedSpec = context.promptRunner().withLlm(LlmOptions(criteria = Auto)).createObject<LLMAgentSpecification>(
                """
                Analyze this user input and extract agent specification components:
                
                User Input: ${userInput.content}
                
                Extract and structure the following:
                1. Domain: What domain/area should this agent work in?
                2. Specification: What should this agent accomplish? (detailed description)
                3. Action Intents: What high-level actions should it perform?
                4. Goal Intents: What high-level goals should it achieve?
                5. Examples: Any examples or use cases mentioned?
                
                Focus on extracting intent and structure from natural language.
                Convert to structured specification components.
                """.trimIndent()
            )
            
            logger.info("🤖 LLM successfully extracted specification:")
            logger.debug("   📋 LLM Name: ${extractedSpec.name}")
            logger.debug("   🎯 LLM Domain: ${extractedSpec.domain}")
            logger.debug("   ⚡ LLM Action Intents: ${extractedSpec.actionIntents}")
            logger.debug("   🎯 LLM Goal Intents: ${extractedSpec.goalIntents}")
            
            convertToAgentSpecification(extractedSpec)
        } catch (e: Exception) {
            logger.warn("🔄 Failed to extract specification via LLM, using fallback: ${e.message}")
            createFallbackSpecification(userInput)
        }
    }
    
    /**
     * Process structured input when user provides templated data.
     * 
     * For future implementation when users provide structured input
     * through iterative prompting (domain?, specification?, etc.)
     */
    private fun processStructuredInput(userInput: UserInput, context: OperationContext): AgentSpecification {
        logger.info("🏗️ Processing structured input")
        
        // TODO: Implement structured input parsing
        // This will handle cases where user provides domain, spec, actions separately
        
        // For now, fallback to free text processing
        return processFreeTextInput(userInput, context)
    }
    
    /**
     * Detect if user input contains structured components.
     */
    private fun isStructuredInput(userInput: UserInput): Boolean {
        // Simple heuristic - check for structured markers
        val content = userInput.content.lowercase()
        return content.contains("domain:") || 
               content.contains("specification:") || 
               content.contains("actions:") ||
               content.contains("goals:")
    }
    
    /**
     * Convert LLM-extracted specification to AgentSpecification model.
     */
    private fun convertToAgentSpecification(llmSpec: LLMAgentSpecification): AgentSpecification {
        return AgentSpecification(
            name = llmSpec.name,
            domain = llmSpec.domain,
            specification = llmSpec.specification,
            actionIntents = llmSpec.actionIntents,
            goalIntents = llmSpec.goalIntents,
            examples = llmSpec.examples
        )
    }
    
    /**
     * Create fallback specification when LLM integration is not available.
     * 
     * FUTURE LLM ENHANCEMENT: This entire method will be replaced with intelligent LLM analysis.
     * 
     * Current: Simple text parsing with hardcoded rules
     * Future:  using(LlmOptions(Auto)).createObjectIfPossible<AgentSpecification>(prompt)
     * 
     * Example LLM prompt for meta-level analysis:
     * """
     * Analyze this user request and extract META-LEVEL information for agent generation:
     * 
     * User Input: "${userInput.content}"
     * 
     * Extract:
     * 1. Domain: What problem domain? (e.g., "restaurant-booking", "weather-services", "travel-planning")
     * 2. Agent Name: What should the Java class be called? (e.g., "RestaurantBookingAgent")
     * 3. High-Level Actions: What broad categories of actions? (e.g., ["search restaurants", "make reservations"])
     * 4. High-Level Goals: What outcomes should the agent achieve? (e.g., ["find suitable options", "complete transaction"])
     * 
     * Note: Return high-level intents, NOT specific implementation details.
     * The actual domain code will be generated later.
     * """
     */
    private fun createFallbackSpecification(userInput: UserInput): AgentSpecification {
        // CURRENT FALLBACK: Basic text parsing (will be replaced by LLM)
        val content = userInput.content.lowercase()
        val words = content.split(" ", "-", "_").filter { it.length > 2 }
        
        // Extract domain keyword (first meaningful word that's not a common verb/article)
        val domain = words.firstOrNull { word ->
            word !in listOf("create", "agent", "for", "the", "and", "with", "that", "help", "make")
        } ?: "custom"
        
        val name = inferAgentName(userInput)
        
        // Parse user's action descriptions into high-level action intents
        val actionIntents = extractActionIntentsFromContent(userInput.content)
        
        return AgentSpecification(
            name = name,
            domain = domain,
            specification = userInput.content,
            actionIntents = actionIntents,
            // Smart fallback: use existing goal inference logic
            goalIntents = inferGoalsFromIntent(userInput.content),
            // Basic example extraction (LLM would find actual user examples)
            examples = listOf("Example: " + userInput.content.take(50))
        )
    }
    
    /**
     * Extract high-level action intents using semantic verb extraction.
     * 
     * INNOVATION: Language-agnostic semantic parsing instead of brittle pattern matching.
     * 
     * ## Semantic Verb Extraction Approach:
     * 
     * Step 1: Find action verbs anywhere in user content
     * Step 2: Pair each verb with nearby meaningful nouns
     * Step 3: Use domain-specific nouns when available
     * 
     * ## Works with ANY user phrasing:
     * ```
     * "Create an agent to: search, find, send"          → ["search data", "find data", "send data"]
     * "I need something that: processes, analyzes"      → ["process data", "analyze data"] 
     * "Build agent which: handles requests, sends"      → ["handle requests", "send data"]
     * "Agent should: validate, check, ensure quality"   → ["validate quality", "check quality", "ensure quality"]
     * ```
     * 
     * ## Domain Noun Intelligence:
     * - Finds domain-specific nouns: "weather", "restaurant", "booking", etc.
     * - Avoids generic system words: "agent", "system", "service"
     * - Uses actual words from user input (not hardcoded defaults)
     * 
     * ## Benefits:
     * 1. **Language Agnostic**: Works with any phrasing, not just "that can:"
     * 2. **Flexible**: Finds verbs anywhere in content
     * 3. **Context Aware**: Pairs verbs with nearby meaningful nouns
     * 4. **Domain Smart**: Uses actual domain words from user input
     * 5. **No Brittle Patterns**: No hardcoded regex dependencies
     * 
     * FUTURE LLM ENHANCEMENT: Will be enhanced with full semantic understanding.
     * 
     * Example LLM prompt:
     * """
     * Extract HIGH-LEVEL action intents from this user description:
     * 
     * Text: "${content}"
     * 
     * Return a list of 2-word action intents (verb + noun) that capture what the user wants the agent to DO.
     * Examples: ["search restaurants", "make reservations", "send notifications"]
     * 
     * Focus on USER INTENTS, not implementation details.
     * """
     */
    private fun extractActionIntentsFromContent(content: String): List<String> {
        // APPROACH: Semantic verb extraction instead of rigid pattern matching
        // Look for action verbs throughout the content and pair them with nearby nouns
        
        // Text preprocessing: normalize and filter to meaningful words
        val words = content.lowercase()              // Normalize case for consistent matching
            .split(Regex("\\s+"))                   // Split on any whitespace (spaces, tabs, newlines)
            .filter { it.length > 2 }               // Remove articles/prepositions ("a", "an", "to", "of", "in")
        val actionIntents = mutableListOf<String>()
        
        // Common action verbs users mention when describing what agents should do
        val actionVerbs = listOf(
            "search", "find", "get", "retrieve", "fetch", "collect", "gather",
            "send", "notify", "alert", "inform", "deliver", "transmit",
            "process", "handle", "manage", "execute", "perform", "run",
            "create", "make", "build", "generate", "produce", "construct",
            "update", "modify", "change", "edit", "save", "store",
            "analyze", "calculate", "compute", "determine", "evaluate",
            "validate", "check", "verify", "confirm", "ensure"
        )
        
        // Extract verb-noun pairs from anywhere in the content
        for (i in words.indices) {
            val word = words[i]
            if (word in actionVerbs) {
                // Look for meaningful noun within next 3 words
                val noun = words.drop(i + 1).take(3).firstOrNull { candidateNoun ->
                    candidateNoun.length > 3 && 
                    candidateNoun !in listOf("the", "and", "for", "with", "that", "this", "from", "into", "onto") &&
                    !candidateNoun.endsWith("ing") && // Skip gerunds like "booking"
                    !actionVerbs.contains(candidateNoun) // Skip other verbs
                } ?: findDomainNounFromContent(content)
                
                actionIntents.add("$word $noun")
            }
        }
        
        // Remove duplicates and limit
        return actionIntents.distinct().take(5)
    }
    
    /**
     * Extract a relevant domain noun from the content as fallback.
     */
    private fun findDomainNounFromContent(content: String): String {
        // Extract longer words (>4 chars) that are likely to be domain-specific nouns
        val words = content.lowercase()              // Normalize case
            .split(Regex("\\s+"))                   // Split on whitespace
            .filter { it.length > 4 }               // Keep substantial words, skip short articles/prepositions
        
        // Look for domain-relevant nouns (not generic system words)
        val domainNoun = words.firstOrNull { word ->
            word !in listOf("agent", "system", "service", "application", "create", "build", "help", "user")
        }
        
        return domainNoun ?: "data" // Ultimate fallback
    }
    
    
    
    
    
    
    
    /**
     * Generate Agent Goal: Create complete agent code with embabel-agent-api annotations
     * 
     * **IMPLEMENTATION STATUS**: ❌ NOT IMPLEMENTED YET - Planned for Commit 5
     * 
     * THIS IS WHERE DOMAIN-SPECIFIC CODE GETS GENERATED!
     * 
     * MetaAgent Role: Provide high-level specification like:
     * - actionIntents: ["search restaurants", "make reservations"] 
     * - domain: "restaurant-booking"
     * 
     * LLM Generation Role: Convert to actual domain code like:
     * ```kotlin
     * @Agent(name = "RestaurantBookingAgent")
     * class RestaurantBookingAgent {
     *     @Action @AchievesGoal(description = "restaurant_found")
     *     fun searchRestaurants(criteria: SearchCriteria): List<Restaurant>
     * 
     *     @Action @AchievesGoal(description = "reservation_confirmed") 
     *     fun makeReservation(restaurant: Restaurant, details: ReservationDetails): Booking
     * }
     * ```
     * 
     * Example LLM prompt:
     * """
     * Generate a complete Kotlin agent class with embabel-agent-api annotations:
     * 
     * Specification:
     * - Name: ${specification.name}
     * - Domain: ${specification.domain} 
     * - Action Intents: ${specification.actionIntents}
     * - Goal Intents: ${specification.goalIntents}
     * 
     * Generate:
     * 1. @Agent annotated class
     * 2. @Action methods for each action intent
     * 3. @AchievesGoal annotations with domain-specific goal states
     * 4. Proper method signatures with domain entities
     * 5. GOAP-compatible goal states (e.g., "restaurant_found", "booking_confirmed")
     * """
     * 
     * @param specification The structured agent specification from the design action
     * @return GeneratedAgentModel Complete agent implementation with metadata
     * 
     * @since 1.0.0 (Commit 5 - Generate Agent Goal)
     */
    @AchievesGoal(description = "Generate complete agent code with annotations")
    @Action(
        description = "Generate complete agent code with embabel-agent-api annotations and GOAP integration",
        cost = 0.5,
        value = 1.0,
        pre = ["it:com.embabel.metaagent.core.model.AgentSpecification"],
        post = ["it:com.embabel.metaagent.core.model.GeneratedAgentModel"]
    )
    @ToolGroup("codegen, emplates")
    fun generateAgent(specification: AgentSpecification, context: OperationContext): GeneratedAgentModel {
        logger.info("⚙️ Starting agent generation for: ${specification.name}")
        logger.debug("📋 Specification: ${specification.specification}")
        logger.debug("⚡ Action Intents: ${specification.actionIntents}")
        logger.debug("🎯 Goal Intents: ${specification.goalIntents}")
        
        try {
            // Generate the agent code using LLM
            logger.info("🤖 Generating Kotlin agent code with LLM...")
            val generatedCode = context.promptRunner().withLlm(LlmOptions(criteria = Auto)).generateText(
                """
                Generate a complete Kotlin agent class using the embabel-agent-api annotations based on this specification:
                
                Agent Name: ${specification.name}
                Domain: ${specification.domain}
                Description: ${specification.specification}
                Action Intents: ${specification.actionIntents.joinToString(", ")}
                Goal Intents: ${specification.goalIntents.joinToString(", ")}
                
                Requirements:
                1. Use package name: com.embabel.agent.{agent_name_based} (e.g., restaurantplanner, not restaurantbooking)
                2. Use @Agent(description = "...") annotation on the class (description parameter only)
                3. Create @Action methods for each action intent with optional cost parameter as Double
                4. Use @AchievesGoal(description = "...") annotations linking actions to goals
                5. Include proper method signatures with domain-appropriate parameters
                6. Use GOAP-compatible goal states (e.g., "resource_managed", "schedule_created")
                8. Follow Kotlin coding conventions
                9. Use exact import: com.embabel.agent.api.annotation.* (singular "annotation")
                
                Return raw Kotlin source code only (no markdown, no code blocks, no explanations):
                """.trimIndent()
            )
            
            logger.info("🎯 Generon(specificated agent code (${generatedCode.length} characters)")
            logger.debug("📝 Generated code preview: ${generatedCode.take(200)}...")
            
            // Extract package name from LLM-generated code
            val packageName = extractPackageNameFromCode(generatedCode) ?: generatePackageName(specification)
            val agent = createAgentFromSpecification(specification)
            val generationMetadata = GenerationMetadata(
                generatedAt = java.time.Instant.now(),
                generatedBy = "MetaAgent",
                llmModel = "Auto",
                codeSize = generatedCode.length
            )
            
            val result = GeneratedAgentModel(
                agent = agent,
                packageName = packageName,
                discoveredTools = emptyList(), // TODO: Implement in Milestone 2
                generationMetadata = generationMetadata,
                generationContext = MetaAgentContext(
                    userInput = com.embabel.agent.domain.io.UserInput(specification.specification),
                    targetLanguage = TargetLanguage.KOTLIN
                ),
                generatedCode = generatedCode // Store the actual generated code
            )
            
            logger.info("✅ Successfully generated agent: ${specification.name}")
            logger.info("   📦 Package: ${packageName}")
            logger.info("   📝 Code size: ${generatedCode.length} characters")
            logger.info("   ⚙️ Generated by: MetaAgent with LLM")
            
            // Write agent to filesystem alongside console output
            val writtenFile = writeAgentToFile(result)
            if (writtenFile != null) {
                logger.info("📁 Agent also written to file: $writtenFile")
            }
            
            return result
            
        } catch (e: Exception) {
            logger.warn("🔄 LLM code generation failed, using template fallback: ${e.message}")
            return createFallbackAgent(specification, context)
        }
    }
    
    /**
     * Create fallback agent when LLM generation fails.
     * 
     * Generates a basic agent template using the specification details
     * without requiring LLM integration.
     */
    private fun createFallbackAgent(specification: AgentSpecification, context: OperationContext): GeneratedAgentModel {
        logger.info("🔄 Creating fallback agent code using template approach")
        
        // Generate basic agent template
        val className = specification.name.replace(" ", "")
        val packageName = generatePackageName(specification)
        
        val templateCode = """
package $packageName

import com.embabel.agent.api.annotation.*
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.domain.io.UserInput

/**
 * ${specification.name} - Generated by MetaAgent
 * 
 * ${specification.specification}
 */
@Agent(description = "${specification.specification}")
class $className {

    ${generateFallbackActions(specification)}
}
        """.trimIndent()
        
        val agent = createAgentFromSpecification(specification)
        val generationMetadata = GenerationMetadata(
            generatedAt = java.time.Instant.now(),
            generatedBy = "MetaAgent-Fallback",
            llmModel = "Template",
            codeSize = templateCode.length
        )
        
        logger.info("✅ Created agent: ${specification.name}")
        logger.info("   📝 Template code size: ${templateCode.length} characters")
        
        val result = GeneratedAgentModel(
            agent = agent,
            packageName = packageName,
            discoveredTools = emptyList(),
            generationMetadata = generationMetadata,
            generationContext = MetaAgentContext(
                userInput = com.embabel.agent.domain.io.UserInput(specification.specification),
                targetLanguage = TargetLanguage.KOTLIN
            ),
            generatedCode = templateCode
        )
        
        // Write agent to filesystem alongside console output
        val writtenFile = writeAgentToFile(result)
        if (writtenFile != null) {
            logger.info("📁 Agent also written to file: $writtenFile")
        }
        
        return result
    }
    
    /**
     * Generate fallback action methods from action intents.
     */
    private fun generateFallbackActions(specification: AgentSpecification): String {
        return specification.actionIntents.mapIndexed { index, actionIntent ->
            val methodName = actionIntent.replace(" ", "").replaceFirstChar { it.lowercase() }
            val goalState = actionIntent.replace(" ", "_").lowercase() + "_completed"
            
            """
            @AchievesGoal(description = "$goalState")
            @Action(
                description = "$actionIntent",
                cost = 0.${5 + index},
                value = 0.${8 - (index * 2).coerceAtMost(6)},
            )
            @ToolGroup("default")
            fun $methodName(input: UserInput, context: OperationContext): String {
                // TODO: Implement $actionIntent logic
                return "Completed: $actionIntent"
            }
            """.trimIndent()
        }.joinToString("\n\n    ")
    }
    
    /**
     * Discover Tools Goal: Find and analyze external tools for integration
     * 
     * **IMPLEMENTATION STATUS**: ❌ NOT IMPLEMENTED YET - Planned for Milestone 2
     * 
     * @param specification The agent specification to discover tools for
     * @return List<DiscoveredTool> List of tools suitable for integration
     * 
     * @since 2.0.0 (Milestone 2 - Tool Discovery)
     */
    @AchievesGoal(description = "Discover and analyze external tools for integration")
    @Action(
        description = "Discover and analyze external tools for integration using multi-source strategy",
        cost = 0.7,
        value = 0.8
    )
    @ToolGroup ("web, apis, rag")
    fun discoverTools(specification: AgentSpecification): List<DiscoveredTool> {
        logger.info("🔍 Starting tool discovery for domain: ${specification.domain}\n\r" +
                   "📋 Specification: ${specification.specification}\n\r" +
                   "⚡ Action Intents: ${specification.actionIntents}\n\r" +
                   "🎯 Goal Intents: ${specification.goalIntents}\n\r" +
                   "📚 Examples: ${specification.examples}")
        
        // STUB IMPLEMENTATION - Will be enhanced in Milestone 2
        return emptyList()
    }
    
    /**
     * Make Audit Aware Goal: Add comprehensive audit capabilities to generated agents
     * 
     * **IMPLEMENTATION STATUS**: ❌ NOT IMPLEMENTED YET - Planned for Milestone 4
     * 
     * @param agent The generated agent to make audit-aware
     * @return GeneratedAgentModel Agent enhanced with audit capabilities
     * 
     * @since 4.0.0 (Milestone 4 - Audit Framework)
     */
    @AchievesGoal(description = "Add comprehensive audit capabilities to agents")
    @Action(
        description = "Add AOP-based audit framework to generated agents",
        cost = 0.4,
        value = 0.7
    )
    @ToolGroup("aop. audit")
    fun makeAuditAware(agent: GeneratedAgentModel): GeneratedAgentModel {
        logger.info("🔍 Adding audit capabilities to: ${agent.agent.name}")
        
        // STUB IMPLEMENTATION - Will be enhanced in Milestone 4
        return agent // Return unchanged for now
    }
    
    // ========================================
    // Private Helper Methods
    // ========================================
    
    /**
     * Infer appropriate Java/Kotlin class name for the agent.
     * 
     * PURPOSE: Convert user description to proper class name (meta-level naming decision)
     * 
     * FUTURE LLM ENHANCEMENT: Intelligent class naming based on domain understanding.
     * 
     * Current: Simple keyword extraction + "Agent" suffix
     * Future:  using(LlmOptions(Auto)).createObjectIfPossible<String>(prompt)
     * 
     * Example LLM prompt:
     * """
     * Generate an appropriate Java class name for an agent based on this user description:
     * 
     * User Input: "${userInput.content}"
     * 
     * Requirements:
     * - Follow Java naming conventions (PascalCase)
     * - End with "Agent" suffix
     * - Be descriptive but concise
     * - Examples: "RestaurantBookingAgent", "WeatherForecastAgent", "TravelPlannerAgent"
     * 
     * Return only the class name, nothing else.
     * """
     */
    private fun inferAgentName(userInput: UserInput): String {
        // CURRENT FALLBACK: Extract first meaningful keyword + "Agent"
        val content = userInput.content.lowercase()
        val words = content.split(" ", "-", "_").filter { it.length > 3 }
        
        // Find first word that's not a common verb/article
        val keyWord = words.firstOrNull { word ->
            word !in listOf("agent", "create", "help", "user", "system", "make", "with", "that", "this")
        } ?: "Custom"
        
        return keyWord.replaceFirstChar { it.uppercase() } + "Agent"
    }
    
    /**
     * Generate generic action method names from user requirements.
     * 
     * PURPOSE: Create placeholder action names when specific parsing fails.
     * 
     * NOTE: This method is rarely used - extractActionIntentsFromContent() handles most cases.
     * This is a fallback for edge cases where no actionIntents are extracted.
     * 
     * FUTURE LLM ENHANCEMENT: Could extract action intents from unstructured requirements.
     * 
     * Current: Generic numbered actions (performAction1, performAction2, etc.)
     * Future:  using(LlmOptions(Auto)).createObjectIfPossible<List<String>>(prompt)
     * 
     * Example LLM prompt:
     * """
     * Extract high-level action intents from these user requirements:
     * 
     * Requirements: ${requirements.joinToString(", ")}
     * 
     * Return action intents as 2-word phrases (verb + noun):
     * - Focus on what the user wants the agent to DO
     * - Examples: ["process requests", "send notifications", "update records"]
     * - Max 5 actions
     * """
     */

    
    /**
     * Generate generic high-level goal intents from user intent description.
     * 
     * PURPOSE: Create domain-agnostic goal categories for agent specification.
     * 
     * NOTE: These are META-LEVEL goals that describe general outcomes.
     * The actual GOAP world states (like "restaurant_found") get generated later in generateAgent().
     * 
     * FUTURE LLM ENHANCEMENT: Extract user's desired outcomes semantically.
     * 
     * Current: Generic goal templates 
     * Future:  using(LlmOptions(Auto)).createObjectIfPossible<List<String>>(prompt)
     * 
     * Example LLM prompt:
     * """
     * Extract high-level goal categories from this user intent:
     * 
     * User Intent: "${intent}"
     * 
     * Return 2-3 high-level goal categories that describe what outcomes the user wants:
     * - Examples: ["find information", "complete transaction", "send notification"]
     * - Focus on OUTCOMES, not implementation details
     * - Keep generic (specific goals like "restaurant_found" come later)
     * """
     */
    private fun inferGoalsFromIntent(intent: String): List<String> {
        // Smart fallback: look for goal-like words in user's actual intent
        val words = intent.lowercase()               // Normalize case for matching
            .split(" ")                             // Split on spaces (simple split for goal extraction)
            .filter { it.length > 3 }               // Filter out short words ("the", "and", "for")
        val goalWords = listOf("complete", "finish", "achieve", "provide", "ensure", "deliver", "fulfill")
        
        val foundGoals = words.filter { it in goalWords }.take(2)
        return foundGoals.map { goal -> "user request $goal" }.ifEmpty {
            // Extract domain nouns as goals
            val domainNouns = words.filter { 
                it.length > 4 && it !in listOf("agent", "create", "system", "service")
            }.take(1)
            domainNouns.map { "$it completed" }
        }
    }
    
    
    /**
     * Legacy helper method - DEPRECATED
     * 
     * NOTE: This method is redundant - extractActionIntentsFromContent() already handles this.
     * Kept for backward compatibility but should be removed.
     */
    @Deprecated("Use extractActionIntentsFromContent() instead", ReplaceWith("emptyList()"))
    private fun extractIntendedActions(userInput: UserInput): List<String> {
        // TODO: Remove this method - it's redundant
        return emptyList()
    }
    
    /**
     * Create basic Agent instance from specification.
     * 
     * PURPOSE: Creates embabel-agent-api Agent structure for internal processing.
     * 
     * NOTE: This creates a basic Agent object, NOT the final generated code.
     * The actual agent code generation happens in generateAgent() using LLM.
     * 
     * CURRENT: Basic Agent instance with empty actions/goals
     * FUTURE: This method will likely be removed when generateAgent() is implemented
     */
    private fun createAgentFromSpecification(specification: AgentSpecification): com.embabel.agent.core.Agent {
        // Create basic Agent structure for internal use
        return com.embabel.agent.core.Agent(
            name = specification.name,
            description = specification.specification,
            provider = "meta-agent-generated",
            actions = emptyList(), // Will be populated during code generation
            goals = emptySet() // Will be populated during code generation
        )
    }
    
    /**
     * Extract package name from LLM-generated Kotlin code.
     * 
     * PURPOSE: Parse the package declaration from LLM-generated code to ensure
     * file path matches the actual package used in the code.
     * 
     * Examples:
     * - "package com.embabel.agent.generated.booking" → "com.embabel.agent.generated.booking"
     * - "package com.example.restaurant" → "com.example.restaurant" 
     * 
     * @param generatedCode The complete Kotlin code generated by LLM
     * @return Extracted package name or null if not found
     */
    private fun extractPackageNameFromCode(generatedCode: String): String? {
        val packageRegex = Regex("""^package\s+([a-zA-Z][a-zA-Z0-9._]*)\s*$""", RegexOption.MULTILINE)
        val matchResult = packageRegex.find(generatedCode)
        return matchResult?.groupValues?.get(1)
    }
    
    /**
     * Generate Java package name from domain specification (fallback).
     * 
     * PURPOSE: Provide fallback package name when extraction from LLM code fails.
     * 
     * The domain field is already populated with single functional words like:
     * - "restaurant" (from "Create an agent for restaurant booking")
     * - "weather" (from "Build weather tracking agent") 
     * - "analytics" (from "Make an analytics system")
     * 
     * Examples:
     * - domain: "restaurant" → "com.embabel.agent.restaurant"
     * - domain: "weather" → "com.embabel.agent.weather"
     * - domain: "analytics" → "com.embabel.agent.analytics"
     * 
     * @param specification AgentSpecification containing the domain field
     * @return Java package name following clean organization pattern
     */
    private fun generatePackageName(specification: AgentSpecification): String {
        return "com.embabel.agent.${specification.domain}"
    }
    
    /**
     * Generate GOAP planning heuristics based on agent specification.
     * 
     * PURPOSE: Create cost/value metrics for GOAP planning optimization.
     * 
     * NOTE: These are meta-level heuristics for action planning, not domain-specific values.
     * Domain-specific costs get assigned during actual agent code generation.
     * 
     * FUTURE LLM ENHANCEMENT: Intelligent heuristics based on domain complexity.
     * 
     * Example LLM prompt:
     * """
     * Generate GOAP planning heuristics for this agent specification:
     * 
     * Domain: "${specification.domain}"
     * Actions: ${specification.actionIntents}
     * Goals: ${specification.goalIntents}
     * 
     * Return cost/value estimates (0.0-1.0) considering:
     * - Domain complexity (e.g., booking systems vs simple lookups)
     * - Action complexity (e.g., search vs transaction processing)
     * - Expected execution time and resource usage
     * 
     * Format: {"actionType": {"cost": 0.3, "value": 0.8}}
     * """
     */
    private fun generateGoapHeuristics(specification: AgentSpecification): Map<String, Double> {
        // CURRENT: Generic heuristics for planning
        // These provide reasonable defaults for GOAP cost/value calculations
        return mapOf(
            "primaryActionCost" to 0.3,
            "primaryActionValue" to 0.8,
            "secondaryActionCost" to 0.5,
            "secondaryActionValue" to 0.6
        )
    }
}