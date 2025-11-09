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
 * Baby steps: Extract base URLs from API documentation.
 * Focus only on URL discovery for different environments.
 */
@Component
class BaseUrlExtractor {
    
    private val logger = LoggerFactory.getLogger(BaseUrlExtractor::class.java)
    
    /**
     * Extract base URLs for different environments from API documentation.
     */
    fun extractBaseUrls(url: String, context: OperationContext): BaseUrlResult? {
        logger.info("🔍 Extracting base URLs from: $url")
        
        return try {
            // Crawl documentation
            val document = Jsoup.connect(url)
                .userAgent("MetaAgent-BaseUrlExtractor/1.0")
                .timeout(10000)
                .get()
            
            val documentText = document.body().text()
            val title = document.title()
            
            logger.info("📄 Document loaded: $title (${documentText.length} chars)")
            
            // Simple prompt focused only on URL extraction
            val prompt = buildUrlExtractionPrompt(url, title, documentText)
            
            val result = context.promptRunner()
                .withLlm(LlmOptions(criteria = Auto))
                .createObject(prompt, LlmBaseUrlResult::class.java)
            
            BaseUrlResult(
                provider = extractProviderName(url, title),
                documentationUrl = url,
                baseUrls = result
            )
            
        } catch (e: Exception) {
            logger.error("❌ Base URL extraction failed: ${e.message}")
            null
        }
    }
    
    /**
     * Simple, generic prompt focused only on finding base URLs.
     */
    private fun buildUrlExtractionPrompt(url: String, title: String, content: String): String {
        return """
You are analyzing API documentation to find base URLs for different environments.

TASK: Extract API base URLs mentioned in this documentation.

DOCUMENTATION:
- Title: $title
- Source: $url

LOOK FOR:
1. Production API base URL (live/prod environment)
2. Sandbox/staging API base URL (test environment)  
3. Development/test API base URL (dev environment)
4. Demo API base URL (demo environment)

SEARCH FOR PATTERNS LIKE:
- "Base URL: https://api.example.com"
- "Production: https://api.example.com/v1"
- "Sandbox: https://sandbox-api.example.com"
- "Test endpoint: https://test.example.com/api"
- "https://api.example.com/v1" (in code examples)
- "curl https://api.example.com" (in curl examples)

COMMON PATTERNS:
- api.domain.com
- domain.com/api
- sandbox.domain.com
- test-api.domain.com
- api-staging.domain.com
- demo.domain.com/api

Return the base URLs found. If no URLs for a specific environment, use null.

DOCUMENTATION CONTENT:
$content
        """.trimIndent()
    }
    
    private fun extractProviderName(url: String, title: String): String {
        return when {
            url.contains("opentable", ignoreCase = true) || title.contains("opentable", ignoreCase = true) -> "OpenTable"
            url.contains("yelp", ignoreCase = true) || title.contains("yelp", ignoreCase = true) -> "Yelp" 
            url.contains("stripe", ignoreCase = true) || title.contains("stripe", ignoreCase = true) -> "Stripe"
            url.contains("twilio", ignoreCase = true) || title.contains("twilio", ignoreCase = true) -> "Twilio"
            url.contains("github", ignoreCase = true) || title.contains("github", ignoreCase = true) -> "GitHub"
            else -> {
                // Extract from domain: https://docs.example.com -> Example
                val domain = url.substringAfter("://").substringBefore("/").substringBefore(".")
                domain.replaceFirstChar { it.uppercase() }
            }
        }
    }
}

/**
 * LLM response for base URL extraction
 */
@JsonClassDescription("Base URLs for different API environments")
data class LlmBaseUrlResult(
    @JsonPropertyDescription("Production API base URL")
    val production: String? = null,
    
    @JsonPropertyDescription("Sandbox or staging API base URL")
    val sandbox: String? = null,
    
    @JsonPropertyDescription("Test or development API base URL")
    val test: String? = null,
    
    @JsonPropertyDescription("Demo or trial environment API base URL")
    val demo: String? = null
)

/**
 * Result containing extracted base URLs
 */
data class BaseUrlResult(
    val provider: String,
    val documentationUrl: String,
    val baseUrls: LlmBaseUrlResult
)