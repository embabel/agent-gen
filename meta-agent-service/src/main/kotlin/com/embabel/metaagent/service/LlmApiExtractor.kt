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

import com.embabel.agent.api.common.OperationContext
import com.embabel.common.ai.model.LlmOptions
import com.embabel.common.ai.model.ModelSelectionCriteria.Companion.Auto
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * LLM-based API extractor that can handle any documentation format.
 * Uses AI to intelligently parse API documentation regardless of structure.
 */
@Component
class LlmApiExtractor {
    
    private val logger = LoggerFactory.getLogger(LlmApiExtractor::class.java)
    
    /**
     * Option 1: Extract API info from URL (if LLM supports web browsing)
     */
    fun extractFromUrl(url: String, context: OperationContext): ApiDocumentation? {
        logger.info("🌐 Attempting LLM extraction from URL: $url")
        
        return try {
            val prompt = """
            Please visit the following URL and extract any API endpoints you find:
            
            URL: $url
            
            Look for:
            - HTTP methods (GET, POST, PUT, DELETE, PATCH, etc.)
            - API endpoints/paths
            - Parameters (query, path, body parameters)
            - Parameter types and requirements
            - Response formats
            
            Extract all API information you can find and return it in structured format.
            If no API documentation is found, return empty endpoints array.
            """.trimIndent()
            
            val result = context.promptRunner()
                .withLlm(LlmOptions(criteria = Auto))
                .createObject(prompt, LlmApiExtractionResult::class.java)
            
            ApiDocumentation(
                title = result.title ?: "API Documentation",
                url = url,
                endpoints = result.endpoints.map { llmEndpoint ->
                    ApiEndpoint(
                        method = llmEndpoint.method,
                        path = llmEndpoint.path,
                        parameters = llmEndpoint.parameters.map { llmParam ->
                            ApiParameter(
                                name = llmParam.name,
                                type = llmParam.type,
                                required = llmParam.required,
                                description = llmParam.description
                            )
                        },
                        description = llmEndpoint.description
                    )
                }
            )
            
        } catch (e: Exception) {
            logger.warn("❌ LLM URL extraction failed: ${e.message}")
            null
        }
    }
    
    /**
     * Option 2: Extract API info from full document text
     */
    fun extractFromDocument(url: String, context: OperationContext): ApiDocumentation? {
        logger.info("📄 Crawling and extracting from document: $url")
        
        return try {
            // First crawl the document
            val document = Jsoup.connect(url)
                .userAgent("MetaAgent-LlmApiExtractor/1.0")
                .timeout(10000)
                .get()
            
            val documentText = document.body().text()
            val title = document.title()
            
            logger.info("📏 Document loaded: $title (${documentText.length} chars)")
            
            // Check if document is too large for LLM context
            if (documentText.length > 100000) {
                logger.warn("⚠️ Document too large (${documentText.length} chars), truncating...")
            }
            
            // Truncate if necessary (keep first part which usually has API overview)
            val textToAnalyze = if (documentText.length > 100000) {
                documentText.take(100000) + "\n[Document truncated...]"
            } else {
                documentText
            }
            
            val prompt = """
            Please analyze the following API documentation and extract all API endpoints:
            
            Document Title: $title
            URL: $url
            
            Look for:
            - HTTP methods and endpoints
            - API paths and URLs  
            - Parameters (query, path, body, headers)
            - Parameter types, requirements, and descriptions
            - Response information
            
            Extract everything you can find and return in structured format.
            
            Documentation Content:
            $textToAnalyze
            """.trimIndent()
            
            val result = context.promptRunner()
                .withLlm(LlmOptions(criteria = Auto))
                .createObject(prompt, LlmApiExtractionResult::class.java)
            
            logger.info("🎯 LLM found ${result.endpoints.size} API endpoints")
            
            ApiDocumentation(
                title = result.title ?: title,
                url = url,
                endpoints = result.endpoints.map { llmEndpoint ->
                    ApiEndpoint(
                        method = llmEndpoint.method,
                        path = llmEndpoint.path,
                        parameters = llmEndpoint.parameters.map { llmParam ->
                            ApiParameter(
                                name = llmParam.name,
                                type = llmParam.type,
                                required = llmParam.required,
                                description = llmParam.description
                            )
                        },
                        description = llmEndpoint.description
                    )
                }
            )
            
        } catch (e: Exception) {
            logger.error("❌ LLM document extraction failed: ${e.message}")
            null
        }
    }
}

/**
 * LLM response structure for API extraction
 */
@JsonClassDescription("Extracted API documentation from analysis")
data class LlmApiExtractionResult(
    @JsonPropertyDescription("Title of the API documentation")
    val title: String? = null,
    
    @JsonPropertyDescription("List of API endpoints found")
    val endpoints: List<LlmApiEndpoint> = emptyList()
)

@JsonClassDescription("Individual API endpoint")
data class LlmApiEndpoint(
    @JsonPropertyDescription("HTTP method (GET, POST, PUT, DELETE, etc.)")
    val method: String,
    
    @JsonPropertyDescription("API endpoint path")
    val path: String,
    
    @JsonPropertyDescription("Endpoint description")
    val description: String? = null,
    
    @JsonPropertyDescription("List of parameters")
    val parameters: List<LlmApiParameter> = emptyList()
)

@JsonClassDescription("API parameter")
data class LlmApiParameter(
    @JsonPropertyDescription("Parameter name")
    val name: String,
    
    @JsonPropertyDescription("Parameter type (string, integer, boolean, etc.)")
    val type: String,
    
    @JsonPropertyDescription("Whether parameter is required")
    val required: Boolean,
    
    @JsonPropertyDescription("Parameter description")
    val description: String? = null
)

/**
 * Extracted API documentation from a site.
 */
data class ApiDocumentation(
    val title: String,
    val url: String,
    val endpoints: List<ApiEndpoint>
)

/**
 * Individual API endpoint.
 */
data class ApiEndpoint(
    val method: String,
    val path: String,
    val parameters: List<ApiParameter>,
    val description: String? = null
)

/**
 * API parameter definition.
 */
data class ApiParameter(
    val name: String,
    val type: String,
    val required: Boolean,
    val description: String? = null
)