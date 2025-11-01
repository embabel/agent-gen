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
package com.embabel.metaagent.core.tools.verification

import com.embabel.metaagent.core.tools.discovery.UrlCandidate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Stage 1B: URL Verification Service
 * 
 * Verifies the reachability of discovered URLs from Stage 1A.5.
 * Uses HTTP HEAD requests to check which documentation URLs are actually accessible,
 * filtering out fake fallback patterns and keeping only working sites.
 */
@Component
class UrlVerificationService {

    private val logger = LoggerFactory.getLogger(UrlVerificationService::class.java)
    
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    /**
     * Verify a list of URL candidates and return only the reachable ones.
     * 
     * @param urlCandidates List of URL candidates from Stage 1A.5
     * @param providerName The provider name for logging
     * @return List of verified URLs that are actually reachable
     */
    fun verifyUrls(urlCandidates: List<UrlCandidate>, providerName: String): List<VerifiedUrl> {
        logger.info("🔍 Stage 1B: Verifying ${urlCandidates.size} URLs for provider: $providerName")
        
        // Filter out generic URLs that don't contain provider name
        val relevantUrls = urlCandidates.filter { candidate ->
            isProviderSpecific(candidate.url, providerName)
        }
        
        val filteredCount = urlCandidates.size - relevantUrls.size
        if (filteredCount > 0) {
            logger.info("🗑️ Filtered out $filteredCount generic URLs not specific to $providerName")
        }
        
        val verifiedUrls = mutableListOf<VerifiedUrl>()
        
        relevantUrls.forEach { candidate ->
            logger.info("🌐 Testing URL: ${candidate.url}")
            
            val verification = verifyUrl(candidate)
            
            val statusIcon = when (verification.status) {
                UrlStatus.REACHABLE -> "✅"
                UrlStatus.NEEDS_AUTH -> "🔐"
                UrlStatus.NOT_FOUND -> "❌"
                UrlStatus.ERROR -> "⚠️"
            }
            
            logger.info("$statusIcon ${candidate.url} → ${verification.status} (HTTP ${verification.statusCode}, ${verification.responseTimeMs}ms)")
            if (verification.message.isNotBlank() && verification.status != UrlStatus.REACHABLE) {
                logger.info("   📝 ${verification.message}")
            }
            
            // Keep URLs that are reachable or need auth (valid endpoints)
            if (verification.status in setOf(UrlStatus.REACHABLE, UrlStatus.NEEDS_AUTH)) {
                verifiedUrls.add(
                    VerifiedUrl(
                        urlCandidate = candidate,
                        verification = verification,
                        adjustedConfidence = calculateAdjustedConfidence(candidate, verification),
                        authorityScore = calculateDomainAuthority(candidate.url, providerName)
                    )
                )
            }
        }
        
        logger.info("✅ Verified ${verifiedUrls.size}/${relevantUrls.size} provider-specific URLs for $providerName")
        return verifiedUrls
    }

    /**
     * Verify a single URL candidate using HTTP HEAD request.
     */
    private fun verifyUrl(candidate: UrlCandidate): UrlVerificationResult {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(candidate.url))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "MetaAgent-UrlVerification/1.0")
                .build()
            
            val startTime = System.currentTimeMillis()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.discarding())
            val responseTime = (System.currentTimeMillis() - startTime).toInt()
            
            val status = when (response.statusCode()) {
                200 -> UrlStatus.REACHABLE
                401, 403 -> UrlStatus.NEEDS_AUTH
                404 -> UrlStatus.NOT_FOUND
                in 300..399 -> UrlStatus.REACHABLE // Redirects are often valid
                in 500..599 -> UrlStatus.ERROR
                else -> UrlStatus.ERROR
            }
            
            UrlVerificationResult(
                status = status,
                statusCode = response.statusCode(),
                responseTimeMs = responseTime,
                message = "HTTP ${response.statusCode()}"
            )
            
        } catch (e: java.net.ConnectException) {
            UrlVerificationResult(
                status = UrlStatus.NOT_FOUND,
                statusCode = 0,
                responseTimeMs = 0,
                message = "Connection refused: ${e.message}"
            )
        } catch (e: java.net.http.HttpTimeoutException) {
            UrlVerificationResult(
                status = UrlStatus.ERROR,
                statusCode = 0,
                responseTimeMs = 10000,
                message = "Request timeout: ${e.message}"
            )
        } catch (e: Exception) {
            UrlVerificationResult(
                status = UrlStatus.ERROR,
                statusCode = 0,
                responseTimeMs = 0,
                message = "Network error: ${e.message}"
            )
        }
    }
    
    /**
     * Calculate adjusted confidence based on verification results.
     * Boost confidence for verified URLs, reduce for fallback patterns.
     */
    private fun calculateAdjustedConfidence(candidate: UrlCandidate, verification: UrlVerificationResult): Double {
        var adjustedConfidence = candidate.confidence
        
        // Boost confidence for verified URLs
        when (verification.status) {
            UrlStatus.REACHABLE -> adjustedConfidence += 0.3
            UrlStatus.NEEDS_AUTH -> adjustedConfidence += 0.2
            else -> adjustedConfidence -= 0.1
        }
        
        // Boost confidence for fast response times
        if (verification.responseTimeMs < 1000) {
            adjustedConfidence += 0.1
        }
        
        // Cap at 1.0
        return minOf(1.0, maxOf(0.0, adjustedConfidence))
    }
    
    /**
     * Check if URL is provider-specific (contains provider name or key components).
     */
    private fun isProviderSpecific(url: String, providerName: String): Boolean {
        val urlLower = url.lowercase()
        val providerLower = providerName.lowercase()
        
        // Simple approach: extract main words from provider name
        val providerWords = providerLower
            .replace(Regex("\\(.*?\\)"), "") // Remove parentheses content
            .split(Regex("[\\s-]+")) // Split on spaces and hyphens
            .filter { it.isNotBlank() && it.length > 2 } // Only keep meaningful words
        
        // URL must contain at least one provider word
        return providerWords.any { word -> urlLower.contains(word) }
    }
    
    /**
     * Calculate domain authority score for ranking.
     */
    private fun calculateDomainAuthority(url: String, providerName: String): Double {
        val domain = extractDomain(url)
        val providerLower = providerName.lowercase()
        
        return when {
            // Official docs domains
            domain.startsWith("docs.$providerLower") -> 1.0
            domain.startsWith("developer.$providerLower") -> 0.9
            domain.startsWith("api.$providerLower") -> 0.9
            
            // Provider's main domain
            domain.contains(".$providerLower.") || domain.endsWith(".$providerLower.com") -> 0.8
            
            // Known API tracking/reference sites
            domain.contains("apitracker.io") -> 0.7
            
            // Code repositories with provider name
            domain.contains("github.com") -> 0.6
            domain.contains("gist.github.com") -> 0.5
            
            // Everything else that passed provider filter
            else -> 0.4
        }
    }
    
    /**
     * Extract domain from URL.
     */
    private fun extractDomain(url: String): String {
        return try {
            java.net.URI(url).host?.lowercase() ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}

/**
 * URL verification result.
 */
data class UrlVerificationResult(
    val status: UrlStatus,
    val statusCode: Int,
    val responseTimeMs: Int,
    val message: String
)

/**
 * Verified URL with original candidate and verification results.
 */
data class VerifiedUrl(
    val urlCandidate: UrlCandidate,
    val verification: UrlVerificationResult,
    val adjustedConfidence: Double,
    val authorityScore: Double
)

/**
 * URL verification status.
 */
enum class UrlStatus {
    REACHABLE,      // 200 - URL is accessible
    NEEDS_AUTH,     // 401/403 - URL exists but needs authentication
    NOT_FOUND,      // 404 - URL not found
    ERROR           // Other errors (timeouts, 5xx, etc.)
}