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
import com.embabel.metaagent.core.agent.MetaAgent
import org.springframework.shell.CompletionContext
import org.springframework.shell.CompletionProposal
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import org.springframework.shell.standard.ValueProvider
import org.springframework.stereotype.Component

/**
 * Provides autocompletion for generated agent Kotlin files.
 */
@Component
class GeneratedAgentFileValueProvider : ValueProvider {
    override fun complete(completionContext: CompletionContext): List<CompletionProposal> {
        val currentInput = completionContext.currentWordUpToCursor() ?: ""
        // Try both relative paths (from meta-agent root or meta-agent-service)
        val baseDirs = listOf(
            java.io.File("meta-agent-examples/src/main/generated/kotlin"),
            java.io.File("../meta-agent-examples/src/main/generated/kotlin")
        )

        val baseDir = baseDirs.firstOrNull { it.exists() } ?: return emptyList()

        return baseDir.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".kt") }
            .map { it.path }
            .filter { it.contains(currentInput, ignoreCase = true) }
            .map { CompletionProposal(it) }
            .toList()
    }
}

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
 * - `gen-tools`: Generate tool skeletons matching agent actions
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

    // Store last agent process to access blackboard for gen-tools
    private var lastAgentProcess: com.embabel.agent.core.AgentProcess? = null

    /**
     * Design a new agent from natural language requirements.
     *
     * Accepts either an inline description or a path to a spec file (resolved relative to CWD).
     *
     * ## Examples
     * ```
     * shell:> design "Create an agent that processes customer orders and sends notifications"
     * shell:> design --spec-file skills.md
     * shell:> design --spec-file /abs/path/to/spec.md
     * ```
     *
     * @param intent Natural language description of the desired agent functionality
     * @param specFile Path to a spec file; resolved relative to CWD when not absolute
     * @return Agent specification summary or delegation result
     */
    @ShellMethod("Design a new agent from requirements")
    fun design(
        @ShellOption(defaultValue = ShellOption.NULL) intent: String?,
        @ShellOption(defaultValue = ShellOption.NULL, value = ["--spec-file", "-f"]) specFile: String?,
    ): String {
        val resolvedIntent = resolveDesignIntent(intent, specFile)
            ?: return resolveDesignIntentError(intent, specFile)

        return try {
            // Delegate to MetaAgent through Autonomy framework using shared session
            val result = autonomy.chooseAndRunAgent(
                intent = "Use MetaAgent to $resolvedIntent",
                processOptions = sharedProcessOptions
            )
            
            // Debug: Extract and show what was actually created
            val agentProcess = result.agentProcess
            lastAgentProcess = agentProcess  // Store for gen-tools access
            val blackboard = agentProcess.processContext.blackboard
            
            buildString {
                appendLine("✅ Meta-agent executed successfully for intent: '$resolvedIntent'")
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
            lastAgentProcess = agentProcess  // Store for gen-tools access
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
     * Generate tool skeletons matching agent actions.
     *
     * Uses the GeneratedAgentModel from the session blackboard (after design + generate).
     * Tools reference data classes defined in the agent file (same package).
     *
     * ## Examples
     * ```
     * shell:> design "find restaurants and compare menus"
     * shell:> generate
     * shell:> gen-tools
     * ```
     *
     * @return Generated tool skeleton or status message
     */
    @ShellMethod("Generate tool skeletons matching agent actions")
    fun genTools(): String {
        // Get GeneratedAgentModel from blackboard
        val process = lastAgentProcess
        if (process == null) {
            return """
                |❌ No agent generated yet.
                |
                |Usage:
                |  1. design "your agent description"
                |  2. generate
                |  3. gen-tools
            """.trimMargin()
        }

        val blackboard = process.processContext.blackboard

        // Try to find GeneratedAgentModel in blackboard
        val generatedAgent = blackboard.objects
            .filterIsInstance<com.embabel.metaagent.core.model.GeneratedAgentModel>()
            .lastOrNull()

        if (generatedAgent == null) {
            return """
                |❌ No GeneratedAgentModel found in session.
                |
                |Make sure you ran 'generate' command first.
                |Usage:
                |  1. design "your agent description"
                |  2. generate
                |  3. gen-tools
            """.trimMargin()
        }

        return try {
            val agentCode = generatedAgent.generatedCode
            val agentClassName = generatedAgent.agentName
                .split(" ", "-", "_")
                .joinToString("") { it.replaceFirstChar { char -> char.uppercase() } }
            val packageName = generatedAgent.packageName

            buildString {
                appendLine("🔧 Tool Generation")
                appendLine()
                appendLine("📦 Package: $packageName")
                appendLine("🎯 Agent: $agentClassName")
                appendLine()

                // Extract all actions - user will prune analytical ones manually
                val actions = extractActions(agentCode)

                appendLine("📊 Actions found: ${actions.size}")
                appendLine("   🔧 Generating tools for all actions:")
                actions.forEach { appendLine("      - $it") }
                appendLine()
                appendLine("💡 Note: Prune analytical actions (compare, summarize, etc.) that")
                appendLine("   should be handled by LLM directly, not as external tools.")
                appendLine()

                // Generate tool class
                val toolClassName = agentClassName.removeSuffix("Agent") + "Tools"
                val toolCode = generateToolCode(packageName, toolClassName, actions, agentCode)

                // Write to file in same directory as agent
                val packageDir = packageName.replace(".", "/")
                val baseDir = findProjectRoot()
                val targetDir = baseDir.resolve("meta-agent-examples/src/main/generated/kotlin/$packageDir")
                java.nio.file.Files.createDirectories(targetDir)
                val toolFile = targetDir.resolve("$toolClassName.kt").toFile()
                toolFile.writeText(toolCode)

                appendLine("✅ Generated tool class: $toolClassName")
                appendLine("📁 Written to: ${toolFile.absolutePath}")
                appendLine()
                appendLine("Generated code:")
                appendLine("─".repeat(60))
                appendLine(toolCode)
                appendLine("─".repeat(60))
                appendLine()
                appendLine("Next steps:")
                appendLine("- Implement TODO sections in generated tool class")
                appendLine("- Wire tools into agent")
                appendLine("- Add dependencies (RestTemplate, Jsoup, etc.)")
            }

        } catch (e: Exception) {
            "❌ Failed to generate tools: ${e.message}\n" +
            "Check logs for detailed error information."
        }
    }

    /**
     * Find project root for writing generated files.
     */
    private fun findProjectRoot(): java.nio.file.Path {
        var currentDir: java.nio.file.Path? = java.nio.file.Paths.get(System.getProperty("user.dir")).toAbsolutePath()
        while (currentDir != null) {
            val dirName = currentDir.fileName?.toString() ?: ""
            if (dirName.startsWith("meta-agent-")) {
                currentDir = currentDir.parent
                continue
            }
            val examplesDir = currentDir.resolve("meta-agent-examples")
            if (java.nio.file.Files.exists(examplesDir) && java.nio.file.Files.isDirectory(examplesDir)) {
                val srcDir = examplesDir.resolve("src")
                if (java.nio.file.Files.exists(srcDir)) {
                    return currentDir
                }
            }
            currentDir = currentDir.parent
        }
        return java.nio.file.Paths.get(System.getProperty("user.dir")).toAbsolutePath()
    }

    private fun extractClassName(code: String): String? {
        val pattern = Regex("""class\s+(\w+)""")
        return pattern.find(code)?.groupValues?.get(1)
    }

    private fun extractPackageName(code: String): String? {
        val pattern = Regex("""package\s+([\w.]+)""")
        return pattern.find(code)?.groupValues?.get(1)
    }

    private fun extractActions(code: String): List<String> {
        val pattern = Regex("""@Action[^)]*\)\s*(?:@\w+[^)]*\)\s*)*fun\s+(\w+)""")
        return pattern.findAll(code).map { it.groupValues[1] }.toList()
    }

    /**
     * All actions are candidates for tool generation.
     * User will manually prune analytical actions that should be handled by LLM directly.
     */
    private fun isIoAction(actionName: String): Boolean = true

    private fun generateToolCode(packageName: String, toolClassName: String,
                                  ioActions: List<String>, agentCode: String): String {
        val methods = ioActions.joinToString("\n\n") { actionName ->
            val methodSignature = extractMethodSignature(agentCode, actionName)
            """    @LlmTool(description = "${actionName.toDescription()}")
    fun $methodSignature {
        logger.info("$actionName called")
        // TODO: Implement API call / web scraping
        TODO("Implement $actionName")
    }"""
        }

        return """package $packageName

import com.embabel.agent.api.annotation.LlmTool
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class $toolClassName(
    @Value("\${'$'}{API_KEY:}") private val apiKey: String = "",
) {
    private val logger = LoggerFactory.getLogger(javaClass)

$methods
}
"""
    }

    /**
     * Extract method signature for tool generation.
     * Preserves parameter names, simplifies types to String for JSON I/O.
     */
    private fun extractMethodSignature(code: String, actionName: String): String {
        val pattern = Regex("""fun\s+$actionName\s*\(([^)]*)\)""")
        val match = pattern.find(code) ?: return "$actionName(): String"

        val params = match.groupValues[1].trim()
        if (params.isBlank()) return "$actionName(): String"

        // Parse each parameter, keep name, simplify type
        val simplifiedParams = params.split(",").joinToString(", ") { param ->
            val parts = param.trim().split(":").map { it.trim() }
            if (parts.size >= 2) {
                val name = parts[0]
                val type = parts[1].substringBefore("=").trim() // handle defaults
                val simpleType = when {
                    type in listOf("String", "Int", "Long", "Double", "Float", "Boolean") -> type
                    else -> "String" // Complex types become JSON string
                }
                "$name: $simpleType"
            } else param.trim()
        }
        return "$actionName($simplifiedParams): String"
    }

    private fun String.toDescription(): String {
        return this.replace(Regex("([A-Z])"), " $1")
            .trim()
            .lowercase()
            .replaceFirstChar { it.uppercase() }
    }

    /**
     * Chat with any agent - Autonomy framework chooses the best agent for the intent.
     *
     * Unlike other commands that delegate to MetaAgent specifically, this command
     * lets the Autonomy framework select the most appropriate agent based on the
     * user's natural language intent.
     *
     * ## Examples
     * ```
     * shell:> chat "find restaurants near me"
     * shell:> chat "design an agent for processing orders"
     * ```
     *
     * @param intent Natural language request
     * @return Agent response
     */
    @ShellMethod("Chat with agents - autonomy chooses the right agent")
    fun ask(intent: String): String {
        logger.info("🔍 Ask command received intent: '$intent'")

        // Debug: list available agents
        logger.info("📋 Available agents:")
        autonomy.agentPlatform.agents().forEach { agent ->
            logger.info("   - ${agent.name}: ${agent.description}")
        }

        return try {
            logger.info("🚀 Calling autonomy.chooseAndRunAgent with intent: '$intent'")
            val result = autonomy.chooseAndRunAgent(
                intent = intent,
                processOptions = sharedProcessOptions
            )

            val agentProcess = result.agentProcess
            logger.info("✅ Selected agent: ${agentProcess.agent.name}")

            buildString {
                appendLine("🤖 Agent: ${agentProcess.agent.name}")
                appendLine("📊 Status: ${agentProcess.status}")
                appendLine()

                // Show action results
                agentProcess.history.forEach { historyItem ->
                    appendLine("   $historyItem")
                }
            }

        } catch (e: Exception) {
            logger.error("❌ Chat error: ${e.message}", e)
            "❌ Error: ${e.message}"
        }
    }

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(MetaAgentShellCommands::class.java)
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
               "- Available Actions: createAgentSpecification, generateAgent, generateTools, discoverTools, makeAuditAware\n" +
               "- Shell Integration: Active\n" +
               "- Autonomy Framework: Connected"
    }
}

/** Returns resolved intent text, or null if validation/file-read failed. */
internal fun resolveDesignIntent(intent: String?, specFile: String?): String? = when {
    intent != null && specFile != null -> null
    specFile != null -> {
        val file = resolveSpecFile(specFile)
        if (file.exists()) file.readText() else null
    }
    intent != null -> intent
    else -> null
}

/** Returns the appropriate error message for a failed resolveDesignIntent. */
internal fun resolveDesignIntentError(intent: String?, specFile: String?): String = when {
    intent != null && specFile != null -> "❌ Provide either intent text or --spec-file, not both."
    specFile != null -> {
        val file = resolveSpecFile(specFile)
        "❌ Spec file not found: ${file.absolutePath}"
    }
    else -> "❌ Provide either intent text or --spec-file."
}

internal fun resolveSpecFile(path: String): java.io.File {
    val file = java.io.File(path)
    return if (file.isAbsolute) file else java.io.File(System.getProperty("user.dir"), path)
}