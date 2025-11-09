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
 * Baby steps: Extract authentication information from API documentation.
 * Focus only on finding authentication methods and requirements.
 */
@Component
class AuthenticationExtractor {
    
    private val logger = LoggerFactory.getLogger(AuthenticationExtractor::class.java)
    
    /**
     * Extract authentication details from API documentation.
     */
    fun extractAuthentication(url: String, context: OperationContext): AuthenticationResult? {
        logger.info("🔐 Extracting authentication info from: $url")
        
        return try {
            // Crawl documentation
            val document = Jsoup.connect(url)
                .userAgent("MetaAgent-AuthenticationExtractor/1.0")
                .timeout(10000)
                .get()
            
            val documentText = document.body().text()
            val title = document.title()
            
            logger.info("📄 Document loaded: $title (${documentText.length} chars)")
            
            // Simple prompt focused only on authentication
            val prompt = buildAuthExtractionPrompt(url, title, documentText)
            
            val result = context.promptRunner()
                .withLlm(LlmOptions(criteria = Auto))
                .createObject(prompt, LlmAuthResult::class.java)
            
            AuthenticationResult(
                provider = extractProviderName(url, title),
                documentationUrl = url,
                authentication = result
            )
            
        } catch (e: Exception) {
            logger.error("❌ Authentication extraction failed: ${e.message}")
            null
        }
    }
    
    /**
     * Simple prompt focused only on finding authentication patterns.
     */
    private fun buildAuthExtractionPrompt(url: String, title: String, content: String): String {
        return """
You are analyzing API documentation to find authentication requirements.

TASK: Extract authentication information from this documentation.

DOCUMENTATION:
- Title: $title
- Source: $url

LOOK FOR AUTHENTICATION PATTERNS:

1. AUTHENTICATION METHODS:
   - API Key authentication
   - Bearer Token authentication  
   - OAuth 2.0 authentication
   - Basic Auth (username/password)
   - Custom authentication schemes

2. WHERE TO INCLUDE AUTHENTICATION:
   - HTTP Headers (Authorization, X-API-Key, etc.)
   - Query parameters (?api_key=xxx)
   - Request body
   - Custom header names

3. AUTHENTICATION EXAMPLES LIKE:
   - "Authorization: Bearer YOUR_API_KEY"
   - "curl -H 'Authorization: Bearer TOKEN'"
   - "X-API-Key: your_api_key_here"
   - "?api_key=YOUR_API_KEY"
   - "Authorization: Basic base64encoded"

4. HOW TO GET CREDENTIALS:
   - API key signup URLs
   - Developer portal registration
   - OAuth application creation
   - Partnership application process

SEARCH FOR KEYWORDS:
- "authentication", "authorization", "API key", "token", "bearer", "oauth"
- "credentials", "sign up", "register", "developer portal"
- "curl -H", "Authorization:", "Bearer", "X-API-Key"

Extract what you find. Return null for fields not mentioned in the documentation.

DOCUMENTATION CONTENT:
$content
        """.trimIndent()
    }
    
    private fun extractProviderName(url: String, title: String): String {
        return when {
            url.contains("yelp", ignoreCase = true) || title.contains("yelp", ignoreCase = true) -> "Yelp"
            url.contains("opentable", ignoreCase = true) || title.contains("opentable", ignoreCase = true) -> "OpenTable"
            url.contains("stripe", ignoreCase = true) || title.contains("stripe", ignoreCase = true) -> "Stripe"
            url.contains("github", ignoreCase = true) || title.contains("github", ignoreCase = true) -> "GitHub"
            else -> {
                val domain = url.substringAfter("://").substringBefore("/").substringBefore(".")
                domain.replaceFirstChar { it.uppercase() }
            }
        }
    }
}

/**
 * LLM response for authentication extraction
 */
@JsonClassDescription("Authentication requirements for API access")
data class LlmAuthResult(
    @JsonPropertyDescription("Authentication method: API_KEY, BEARER_TOKEN, OAUTH, BASIC_AUTH, CUSTOM")
    val method: String? = null,
    
    @JsonPropertyDescription("Where to include auth: HEADER, QUERY_PARAMETER, BODY")
    val location: String? = null,
    
    @JsonPropertyDescription("Parameter name for authentication (e.g., Authorization, X-API-Key, api_key)")
    val parameterName: String? = null,
    
    @JsonPropertyDescription("Example format from documentation (e.g., 'Authorization: Bearer TOKEN', 'X-API-Key: KEY')")
    val exampleFormat: String? = null,
    
    @JsonPropertyDescription("URL where developers can sign up for API access")
    val signupUrl: String? = null,
    
    @JsonPropertyDescription("Additional notes about authentication requirements")
    val notes: String? = null
)

/**
 * Result containing extracted authentication details
 */
data class AuthenticationResult(
    val provider: String,
    val documentationUrl: String,
    val authentication: LlmAuthResult
)