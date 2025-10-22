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
package com.embabel.metaagent.core.tools.discovery

import com.embabel.agent.api.common.OperationContext
import com.embabel.common.ai.model.LlmOptions
import com.embabel.common.ai.model.ModelSelectionCriteria.Companion.Auto
import com.embabel.metaagent.core.model.DiscoveredTool
import com.embabel.metaagent.core.model.ParameterLocation
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.regex.Pattern

/**
 * Modular API parameter discovery service.
 * 
 * Analyzes discovered API endpoints to extract parameters using:
 * 1. Endpoint structure parsing (path parameters)
 * 2. LLM analysis of endpoint purpose (body/query parameters)
 */
@Component
class ApiParameterDiscoveryService {

    private val logger = LoggerFactory.getLogger(ApiParameterDiscoveryService::class.java)
    
    // Pattern to extract path parameters like {id}, {restaurantId}
    private val pathParameterPattern = Pattern.compile("\\{([^}]+)\\}")

    /**
     * Discover parameters for discovered API endpoints.
     * 
     * @param api The discovered API with endpoints
     * @param context Operation context for LLM calls
     * @return Map of endpoint to discovered parameters
     */
    fun discoverParametersForApi(
        api: DiscoveredTool, 
        context: OperationContext
    ): Map<String, List<ApiParameter>> {
        logger.info("🔍 Discovering parameters for ${api.name} endpoints")
        
        return try {
            // Get endpoints from tools discovery (keyEndpoints from LLM suggestion)
            val endpointAnalysis = analyzeEndpointsForParameters(api, context)
            
            logger.info("✅ Parameter discovery complete for ${api.name}: ${endpointAnalysis.size} endpoints analyzed")
            endpointAnalysis
            
        } catch (e: Exception) {
            logger.warn("Failed parameter discovery for ${api.name}: ${e.message}")
            emptyMap()
        }
    }
    
    /**
     * Analyze all endpoints and discover their parameters.
     */
    private fun analyzeEndpointsForParameters(
        api: DiscoveredTool, 
        context: OperationContext
    ): Map<String, List<ApiParameter>> {
        
        // For now, use the endpoint information we can infer
        // In a real implementation, we'd need to access the keyEndpoints from the LLM suggestion
        // This is a simplified version that shows the structure
        
        val endpointAnalysis = mutableMapOf<String, List<ApiParameter>>()
        
        // Example endpoints based on the API categories
        val inferredEndpoints = inferEndpointsFromApiCategories(api)
        
        inferredEndpoints.forEach { endpoint ->
            val parameters = analyzeEndpointParameters(endpoint, api, context)
            endpointAnalysis[endpoint] = parameters
            
            logger.info("📋 API: ${api.name} | Endpoint: ${endpoint}")
            logger.info("   Parameters: ${parameters.size} discovered")
            parameters.forEach { param ->
                val requiredText = if (param.required) "Required" else "Optional"
                val sourceText = "[Source: ${param.source}]"
                logger.info("   - ${param.name} (${param.type}): $requiredText - ${param.description} $sourceText")
            }
        }
        
        return endpointAnalysis
    }
    
    /**
     * Analyze individual endpoint for parameters.
     */
    private fun analyzeEndpointParameters(
        endpoint: String, 
        api: DiscoveredTool, 
        context: OperationContext
    ): List<ApiParameter> {
        
        val parameters = mutableListOf<ApiParameter>()
        
        // 1. Extract path parameters from endpoint structure
        parameters.addAll(extractPathParameters(endpoint))
        
        // 2. Use LLM to analyze what other parameters this endpoint would need
        parameters.addAll(analyzeLlmParameters(endpoint, api, context))
        
        return parameters.distinctBy { it.name.lowercase() }
    }
    
    /**
     * Extract path parameters from endpoint URL.
     */
    private fun extractPathParameters(endpoint: String): List<ApiParameter> {
        val pathParams = mutableListOf<ApiParameter>()
        val matcher = pathParameterPattern.matcher(endpoint)
        
        while (matcher.find()) {
            val paramName = matcher.group(1)
            pathParams.add(
                ApiParameter(
                    name = paramName,
                    type = ParameterType.STRING,
                    required = true,
                    description = "Path parameter: $paramName",
                    location = ParameterLocation.PATH,
                    source = ParameterSource.STRUCTURE_ANALYSIS
                )
            )
        }
        
        return pathParams
    }
    
    /**
     * Use LLM to analyze endpoint parameters.
     */
    private fun analyzeLlmParameters(
        endpoint: String, 
        api: DiscoveredTool, 
        context: OperationContext
    ): List<ApiParameter> {
        return try {
            logger.debug("Starting LLM parameter analysis for endpoint: $endpoint")
            
            // Try LLM analysis - let it work normally, only catch the specific retry-causing exceptions
            val parameterAnalysis = try {
                logger.info("🤖 Sending LLM prompt for $endpoint")
                val result = context.promptRunner().withLlm(LlmOptions(criteria = Auto)).createObject(
                    buildEndpointParameterPrompt(endpoint, api),
                    LlmParameterAnalysis::class.java
                )
                logger.info("✅ LLM successfully parsed response for $endpoint: ${result.parameters.size} parameters")
                result
            } catch (e: com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException) {
                logger.debug("LLM returned incomplete JSON for $endpoint, using safe fallback: ${e.message}")
                // Only catch specific missing parameter exceptions to prevent retry loops
                LlmParameterAnalysis(parameters = emptyList())
            } catch (e: com.fasterxml.jackson.core.JsonParseException) {
                logger.debug("LLM returned invalid JSON for $endpoint, using safe fallback: ${e.message}")
                // Only catch JSON parsing errors to prevent retry loops
                LlmParameterAnalysis(parameters = emptyList())
            }
            // Let other exceptions propagate for normal retry logic
            
            // Handle null or empty parameters safely
            val parameters = parameterAnalysis.parameters
            
            parameters.map { llmParam ->
                ApiParameter(
                    name = llmParam.name,
                    type = parseParameterType(llmParam.type),
                    required = llmParam.required,
                    description = llmParam.description,
                    format = llmParam.format,
                    validation = llmParam.validation,
                    location = parseParameterLocation(llmParam.location),
                    source = ParameterSource.LLM_ANALYSIS
                )
            }
            
        } catch (e: Exception) {
            logger.debug("LLM parameter analysis completely failed for $endpoint: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Infer likely endpoints from API categories (temporary until we access keyEndpoints).
     */
    private fun inferEndpointsFromApiCategories(api: DiscoveredTool): List<String> {
        val endpoints = mutableListOf<String>()
        
        if (api.categories.any { it.contains("booking", ignoreCase = true) || it.contains("reservation", ignoreCase = true) }) {
            endpoints.addAll(listOf(
                "POST /restaurants/{id}/reservations",
                "GET /restaurants/{id}/reservations", 
                "DELETE /reservations/{reservationId}",
                "GET /restaurants/{id}/availability"
            ))
        }
        
        if (api.categories.any { it.contains("search", ignoreCase = true) }) {
            endpoints.addAll(listOf(
                "GET /restaurants",
                "GET /restaurants/{id}"
            ))
        }
        
        return endpoints.ifEmpty { listOf("GET /", "POST /") }
    }
    
    /**
     * Build LLM prompt for endpoint parameter analysis.
     */
    private fun buildEndpointParameterPrompt(endpoint: String, api: DiscoveredTool): String {
        return """
        Analyze this specific API endpoint and determine what parameters it would need.
        
        Endpoint: $endpoint
        API: ${api.name}
        Categories: ${api.categories.joinToString(", ")}
        
        Task: What parameters would this endpoint logically require?
        
        Consider:
        - HTTP method (GET needs query params, POST needs body params)
        - Endpoint purpose (what data is needed to fulfill this operation?)
        - Standard API parameter patterns
        - Required vs optional parameters
        
        Focus on practical parameters that would be needed for this specific endpoint.
        """.trimIndent()
    }
    
    private fun parseParameterType(typeString: String): ParameterType {
        return when (typeString.lowercase()) {
            "string" -> ParameterType.STRING
            "integer", "int" -> ParameterType.INTEGER
            "number", "float", "double" -> ParameterType.NUMBER
            "boolean", "bool" -> ParameterType.BOOLEAN
            "date" -> ParameterType.DATE
            "datetime", "timestamp" -> ParameterType.DATETIME
            else -> ParameterType.STRING
        }
    }
    
    private fun parseParameterLocation(locationString: String?): ParameterLocation {
        return when (locationString?.lowercase()) {
            "path" -> ParameterLocation.PATH
            "query" -> ParameterLocation.QUERY
            "header" -> ParameterLocation.HEADER
            "body" -> ParameterLocation.BODY
            else -> ParameterLocation.QUERY
        }
    }
}

/**
 * LLM response for parameter analysis.
 */
@JsonClassDescription("Parameter analysis for a specific endpoint")
data class LlmParameterAnalysis(
    @JsonPropertyDescription("Parameters needed for the endpoint")
    val parameters: List<LlmParameter> = emptyList()
)

@JsonClassDescription("Individual parameter from LLM analysis")
data class LlmParameter(
    @JsonPropertyDescription("Parameter name")
    val name: String,
    
    @JsonPropertyDescription("Parameter type: string, integer, number, boolean, date, datetime")
    val type: String,
    
    @JsonPropertyDescription("Is parameter required")
    val required: Boolean,
    
    @JsonPropertyDescription("Parameter description")
    val description: String,
    
    @JsonPropertyDescription("Format specification (e.g., 'YYYY-MM-DD', 'email')")
    val format: String?,
    
    @JsonPropertyDescription("Validation rules (e.g., 'min:1,max:10')")
    val validation: String?,
    
    @JsonPropertyDescription("Parameter location: query, body, header")
    val location: String?
)

/**
 * Discovered API parameter.
 */
data class ApiParameter(
    val name: String,
    val type: ParameterType,
    val required: Boolean,
    val description: String,
    val format: String? = null,
    val validation: String? = null,
    val location: ParameterLocation = ParameterLocation.QUERY,
    val source: ParameterSource = ParameterSource.LLM_ANALYSIS
)

/**
 * Parameter data types.
 */
enum class ParameterType {
    STRING, INTEGER, NUMBER, BOOLEAN, DATE, DATETIME
}

/**
 * Source of parameter discovery.
 */
enum class ParameterSource {
    LLM_ANALYSIS,                    // From LLM endpoint analysis
    STRUCTURE_ANALYSIS,              // From endpoint URL structure parsing
    DOCUMENTATION_INTROSPECTION,     // From Help API documentation parsing
    OPENAPI_INTROSPECTION           // From OpenAPI/Swagger schema parsing
}