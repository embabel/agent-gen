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

import com.embabel.metaagent.core.model.DiscoveredTool
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Step 1: Discover Help APIs for introspection.
 * 
 * Separate module that finds API introspection capabilities.
 * Does not modify existing functionality.
 */
@Component
class HelpApiDiscoveryService {

    private val logger = LoggerFactory.getLogger(HelpApiDiscoveryService::class.java)

    /**
     * Discover Help APIs for an API.
     * 
     * @param api The discovered API to analyze
     * @return List of potential Help API URLs
     */
    fun discoverHelpApis(api: DiscoveredTool): List<HelpApi> {
        logger.info("🔍 Discovering Help APIs for ${api.name}")
        
        val helpApis = generatePotentialHelpApis(api)
        
        logger.info("✅ Found ${helpApis.size} potential Help APIs for ${api.name}")
        helpApis.forEach { helpApi ->
            logger.info("   📋 ${helpApi.type}: ${helpApi.url}")
        }
        
        return helpApis
    }
    
    /**
     * Generate potential Help API URLs to config.
     */
    private fun generatePotentialHelpApis(api: DiscoveredTool): List<HelpApi> {
        val baseUrl = api.apiUrl.trimEnd('/')
        
        return listOf(
            HelpApi("$baseUrl/help", HelpApiType.HELP),
            HelpApi("$baseUrl/docs", HelpApiType.DOCUMENTATION),
            HelpApi("$baseUrl/schema", HelpApiType.SCHEMA),
            HelpApi("$baseUrl/swagger.json", HelpApiType.SWAGGER),
            HelpApi("$baseUrl/openapi.json", HelpApiType.OPENAPI),
            HelpApi("$baseUrl/api-docs", HelpApiType.API_DOCS),
            HelpApi("$baseUrl/graphql", HelpApiType.GRAPHQL)
        )
    }
}

/**
 * Discovered Help API with metadata.
 */
data class HelpApi(
    val url: String,
    val type: HelpApiType
)

/**
 * Types of Help APIs.
 */
enum class HelpApiType {
    HELP,           // Generic help API
    DOCUMENTATION,  // Documentation API
    SCHEMA,         // Schema definition API
    SWAGGER,        // Swagger specification API
    OPENAPI,        // OpenAPI specification API  
    API_DOCS,       // API documentation API
    GRAPHQL         // GraphQL introspection API
}