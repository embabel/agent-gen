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
package com.embabel.metaagent.service.shell

import com.embabel.agent.api.common.autonomy.Autonomy
import com.embabel.agent.domain.io.UserInput
import com.embabel.metaagent.core.agent.MetaAgent
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption

/**
 * Shell commands for Meta-Agent operations.
 * 
 * Extends the embabel-agent-shell pattern by delegating shell commands to the MetaAgent
 * through the Autonomy framework. This implements the Shell-to-Agent delegation pattern
 * where shell commands trigger agent goals and actions.
 * 
 * ## Available Commands
 * - `design`: Create agent specification from natural language requirements
 * - `generate`: Generate agent code from specification
 * - `test`: Test generated agent functionality  
 * - `recover`: Recover from compilation errors
 * 
 * ## Agent-as-Agent Architecture
 * This class implements the revolutionary approach where the meta-agent itself is a 
 * first-class `@Agent` using GOAP planning. Shell commands delegate to agent actions
 * rather than calling service methods directly.
 * 
 * @param metaAgent The core meta-agent instance with @Agent annotation
 * @param autonomy The autonomy framework for goal-oriented execution
 */
@ShellComponent
class MetaAgentShellCommands(
    private val metaAgent: MetaAgent,
    private val autonomy: Autonomy
) {
    
    // Shared session across all commands to enable GOAP type-based chaining
    private val sharedProcessOptions = com.embabel.agent.core.ProcessOptions()

    /**
     * Design a new agent from natural language requirements.
     * 
     * This command delegates to the MetaAgent's `createAgentSpecification` action,
     * which uses GOAP planning to analyze user requirements and create a detailed
     * agent specification.
     * 
     * ## Examples
     * ```
     * shell:> design "Create an agent that processes customer orders and sends notifications"
     * shell:> design "Build an agent for analyzing financial data and generating reports"
     * ```
     * 
     * @param intent Natural language description of the desired agent functionality
     * @return Agent specification summary or delegation result
     */
    @ShellMethod("Design a new agent from requirements")
    fun design(intent: String): String {
        return try {
            // Delegate to MetaAgent through Autonomy framework using shared session
            val result = autonomy.chooseAndRunAgent(
                intent = "Use MetaAgent to $intent",
                processOptions = sharedProcessOptions
            )
            
            // Debug: Extract and show what was actually created
            val agentProcess = result.agentProcess
            val blackboard = agentProcess.processContext.blackboard
            
            buildString {
                appendLine("✅ Meta-agent executed successfully for intent: '$intent'")
                appendLine("🎯 Agent: ${agentProcess.agent.name}")
                appendLine("📊 Status: ${agentProcess.status}")
                appendLine()
                
                // Debug blackboard contents
                appendLine("🔍 Debug Information:")
                try {
                    appendLine("📋 Blackboard info: ${blackboard.infoString(verbose = true)}")
                } catch (e: Exception) {
                    appendLine("📋 Blackboard access error: ${e.message}")
                    appendLine("📋 Blackboard type: ${blackboard::class.simpleName}")
                }
                
                // Also check action results
                appendLine()
                appendLine("🔍 Action Results:")
                agentProcess.history.forEach { historyItem ->
                    appendLine("   ${historyItem}")
                }
                
                appendLine()
                appendLine("Next steps:")
                appendLine("- Use 'generate' to create agent code")
                appendLine("- Use 'test' to validate the generated agent") 
                appendLine("- Use 'status' to check meta-agent capabilities")
            }
            
        } catch (e: Exception) {
            "❌ Failed to design agent: ${e.message}\n" +
            "Check logs for detailed error information."
        }
    }

    /**
     * Generate agent code from the current specification.
     * 
     * This command delegates to the MetaAgent's `generateAgent` action to create
     * actual Kotlin agent code with embabel-agent-api annotations.
     * 
     * @return Generated agent code or error message
     */
    @ShellMethod("Generate agent code from specification")
    fun generate(): String {
        return try {
            // Delegate to MetaAgent through Autonomy framework using shared session
            // This should automatically find the AgentSpecification from the design command
            val result = autonomy.chooseAndRunAgent(
                intent = "Use MetaAgent to generate agent code from the existing specification",
                processOptions = sharedProcessOptions
            )
            
            // Extract and display the generated code
            val agentProcess = result.agentProcess
            val blackboard = agentProcess.processContext.blackboard
            
            buildString {
                appendLine("✅ Agent code generation completed successfully")
                appendLine("🎯 Agent: ${agentProcess.agent.name}")
                appendLine("📊 Status: ${agentProcess.status}")
                appendLine()
                
                // Debug blackboard to find GeneratedAgentModel
                try {
                    appendLine("🔍 Debug Information:")
                    appendLine("📋 Blackboard info: ${blackboard.infoString(verbose = true)}")
                } catch (e: Exception) {
                    appendLine("📋 Blackboard access error: ${e.message}")
                }
                
                // Look for generated code in the action results
                appendLine()
                appendLine("🔍 Action Results:")
                agentProcess.history.forEach { historyItem ->
                    appendLine("   ${historyItem}")
                }
                
                appendLine()
                appendLine("Next steps:")
                appendLine("- Use 'test' to validate the generated agent")
                appendLine("- Check logs for generated code details")
                appendLine("- Use 'status' to check meta-agent capabilities")
            }
            
        } catch (e: Exception) {
            "❌ Failed to generate agent code: ${e.message}\n" +
            "Check logs for detailed error information."
        }
    }

    /**
     * Test the generated agent functionality.
     * 
     * This command will delegate to the MetaAgent's testing actions when implemented.
     * Currently returns a placeholder message.
     * 
     * @return Test result or placeholder message
     */
    @ShellMethod("Test generated agent functionality")
    fun test(): String {
        return "Test command not yet implemented. This will delegate to MetaAgent testing actions."
    }

    /**
     * Recover from compilation or runtime errors.
     * 
     * This command will delegate to the MetaAgent's error recovery actions when implemented.
     * Currently returns a placeholder message.
     * 
     * @return Recovery result or placeholder message
     */
    @ShellMethod("Recover from compilation errors")
    fun recover(): String {
        return "Recovery command not yet implemented. This will delegate to MetaAgent error recovery actions."
    }

    /**
     * Show current meta-agent status and capabilities.
     * 
     * @return Meta-agent status information
     */
    @ShellMethod("Show meta-agent status")
    fun status(): String {
        return "Meta-Agent Status:\n" +
               "- Agent Name: ${metaAgent.javaClass.simpleName}\n" +
               "- Architecture: Agent-as-Agent with GOAP planning\n" +
               "- Available Actions: createAgentSpecification, generateAgent, discoverTools, makeAuditAware\n" +
               "- Shell Integration: Active\n" +
               "- Autonomy Framework: Connected"
    }
}