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
package com.embabel.metaagent.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.javaType

/**
 * Service for matching agent method signatures with extracted API endpoints.
 * Generates @Tool annotations and selects optimal single API provider per agent.
 */
@Component
class ToolsMatchingService {
    
    private val logger = LoggerFactory.getLogger(ToolsMatchingService::class.java)
    
    /**
     * Match agent methods with extracted APIs and generate enhanced agent with @Tool annotations.
     */
    fun matchToolsWithAgent(
        agentClass: KClass<*>,
        extractedAPIs: List<ApiDocumentation>,
        preferredProvider: String? = null
    ): ToolMatchingResult {
        logger.info("🔧 Matching tools for agent: ${agentClass.simpleName}")
        
        // 1. Analyze agent methods
        val agentMethods = analyzeAgentMethods(agentClass)
        logger.info("📋 Found ${agentMethods.size} agent methods to match")
        
        // 2. Score API compatibility for each provider
        val providerScores = scoreProviderCompatibility(agentMethods, extractedAPIs)
        
        // 3. Select single best provider
        val selectedProvider = selectOptimalProvider(providerScores, preferredProvider)
        logger.info("🎯 Selected provider: ${selectedProvider.provider} (score: ${selectedProvider.score})")
        
        // 4. Match methods to APIs
        val toolMatches = matchMethodsToAPIs(agentMethods, selectedProvider.apis)
        
        // 5. Generate enhanced agent source code
        val enhancedSourceCode = generateEnhancedAgentSource(agentClass, toolMatches, selectedProvider)
        
        return ToolMatchingResult(
            agentClass = agentClass.simpleName ?: "UnknownAgent",
            selectedProvider = selectedProvider.provider,
            providerScore = selectedProvider.score,
            totalMethods = agentMethods.size,
            matchedMethods = toolMatches.size,
            toolMatches = toolMatches,
            enhancedSourceCode = enhancedSourceCode
        )
    }
    
    private fun analyzeAgentMethods(agentClass: KClass<*>): List<AgentMethod> {
        return agentClass.functions
            .filter { !it.name.startsWith("get") && !it.name.startsWith("set") }
            .filter { !it.name.equals("equals") && !it.name.equals("hashCode") && !it.name.equals("toString") }
            .map { function ->
                AgentMethod(
                    name = function.name,
                    parameters = function.parameters.drop(1).map { param -> // Skip 'this' parameter
                        MethodParameter(
                            name = param.name ?: "unknown",
                            type = param.type.javaType.typeName,
                            optional = param.isOptional
                        )
                    },
                    returnType = function.returnType.javaType.typeName,
                    function = function
                )
            }
    }
    
    private fun scoreProviderCompatibility(
        agentMethods: List<AgentMethod>,
        extractedAPIs: List<ApiDocumentation>
    ): List<ProviderScore> {
        return extractedAPIs.map { apiDoc ->
            val matchScore = calculateCompatibilityScore(agentMethods, apiDoc.endpoints)
            ProviderScore(
                provider = extractProviderName(apiDoc),
                score = matchScore,
                apis = apiDoc.endpoints,
                documentation = apiDoc
            )
        }.sortedByDescending { it.score }
    }
    
    private fun calculateCompatibilityScore(agentMethods: List<AgentMethod>, endpoints: List<ApiEndpoint>): Double {
        var totalScore = 0.0
        var possibleMatches = 0
        
        agentMethods.forEach { method ->
            val bestEndpointMatch = endpoints.maxByOrNull { endpoint ->
                scoreMethodEndpointMatch(method, endpoint)
            }
            if (bestEndpointMatch != null) {
                totalScore += scoreMethodEndpointMatch(method, bestEndpointMatch)
                possibleMatches++
            }
        }
        
        return if (possibleMatches > 0) totalScore / possibleMatches else 0.0
    }
    
    private fun scoreMethodEndpointMatch(method: AgentMethod, endpoint: ApiEndpoint): Double {
        var score = 0.0
        
        // Semantic matching of method names
        score += scoreSemanticMatch(method.name, endpoint.path, endpoint.description)
        
        // Parameter compatibility
        score += scoreParameterCompatibility(method.parameters, endpoint.parameters)
        
        // HTTP method appropriateness
        score += scoreHttpMethodMatch(method.name, endpoint.method)
        
        return score.coerceIn(0.0, 1.0)
    }
    
    private fun scoreSemanticMatch(methodName: String, path: String, description: String?): Double {
        val keywords = mapOf(
            "search" to listOf("search", "find", "query", "list"),
            "get" to listOf("get", "retrieve", "fetch", "details"),
            "make" to listOf("create", "book", "reserve", "post"),
            "modify" to listOf("update", "change", "modify", "put"),
            "cancel" to listOf("delete", "cancel", "remove")
        )
        
        val methodWords = methodName.lowercase()
        val pathWords = path.lowercase()
        val descWords = description?.lowercase() ?: ""
        
        for ((category, words) in keywords) {
            if (words.any { methodWords.contains(it) }) {
                if (words.any { pathWords.contains(it) || descWords.contains(it) }) {
                    return 0.8
                }
            }
        }
        
        return 0.2
    }
    
    private fun scoreParameterCompatibility(methodParams: List<MethodParameter>, endpointParams: List<ApiParameter>): Double {
        if (methodParams.isEmpty() && endpointParams.isEmpty()) return 0.3
        if (methodParams.isEmpty() || endpointParams.isEmpty()) return 0.1
        
        val matchedParams = methodParams.count { methodParam ->
            endpointParams.any { endpointParam ->
                methodParam.name.lowercase().contains(endpointParam.name.lowercase()) ||
                endpointParam.name.lowercase().contains(methodParam.name.lowercase())
            }
        }
        
        return (matchedParams.toDouble() / methodParams.size) * 0.5
    }
    
    private fun scoreHttpMethodMatch(methodName: String, httpMethod: String): Double {
        return when {
            methodName.startsWith("get") && httpMethod == "GET" -> 0.3
            methodName.startsWith("search") && httpMethod == "GET" -> 0.3
            methodName.startsWith("make") && httpMethod == "POST" -> 0.3
            methodName.startsWith("create") && httpMethod == "POST" -> 0.3
            methodName.startsWith("modify") && httpMethod == "PUT" -> 0.3
            methodName.startsWith("update") && httpMethod == "PUT" -> 0.3
            methodName.startsWith("cancel") && httpMethod == "DELETE" -> 0.3
            methodName.startsWith("delete") && httpMethod == "DELETE" -> 0.3
            else -> 0.1
        }
    }
    
    private fun extractProviderName(apiDoc: ApiDocumentation): String {
        return when {
            apiDoc.url.contains("opentable", ignoreCase = true) -> "OpenTable"
            apiDoc.url.contains("yelp", ignoreCase = true) -> "Yelp"
            apiDoc.title.contains("opentable", ignoreCase = true) -> "OpenTable"
            apiDoc.title.contains("yelp", ignoreCase = true) -> "Yelp"
            else -> "Unknown"
        }
    }
    
    private fun selectOptimalProvider(
        providerScores: List<ProviderScore>,
        preferredProvider: String?
    ): ProviderScore {
        return if (preferredProvider != null) {
            providerScores.find { it.provider.equals(preferredProvider, ignoreCase = true) }
                ?: providerScores.first()
        } else {
            providerScores.first()
        }
    }
    
    private fun matchMethodsToAPIs(
        agentMethods: List<AgentMethod>,
        endpoints: List<ApiEndpoint>
    ): List<ToolMatch> {
        return agentMethods.mapNotNull { method ->
            val bestEndpoint = endpoints.maxByOrNull { endpoint ->
                scoreMethodEndpointMatch(method, endpoint)
            }
            
            if (bestEndpoint != null && scoreMethodEndpointMatch(method, bestEndpoint) > 0.4) {
                ToolMatch(
                    method = method,
                    endpoint = bestEndpoint,
                    matchScore = scoreMethodEndpointMatch(method, bestEndpoint)
                )
            } else null
        }
    }
    
    private fun generateEnhancedAgentSource(
        agentClass: KClass<*>,
        toolMatches: List<ToolMatch>,
        selectedProvider: ProviderScore
    ): String {
        val packageName = agentClass.java.`package`?.name ?: "com.embabel.agent.generated"
        val baseClassName = agentClass.simpleName?.removeSuffix("Agent") ?: "Generated"
        val providerToolGroup = selectedProvider.provider.lowercase()
        
        return buildString {
            appendLine("package $packageName")
            appendLine()
            appendLine("import com.embabel.agent.api.annotation.*")
            appendLine("import com.embabel.agent.api.common.OperationContext")
            appendLine()
            appendLine("/**")
            appendLine(" * ${baseClassName}Tooling - API wrapper for ${selectedProvider.provider}")
            appendLine(" * This is NOT the agent - it's the tooling class that provides @Tool methods")
            appendLine(" * Provider: ${selectedProvider.provider}")
            appendLine(" * Compatibility Score: ${String.format("%.2f", selectedProvider.score)}")
            appendLine(" * Matched Methods: ${toolMatches.size}")
            appendLine(" * Tool Group: $providerToolGroup-tooling")
            appendLine(" */")
            appendLine("@Component")
            appendLine("class ${baseClassName}Tooling {")
            appendLine()
            
            toolMatches.forEach { match ->
                appendToolMethod(match, "${providerToolGroup}-tooling")
            }
            
            appendLine("}")
        }
    }
    
    private fun StringBuilder.appendToolMethod(match: ToolMatch, toolGroup: String) {
        val method = match.method
        val endpoint = match.endpoint
        
        appendLine("    /**")
        appendLine("     * ${endpoint.description ?: method.name}")
        appendLine("     * API: ${endpoint.method} ${endpoint.path}")
        appendLine("     * Match Score: ${String.format("%.2f", match.matchScore)}")
        appendLine("     */")
        appendLine("    @Tool(description = \"${endpoint.description ?: method.name}\")")
        
        val paramString = method.parameters.joinToString(", ") { param ->
            val optionalSuffix = if (param.optional) "? = null" else ""
            "${param.name}: ${param.type}$optionalSuffix"
        }
        
        appendLine("    fun ${method.name}($paramString): ${method.returnType} {")
        appendLine("        // API Call: ${endpoint.method} ${endpoint.path}")
        appendLine("        // Parameters: ${endpoint.parameters.joinToString { "${it.name}:${it.type}" }}")
        appendLine("        TODO(\"Implement ${endpoint.method} ${endpoint.path}\")")
        appendLine("    }")
        appendLine()
    }
}

/**
 * Result of tools matching process
 */
data class ToolMatchingResult(
    val agentClass: String,
    val selectedProvider: String,
    val providerScore: Double,
    val totalMethods: Int,
    val matchedMethods: Int,
    val toolMatches: List<ToolMatch>,
    val enhancedSourceCode: String
)

/**
 * Provider compatibility scoring
 */
data class ProviderScore(
    val provider: String,
    val score: Double,
    val apis: List<ApiEndpoint>,
    val documentation: ApiDocumentation
)

/**
 * Tool matching between agent method and API endpoint
 */
data class ToolMatch(
    val method: AgentMethod,
    val endpoint: ApiEndpoint,
    val matchScore: Double
)

/**
 * Analyzed agent method
 */
data class AgentMethod(
    val name: String,
    val parameters: List<MethodParameter>,
    val returnType: String,
    val function: KFunction<*>
)

/**
 * Method parameter information
 */
data class MethodParameter(
    val name: String,
    val type: String,
    val optional: Boolean
)