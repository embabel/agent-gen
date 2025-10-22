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
package com.embabel.metaagent.core.tools.analysis

import com.embabel.agent.api.common.OperationContext
import com.embabel.common.ai.model.LlmOptions
import com.embabel.common.ai.model.ModelSelectionCriteria.Companion.Auto
import com.embabel.metaagent.core.model.DiscoveredTool
import com.embabel.metaagent.core.model.ParameterLocation
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI
import java.time.Duration

/**
 * Step 2: Help API Introspection Service.
 * 
 * Tests discovered Help APIs and extracts parameter information from responses.
 * Provides real-time parameter introspection using API documentation endpoints.
 */
@Component
class HelpApiIntrospectionService {

    private val logger = LoggerFactory.getLogger(HelpApiIntrospectionService::class.java)
    
    @Autowired
    private lateinit var parameterDiscoveryService: ApiParameterDiscoveryService
    
    // HTTP client with timeout for Help API testing
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    /**
     * Introspect parameters using discovered Help APIs.
     * 
     * @param api The discovered API tool
     * @param helpApis List of potential Help APIs from Step 1
     * @param context Operation context for LLM calls
     * @return Complete parameter introspection result
     */
    fun introspectParameters(
        api: DiscoveredTool,
        helpApis: List<HelpApi>,
        context: OperationContext
    ): IntrospectionResult {
        logger.info("🔍 Starting Help API introspection for ${api.name}")
        
        return try {
            // 1. Test Help API availability (parallel testing)
            val workingHelpApis = testHelpApiAvailability(helpApis)
            logger.info("✅ Found ${workingHelpApis.size} working Help APIs")
            
            // 2. Parse responses by type
            val parsedSchemas = parseHelpApiResponses(workingHelpApis, context)
            logger.info("📋 Parsed ${parsedSchemas.size} schema responses")
            
            // 3. Extract parameters from schemas
            val extractedParams = extractParametersFromSchemas(parsedSchemas)
            logger.info("🎯 Extracted ${extractedParams.size} parameter sets")
            
            // 4. Merge with existing LLM discovery
            val mergedParams = mergeWithExistingDiscovery(api, extractedParams, context)
            
            // 5. Calculate confidence score
            val confidence = calculateConfidence(workingHelpApis, extractedParams)
            
            IntrospectionResult(
                workingHelpApis = workingHelpApis,
                extractedParameters = mergedParams,
                confidence = confidence,
                introspectionSource = determineSource(workingHelpApis)
            )
            
        } catch (e: Exception) {
            logger.warn("⚠️ Help API introspection failed for ${api.name}: ${e.message}")
            // Fallback to existing LLM discovery
            IntrospectionResult(
                workingHelpApis = emptyList(),
                extractedParameters = parameterDiscoveryService.discoverParametersForApi(api, context),
                confidence = 0.5, // Medium confidence for LLM fallback
                introspectionSource = IntrospectionSource.LLM_FALLBACK
            )
        }
    }
    
    /**
     * Test Help API availability with HTTP requests.
     */
    private fun testHelpApiAvailability(helpApis: List<HelpApi>): List<WorkingHelpApi> {
        logger.info("🌐 Testing ${helpApis.size} Help APIs for availability...")
        
        return helpApis.mapNotNull { helpApi ->
            try {
                // HEAD request first to check availability
                val headRequest = HttpRequest.newBuilder()
                    .uri(URI.create(helpApi.url))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(5))
                    .build()
                
                val headResponse = httpClient.send(headRequest, HttpResponse.BodyHandlers.discarding())
                
                if (headResponse.statusCode() in 200..299) {
                    // Follow up with GET for content
                    val getRequest = HttpRequest.newBuilder()
                        .uri(URI.create(helpApi.url))
                        .GET()
                        .timeout(Duration.ofSeconds(10))
                        .build()
                    
                    val getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString())
                    
                    if (getResponse.statusCode() in 200..299 && getResponse.body().isNotBlank()) {
                        logger.info("✅ Working: ${helpApi.type} at ${helpApi.url}")
                        WorkingHelpApi(
                            helpApi = helpApi,
                            content = getResponse.body(),
                            headers = getResponse.headers().map()
                        )
                    } else {
                        logger.debug("❌ Empty response from ${helpApi.url}")
                        null
                    }
                } else {
                    logger.debug("❌ Not available: ${helpApi.url} (status: ${headResponse.statusCode()})")
                    null
                }
                
            } catch (e: Exception) {
                logger.debug("❌ Failed to reach ${helpApi.url}: ${e.message}")
                null
            }
        }
    }
    
    /**
     * Parse Help API responses by type.
     */
    private fun parseHelpApiResponses(
        workingApis: List<WorkingHelpApi>,
        context: OperationContext
    ): List<ParsedSchema> {
        return workingApis.mapNotNull { workingApi ->
            when (workingApi.helpApi.type) {
                HelpApiType.SWAGGER,
                HelpApiType.OPENAPI -> parseOpenApiSchema(workingApi)
                
                HelpApiType.HELP,
                HelpApiType.DOCUMENTATION,
                HelpApiType.API_DOCS -> parseDocumentationText(workingApi, context)
                
                HelpApiType.SCHEMA -> parseJsonSchema(workingApi)
                
                HelpApiType.GRAPHQL -> parseGraphqlSchema(workingApi)
                
                else -> {
                    logger.debug("Unsupported Help API type: ${workingApi.helpApi.type}")
                    null
                }
            }
        }
    }
    
    /**
     * Parse OpenAPI/Swagger schema (basic implementation).
     */
    private fun parseOpenApiSchema(workingApi: WorkingHelpApi): ParsedSchema? {
        return try {
            // TODO: Add OpenAPI parser dependency and implement full parsing
            // For now, detect if content looks like OpenAPI and extract basic info
            val content = workingApi.content
            
            if (content.contains("\"openapi\"") || content.contains("\"swagger\"")) {
                logger.info("📋 Detected OpenAPI/Swagger schema at ${workingApi.helpApi.url}")
                
                // Basic parameter extraction (enhanced parsing to be added later)
                val basicParams = extractBasicParametersFromJson(content)
                
                ParsedSchema(
                    type = workingApi.helpApi.type,
                    parameters = basicParams,
                    rawContent = content
                )
            } else {
                null
            }
            
        } catch (e: Exception) {
            logger.debug("Failed to parse OpenAPI schema: ${e.message}")
            null
        }
    }
    
    /**
     * Parse documentation text using LLM analysis.
     */
    private fun parseDocumentationText(
        workingApi: WorkingHelpApi,
        context: OperationContext
    ): ParsedSchema? {
        return try {
            logger.info("📖 Analyzing documentation text with LLM: ${workingApi.helpApi.url}")
            
            // Try direct parsing first, fallback to flexible parsing if it fails
            val analysis = try {
                context.promptRunner().withLlm(LlmOptions(criteria = Auto)).createObject(
                    buildDocumentationParsingPrompt(workingApi.content),
                    LlmDocumentationAnalysis::class.java
                )
            } catch (e: com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException) {
                logger.debug("Direct parsing failed due to missing parameters, using fallback: ${e.message}")
                // Use safe fallback to prevent retry loops
                LlmDocumentationAnalysis(endpoints = emptyList())
            } catch (e: com.fasterxml.jackson.core.JsonParseException) {
                logger.debug("Direct parsing failed due to JSON format, using fallback: ${e.message}")
                // Use safe fallback to prevent retry loops
                LlmDocumentationAnalysis(endpoints = emptyList())
            }
            
            val parameters = analysis.endpoints.flatMap { endpoint ->
                endpoint.parameters.map { param ->
                    IntrospectedParameter(
                        name = param.name,
                        type = parseParameterType(param.type),
                        required = param.required,
                        location = parseParameterLocation(param.location),
                        description = param.description,
                        format = param.format,
                        source = ParameterSource.DOCUMENTATION_INTROSPECTION
                    )
                }
            }
            
            ParsedSchema(
                type = workingApi.helpApi.type,
                parameters = parameters,
                rawContent = workingApi.content
            )
            
        } catch (e: Exception) {
            logger.debug("Failed to parse documentation with LLM: ${e.message}")
            null
        }
    }
    
    /**
     * Parse LLM response handling both data format and schema format.
     */
    private fun parseFlexibleLlmResponse(response: String): LlmDocumentationAnalysis {
        return try {
            if (response.contains("\"properties\"") && response.contains("\$schema")) {
                // Handle JSON schema format
                logger.debug("Detected JSON schema format, extracting data...")
                parseSchemaFormat(response)
            } else {
                // Handle direct data format
                com.fasterxml.jackson.module.kotlin.jacksonObjectMapper().readValue(
                    response, 
                    LlmDocumentationAnalysis::class.java
                )
            }
        } catch (e: Exception) {
            logger.warn("Failed to parse LLM response, returning empty analysis: ${e.message}")
            // Return empty analysis as fallback
            LlmDocumentationAnalysis(endpoints = emptyList())
        }
    }
    
    /**
     * Parse JSON schema format and extract actual data.
     */
    private fun parseSchemaFormat(response: String): LlmDocumentationAnalysis {
        return try {
            val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
            val jsonNode = mapper.readTree(response)
            
            // Extract from properties.parameters if it exists
            val parametersNode = jsonNode.get("properties")?.get("parameters")
            
            if (parametersNode != null && parametersNode.isArray) {
                // Convert parameters array to endpoints structure
                val parameters = parametersNode.map { paramNode ->
                    LlmDocumentationParameter(
                        name = paramNode.get("name")?.asText() ?: "unknown",
                        type = paramNode.get("type")?.asText() ?: "string",
                        required = paramNode.get("required")?.asBoolean() ?: false,
                        description = paramNode.get("description")?.asText() ?: "",
                        format = paramNode.get("format")?.asText(),
                        location = paramNode.get("location")?.asText()
                    )
                }
                
                // Create a generic endpoint with extracted parameters
                val endpoint = LlmDocumentationEndpoint(
                    endpoint = "POST /reservations", // Default endpoint
                    parameters = parameters
                )
                
                LlmDocumentationAnalysis(endpoints = listOf(endpoint))
            } else {
                LlmDocumentationAnalysis(endpoints = emptyList())
            }
            
        } catch (e: Exception) {
            logger.warn("Failed to parse schema format: ${e.message}")
            LlmDocumentationAnalysis(endpoints = emptyList())
        }
    }
    
    /**
     * Basic JSON schema parsing (placeholder implementation).
     */
    private fun parseJsonSchema(workingApi: WorkingHelpApi): ParsedSchema? {
        logger.debug("Parsing JSON schema (basic implementation)")
        // TODO: Implement JSON schema parsing
        return null
    }
    
    /**
     * Basic GraphQL schema parsing (placeholder implementation).
     */
    private fun parseGraphqlSchema(workingApi: WorkingHelpApi): ParsedSchema? {
        logger.debug("Parsing GraphQL schema (basic implementation)")
        // TODO: Implement GraphQL introspection parsing
        return null
    }
    
    /**
     * Extract basic parameters from JSON content (simple pattern matching).
     */
    private fun extractBasicParametersFromJson(content: String): List<IntrospectedParameter> {
        val params = mutableListOf<IntrospectedParameter>()
        
        // Basic pattern matching for common parameter indicators
        // TODO: Replace with proper OpenAPI parsing
        
        return params
    }
    
    /**
     * Extract parameters from parsed schemas.
     */
    private fun extractParametersFromSchemas(schemas: List<ParsedSchema>): List<IntrospectedParameter> {
        return schemas.flatMap { it.parameters }
    }
    
    /**
     * Merge introspected parameters with existing LLM discovery.
     */
    private fun mergeWithExistingDiscovery(
        api: DiscoveredTool,
        introspectedParams: List<IntrospectedParameter>,
        context: OperationContext
    ): Map<String, List<ApiParameter>> {
        
        val existingParams = parameterDiscoveryService.discoverParametersForApi(api, context)
        
        if (introspectedParams.isEmpty()) {
            logger.info("📋 No introspected parameters found, using existing discovery")
            return existingParams
        }
        
        // Convert introspected parameters to ApiParameters
        val introspectedApiParams = introspectedParams.map { it.toApiParameter() }
        
        // For now, return introspected parameters grouped by inferred endpoints
        // TODO: Implement sophisticated endpoint matching
        return mapOf(
            "introspected_params" to introspectedApiParams
        )
    }
    
    /**
     * Calculate confidence score based on introspection success.
     */
    private fun calculateConfidence(
        workingApis: List<WorkingHelpApi>,
        extractedParams: List<IntrospectedParameter>
    ): Double {
        val apiScore = when {
            workingApis.isEmpty() -> 0.0
            workingApis.size == 1 -> 0.6
            workingApis.size >= 2 -> 0.8
            else -> 0.4
        }
        
        val paramScore = when {
            extractedParams.isEmpty() -> 0.0
            extractedParams.size < 3 -> 0.4
            extractedParams.size >= 5 -> 0.8
            else -> 0.6
        }
        
        return (apiScore + paramScore) / 2.0
    }
    
    /**
     * Determine primary introspection source.
     */
    private fun determineSource(workingApis: List<WorkingHelpApi>): IntrospectionSource {
        return when {
            workingApis.any { it.helpApi.type == HelpApiType.OPENAPI } -> IntrospectionSource.OPENAPI_SCHEMA
            workingApis.any { it.helpApi.type == HelpApiType.SWAGGER } -> IntrospectionSource.SWAGGER_SCHEMA
            workingApis.any { it.helpApi.type == HelpApiType.DOCUMENTATION } -> IntrospectionSource.DOCUMENTATION_TEXT
            workingApis.size > 1 -> IntrospectionSource.MIXED_SOURCES
            else -> IntrospectionSource.DOCUMENTATION_TEXT
        }
    }
    
    /**
     * Build LLM prompt for documentation parsing.
     */
    private fun buildDocumentationParsingPrompt(content: String): String {
        return """
        Analyze this API documentation and extract parameter information.
        
        Documentation Content:
        ${content.take(2000)}...
        
        Task: Extract all API parameters from this documentation.
        
        Look for:
        - Endpoint definitions with parameters
        - Parameter names, types, and descriptions
        - Required vs optional parameters
        - Parameter locations (query, body, header, path)
        
        IMPORTANT: Return ONLY a JSON object in this exact format:
        {
          "endpoints": [
            {
              "endpoint": "POST /reservations",
              "parameters": [
                {
                  "name": "restaurantId",
                  "type": "string",
                  "required": true,
                  "description": "Unique identifier of the restaurant",
                  "format": null,
                  "location": "body"
                }
              ]
            }
          ]
        }
        
        Do NOT return a JSON schema. Return actual data in the exact format above.
        Focus on practical parameters that would be needed for API calls.
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
 * Result of Help API introspection.
 */
data class IntrospectionResult(
    val workingHelpApis: List<WorkingHelpApi>,
    val extractedParameters: Map<String, List<ApiParameter>>,
    val confidence: Double,
    val introspectionSource: IntrospectionSource
)

/**
 * Working Help API with response content.
 */
data class WorkingHelpApi(
    val helpApi: HelpApi,
    val content: String,
    val headers: Map<String, List<String>>
)

/**
 * Parsed schema from Help API response.
 */
data class ParsedSchema(
    val type: HelpApiType,
    val parameters: List<IntrospectedParameter>,
    val rawContent: String
)

/**
 * Parameter discovered through introspection.
 */
data class IntrospectedParameter(
    val name: String,
    val type: ParameterType,
    val required: Boolean,
    val location: ParameterLocation,
    val description: String?,
    val format: String? = null,
    val source: ParameterSource
) {
    fun toApiParameter(): ApiParameter = ApiParameter(
        name = name,
        type = type,
        required = required,
        description = description ?: "",
        format = format,
        location = location,
        source = source
    )
}

/**
 * Source of introspection data.
 */
enum class IntrospectionSource {
    OPENAPI_SCHEMA,
    SWAGGER_SCHEMA,
    DOCUMENTATION_TEXT,
    MIXED_SOURCES,
    LLM_FALLBACK
}

/**
 * LLM response for documentation analysis.
 */
@JsonClassDescription("Analysis of API documentation for parameter extraction")
data class LlmDocumentationAnalysis(
    @JsonPropertyDescription("List of endpoints found in documentation")
    val endpoints: List<LlmDocumentationEndpoint> = emptyList()
)

@JsonClassDescription("Endpoint information from documentation")
data class LlmDocumentationEndpoint(
    @JsonPropertyDescription("HTTP method and path (e.g., 'POST /reservations')")
    val endpoint: String,
    
    @JsonPropertyDescription("Parameters for this endpoint")
    val parameters: List<LlmDocumentationParameter> = emptyList()
)

@JsonClassDescription("Parameter from documentation analysis")
data class LlmDocumentationParameter(
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
    
    @JsonPropertyDescription("Parameter location: query, body, header, path")
    val location: String?
)