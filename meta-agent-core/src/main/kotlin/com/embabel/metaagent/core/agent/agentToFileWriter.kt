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

import com.embabel.metaagent.core.model.GeneratedAgentModel
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

private val logger = LoggerFactory.getLogger("AgentToFileWriter")

/**
 * Write generated agent code to filesystem alongside console output.
 * 
 * PURPOSE: Persist generated agent code to proper Maven directory structure
 * for IntelliJ recognition and compilation.
 * 
 * **File Structure Created:**
 * ```
 * meta-agent-examples/src/main/generated/kotlin/
 * └── com/
 *     └── embabel/
 *         └── agent/
 *             └── generated/
 *                 └── {domain}/
 *                     └── {AgentName}.kt
 * ```
 * 
 * @param generatedAgent The generated agent model containing code and metadata
 * @return Path to the written file for verification and logging, null if failed
 */
fun writeAgentToFile(generatedAgent: GeneratedAgentModel): Path? {
    return try {
        // Determine the className from the agent name (ensure UpperCamelCase)
        val className = generatedAgent.agent.name
            .split(" ", "-", "_")
            .joinToString("") { it.replaceFirstChar { char -> char.uppercase() } }
        
        // Convert package name to directory structure
        val packageDir = generatedAgent.packageName.replace(".", "/")
        
        // Create the full path: meta-agent-examples/src/main/generated/kotlin/{package}/{ClassName}.kt
        val baseDir = findProjectRoot()
        val targetDir = baseDir.resolve("meta-agent-examples/src/main/generated/kotlin/$packageDir")
        val targetFile = targetDir.resolve("$className.kt")
        
        logger.info("📁 Writing agent code to filesystem:")
        logger.info("   📂 Target directory: $targetDir")
        logger.info("   📄 Target file: $targetFile")
        
        // Create directory structure if it doesn't exist
        Files.createDirectories(targetDir)
        logger.debug("✅ Created directory structure: $targetDir")
        
        // Write the generated code to file
        Files.write(
            targetFile, 
            generatedAgent.generatedCode.toByteArray(),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
        
        logger.info("✅ Successfully wrote agent to file:")
        logger.info("   📄 File: $targetFile")
        logger.info("   📏 Size: ${generatedAgent.generatedCode.length} characters")
        logger.info("   📦 Package: ${generatedAgent.packageName}")
        logger.info("   🎯 Agent: ${generatedAgent.agent.name}")
        
        targetFile
        
    } catch (e: Exception) {
        logger.error("❌ Failed to write agent to file: ${e.message}", e)
        logger.warn("🔄 Agent code is still available in console output and GeneratedAgentModel")
        null
    }
}

/**
 * Find the project root directory by looking for the parent pom.xml.
 * 
 * Starts from current working directory and walks up the directory tree
 * until it finds a directory containing pom.xml with meta-agent artifactId.
 */
private fun findProjectRoot(): Path {
    var currentDir = Paths.get(System.getProperty("user.dir"))
    
    // Walk up the directory tree looking for meta-agent root
    while (currentDir != null) {
        val pomFile = currentDir.resolve("pom.xml")
        if (Files.exists(pomFile)) {
            // Check if this is the meta-agent root by looking for meta-agent-examples subdirectory
            val examplesDir = currentDir.resolve("meta-agent-examples")
            if (Files.exists(examplesDir)) {
                logger.debug("📁 Found project root: $currentDir")
                return currentDir
            }
        }
        currentDir = currentDir.parent
    }
    
    // Fallback to current directory if we can't find the root
    val fallback = Paths.get(System.getProperty("user.dir"))
    logger.warn("⚠️ Could not find project root, using current directory: $fallback")
    return fallback
}