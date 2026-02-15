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
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * API Verification Harness - Tests actual API endpoint accessibility.
 * 
 * Takes discovered API signatures and base URLs, constructs full endpoints,
 * and tests real connectivity to determine what's accessible without partnerships.
 */
@Component
class ApiVerificationHarness {
    
    private val logger = LoggerFactory.getLogger(ApiVerificationHarness::class.java)
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()
    
    /**
     * Verify API endpoints by combining extracted signatures with base URLs.
     * 
     * @param extractedApis API documentation from LlmApiExtractor
     * @param baseUrls Base URLs from BaseUrlExtractor
     * @param context Operation context
     * @return List of verification results
     */
    fun verifyApiEndpoints(
        extractedApis: ApiDocumentation,
        baseUrls: LlmBaseUrlResult,
        context: OperationContext
    ): List<ApiVerificationResult> {
        logger.info("🧪 Starting API verification harness for ${extractedApis.endpoints.size} endpoints")
        
        val results = mutableListOf<ApiVerificationResult>()
        val baseUrl = selectBestBaseUrl(baseUrls)
        
        if (baseUrl == null) {
            logger.warn("⚠️ No base URL available for verification")
            return emptyList()
        }
        
        logger.info("🔗 Using base URL: $baseUrl")
        
        // Smart chaining: First find search endpoints to extract IDs for detail endpoints
        val extractedIds = mutableMapOf<String, String>()
        
        // Step 1: Test search endpoints first and extract IDs
        val searchEndpoints = extractedApis.endpoints.filter { isSearchEndpoint(it) }
        searchEndpoints.forEach { endpoint ->
            try {
                val testUrl = constructSearchTestUrl(baseUrl, endpoint)
                val verification = testEndpointWithResponse(testUrl, endpoint.method)
                
                results.add(ApiVerificationResult(
                    endpoint = endpoint,
                    fullUrl = testUrl,
                    baseUrl = baseUrl,
                    verification = verification.first
                ))
                
                // Extract business IDs from search response if we got any response
                if (verification.second != null) {
                    val extractedId = extractBusinessId(verification.second!!)
                    if (extractedId != null) {
                        extractedIds["id"] = extractedId
                        extractedIds["business_id"] = extractedId
                        logger.info("🔑 Extracted business ID: $extractedId")
                    }
                }
                
                logger.info("✅ Tested ${endpoint.method} ${endpoint.path} → ${verification.first.status}")
                
            } catch (e: Exception) {
                logger.error("❌ Failed to verify search endpoint ${endpoint.path}: ${e.message}")
                results.add(createErrorResult(endpoint, baseUrl, e))
            }
        }
        
        // Step 2: Test detail endpoints with extracted IDs (only if we have IDs)
        val detailEndpoints = extractedApis.endpoints.filter { !isSearchEndpoint(it) }
        if (extractedIds.isNotEmpty()) {
            logger.info("🔑 Testing detail endpoints with extracted ID: ${extractedIds["id"]}")
            detailEndpoints.forEach { endpoint ->
                try {
                    val fullUrl = constructDetailUrl(baseUrl, endpoint, extractedIds)
                    val verification = testEndpoint(fullUrl, endpoint.method)
                    
                    results.add(ApiVerificationResult(
                        endpoint = endpoint,
                        fullUrl = fullUrl,
                        baseUrl = baseUrl,
                        verification = verification
                    ))
                    
                    logger.info("✅ Tested ${endpoint.method} ${endpoint.path} → ${verification.status}")
                    
                } catch (e: Exception) {
                    logger.error("❌ Failed to verify detail endpoint ${endpoint.path}: ${e.message}")
                    results.add(createErrorResult(endpoint, baseUrl, e))
                }
            }
        } else {
            logger.warn("⚠️ No business IDs extracted from search - skipping detail endpoint tests")
            detailEndpoints.forEach { endpoint ->
                results.add(ApiVerificationResult(
                    endpoint = endpoint,
                    fullUrl = "SKIPPED - No extracted ID",
                    baseUrl = baseUrl,
                    verification = EndpointVerification(
                        status = ApiAccessStatus.ERROR,
                        statusCode = null,
                        responseTimeMs = 0,
                        errorMessage = "No business ID extracted from search endpoints"
                    )
                ))
            }
        }
        
        logger.info("🎯 Verification complete: ${results.size} endpoints tested")
        logVerificationSummary(results)
        
        return results
    }
    
    /**
     * Select the best available base URL for testing.
     */
    private fun selectBestBaseUrl(baseUrls: LlmBaseUrlResult): String? {
        return baseUrls.production ?: baseUrls.sandbox ?: baseUrls.test ?: baseUrls.demo
    }
    
    /**
     * Check if endpoint is a search endpoint that can provide IDs.
     */
    private fun isSearchEndpoint(endpoint: ApiEndpoint): Boolean {
        return endpoint.path.contains("search", ignoreCase = true) && 
               endpoint.method.uppercase() == "GET"
    }
    
    /**
     * Construct search URL for verification.
     *
     * Note: We don't add test parameters here because:
     * 1. Different APIs have different required parameters
     * 2. Verification checks endpoint existence, not data retrieval
     * 3. 400 Bad Request still proves endpoint exists and is accessible
     */
    private fun constructSearchTestUrl(baseUrl: String, endpoint: ApiEndpoint): String {
        val cleanBase = baseUrl.trimEnd('/')
        val cleanPath = endpoint.path.removePrefix("/v3").let {
            if (it.startsWith("/")) it else "/$it"
        }
        return "$cleanBase$cleanPath"
    }
    
    /**
     * Construct detail URL with extracted IDs.
     */
    private fun constructDetailUrl(baseUrl: String, endpoint: ApiEndpoint, extractedIds: Map<String, String>): String {
        val cleanBase = baseUrl.trimEnd('/')
        var cleanPath = endpoint.path.removePrefix("/v3").let { 
            if (it.startsWith("/")) it else "/$it" 
        }
        
        // Replace path parameters with extracted IDs
        extractedIds.forEach { (key, value) ->
            cleanPath = cleanPath.replace("{$key}", value)
        }
        
        return "$cleanBase$cleanPath"
    }
    
    /**
     * Construct full URL from base and endpoint path.
     */
    private fun constructFullUrl(baseUrl: String, endpointPath: String): String {
        val cleanBase = baseUrl.trimEnd('/')
        val cleanPath = if (endpointPath.startsWith('/')) endpointPath else "/$endpointPath"
        return "$cleanBase$cleanPath"
    }
    
    /**
     * Test endpoint and return both verification and response body for ID extraction.
     */
    private fun testEndpointWithResponse(fullUrl: String, method: String): Pair<EndpointVerification, String?> {
        val startTime = System.currentTimeMillis()
        
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "MetaAgent-ApiVerificationHarness/1.0")
                .method(method.uppercase(), 
                    if (method.uppercase() in setOf("POST", "PUT", "PATCH")) 
                        HttpRequest.BodyPublishers.noBody() 
                    else HttpRequest.BodyPublishers.noBody())
                .build()
            
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val responseTime = System.currentTimeMillis() - startTime
            val status = analyzeResponseStatus(response.statusCode())
            
            val verification = EndpointVerification(
                status = status,
                statusCode = response.statusCode(),
                responseTimeMs = responseTime,
                errorMessage = null
            )
            
            Pair(verification, response.body())
            
        } catch (e: Exception) {
            val responseTime = System.currentTimeMillis() - startTime
            val verification = EndpointVerification(
                status = ApiAccessStatus.ERROR,
                statusCode = null,
                responseTimeMs = responseTime,
                errorMessage = e.message
            )
            Pair(verification, null)
        }
    }
    
    /**
     * Test actual endpoint connectivity and analyze response.
     */
    private fun testEndpoint(fullUrl: String, method: String): EndpointVerification {
        val startTime = System.currentTimeMillis()
        
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "MetaAgent-ApiVerificationHarness/1.0")
                .method(method.uppercase(), 
                    if (method.uppercase() in setOf("POST", "PUT", "PATCH")) 
                        HttpRequest.BodyPublishers.noBody() 
                    else HttpRequest.BodyPublishers.noBody())
                .build()
            
            val response = httpClient.send(request, HttpResponse.BodyHandlers.discarding())
            val responseTime = System.currentTimeMillis() - startTime
            
            val status = analyzeResponseStatus(response.statusCode())
            
            EndpointVerification(
                status = status,
                statusCode = response.statusCode(),
                responseTimeMs = responseTime,
                errorMessage = null
            )
            
        } catch (e: Exception) {
            val responseTime = System.currentTimeMillis() - startTime
            EndpointVerification(
                status = ApiAccessStatus.ERROR,
                statusCode = null,
                responseTimeMs = responseTime,
                errorMessage = e.message
            )
        }
    }
    
    /**
     * Analyze HTTP response status to determine API accessibility.
     */
    private fun analyzeResponseStatus(statusCode: Int): ApiAccessStatus {
        return when (statusCode) {
            in 200..299 -> ApiAccessStatus.PUBLICLY_ACCESSIBLE
            401, 403 -> ApiAccessStatus.REQUIRES_AUTH
            404 -> ApiAccessStatus.NOT_ACCESSIBLE
            429 -> ApiAccessStatus.REQUIRES_AUTH // Rate limited but exists
            in 500..599 -> ApiAccessStatus.ERROR
            else -> ApiAccessStatus.UNKNOWN
        }
    }
    
    /**
     * Extract business ID from search response JSON.
     */
    private fun extractBusinessId(responseBody: String): String? {
        return try {
            // Simple regex to find first "id" field value in JSON response
            val idPattern = """"id"\s*:\s*"([^"]+)"""".toRegex()
            val matchResult = idPattern.find(responseBody)
            matchResult?.groupValues?.get(1)
        } catch (e: Exception) {
            logger.warn("⚠️ Failed to extract business ID from response: ${e.message}")
            null
        }
    }
    
    /**
     * Create error result for failed verification.
     */
    private fun createErrorResult(endpoint: ApiEndpoint, baseUrl: String, error: Exception): ApiVerificationResult {
        return ApiVerificationResult(
            endpoint = endpoint,
            fullUrl = "$baseUrl${endpoint.path}",
            baseUrl = baseUrl,
            verification = EndpointVerification(
                status = ApiAccessStatus.ERROR,
                statusCode = null,
                responseTimeMs = 0,
                errorMessage = error.message
            )
        )
    }
    
    /**
     * Log summary of verification results.
     */
    private fun logVerificationSummary(results: List<ApiVerificationResult>) {
        val statusCounts = results.groupBy { it.verification.status }.mapValues { it.value.size }
        
        logger.info("📊 VERIFICATION SUMMARY:")
        statusCounts.forEach { (status, count) ->
            val icon = when (status) {
                ApiAccessStatus.PUBLICLY_ACCESSIBLE -> "🟢"
                ApiAccessStatus.REQUIRES_AUTH -> "🟡"
                ApiAccessStatus.NOT_ACCESSIBLE -> "🔴"
                ApiAccessStatus.ERROR -> "⚠️"
                ApiAccessStatus.UNKNOWN -> "❓"
            }
            logger.info("   $icon $status: $count endpoints")
        }
    }
}

/**
 * Result of API endpoint verification.
 */
data class ApiVerificationResult(
    val endpoint: ApiEndpoint,
    val fullUrl: String,
    val baseUrl: String,
    val verification: EndpointVerification
)

/**
 * Verification details for a single endpoint.
 */
data class EndpointVerification(
    val status: ApiAccessStatus,
    val statusCode: Int?,
    val responseTimeMs: Long,
    val errorMessage: String?
)

/**
 * API access status classification.
 */
enum class ApiAccessStatus {
    PUBLICLY_ACCESSIBLE,  // 2xx response - works without auth
    REQUIRES_AUTH,        // 401/403 - endpoint exists but needs auth
    NOT_ACCESSIBLE,       // 404 - endpoint not found/blocked
    ERROR,                // 5xx or network error
    UNKNOWN               // Unclear response
}