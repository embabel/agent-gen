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
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Integration test for API verification harness with real endpoint testing.
 */
@SpringBootTest(classes = [MetaAgentApplication::class])
@ActiveProfiles("test")
class ApiVerificationHarnessTest {

    @Autowired
    lateinit var operationContext: OperationContext

    private val apiVerificationHarness = ApiVerificationHarness()
    private val llmApiExtractor = LlmApiExtractor()
    private val baseUrlExtractor = BaseUrlExtractor()
    private val logger = LoggerFactory.getLogger(ApiVerificationHarnessTest::class.java)

    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            System.setProperty("embabel.agent.shell.interactive.enabled", "false")
        }
    }

    @Test
    fun `test API verification harness with Yelp endpoints`() {
        logger.info("""
🧪 ========== API VERIFICATION HARNESS TEST ==========
🎯 Goal: Test real API endpoint accessibility
📋 Method: Extract APIs + Base URLs, then verify connectivity
        """.trimIndent())
        
        // Step 1: Extract Yelp API endpoints
        val yelpDocUrl = "https://docs.developer.yelp.com/docs/places-intro"
        logger.info("🔍 Extracting Yelp API endpoints from: $yelpDocUrl")
        
        val extractedApis = llmApiExtractor.extractFromUrl(yelpDocUrl, operationContext)
        logger.info("📋 Extracted ${extractedApis?.endpoints?.size ?: 0} Yelp endpoints")
        
        // Step 2: Extract Yelp base URLs
        logger.info("🔗 Extracting Yelp base URLs...")
        val baseUrls = baseUrlExtractor.extractBaseUrls(yelpDocUrl, operationContext)
        logger.info("🌐 Base URL found: ${baseUrls?.baseUrls?.production ?: "None"}")
        
        // Step 3: Verify the endpoints
        if (extractedApis != null && baseUrls != null) {
            logger.info("🧪 Starting endpoint verification...")
            
            val verificationResults = apiVerificationHarness.verifyApiEndpoints(
                extractedApis = extractedApis,
                baseUrls = baseUrls.baseUrls,
                context = operationContext
            )
            
            // Log detailed results
            val report = buildVerificationReport(verificationResults)
            logger.info(report)
            
            // Analysis and assertions
            val analysis = analyzeVerificationResults(verificationResults)
            logger.info(analysis)
            
            // Assertions
            assert(verificationResults.isNotEmpty()) { "Should have verification results" }
            
            // Check URLs (allow "SKIPPED" URLs for endpoints that need extracted IDs)
            val invalidUrls = verificationResults.filter { 
                !it.fullUrl.startsWith("http") && !it.fullUrl.startsWith("SKIPPED")
            }
            assert(invalidUrls.isEmpty()) { "Some URLs are malformed: ${invalidUrls.map { it.fullUrl }}" }
            
            logger.info("✅ API verification harness test completed successfully!")
            
        } else {
            logger.error("❌ Failed to extract APIs or base URLs for verification")
            assert(false) { "Could not extract required data for verification" }
        }
    }
    
    private fun buildVerificationReport(results: List<ApiVerificationResult>): String {
        return buildString {
            appendLine("\n📊 API VERIFICATION RESULTS:")
            appendLine("=" .repeat(80))
            
            results.forEachIndexed { index, result ->
                val statusIcon = when (result.verification.status) {
                    ApiAccessStatus.PUBLICLY_ACCESSIBLE -> "🟢"
                    ApiAccessStatus.REQUIRES_AUTH -> "🟡"
                    ApiAccessStatus.NOT_ACCESSIBLE -> "🔴"
                    ApiAccessStatus.ERROR -> "⚠️"
                    ApiAccessStatus.UNKNOWN -> "❓"
                }
                
                appendLine("${index + 1}. $statusIcon ${result.endpoint.method.uppercase()} ${result.endpoint.path}")
                appendLine("   📍 URL: ${result.fullUrl}")
                appendLine("   📊 Status: ${result.verification.status} (${result.verification.statusCode ?: "N/A"})")
                appendLine("   ⏱️ Time: ${result.verification.responseTimeMs}ms")
                result.verification.errorMessage?.let { 
                    appendLine("   ❌ Error: $it") 
                }
                appendLine("   📝 ${result.endpoint.description}")
                appendLine()
            }
            append("=" .repeat(80))
        }
    }
    
    private fun analyzeVerificationResults(results: List<ApiVerificationResult>): String {
        val accessibleCount = results.count { it.verification.status == ApiAccessStatus.PUBLICLY_ACCESSIBLE }
        val authRequiredCount = results.count { it.verification.status == ApiAccessStatus.REQUIRES_AUTH }
        val notAccessibleCount = results.count { it.verification.status == ApiAccessStatus.NOT_ACCESSIBLE }
        val errorCount = results.count { it.verification.status == ApiAccessStatus.ERROR }
        
        val toolCandidates = results.filter { 
            it.verification.status in setOf(
                ApiAccessStatus.PUBLICLY_ACCESSIBLE,
                ApiAccessStatus.REQUIRES_AUTH
            )
        }
        
        return buildString {
            appendLine("\n🎯 ACCESSIBILITY ANALYSIS:")
            appendLine("   🟢 Publicly accessible: $accessibleCount")
            appendLine("   🟡 Requires authentication: $authRequiredCount") 
            appendLine("   🔴 Not accessible: $notAccessibleCount")
            appendLine("   ⚠️ Errors: $errorCount")
            appendLine()
            appendLine("🔧 TOOL vs ADVICE DECISION:")
            appendLine("   ✅ Tool candidates: ${toolCandidates.size} endpoints")
            appendLine("   💡 Advice candidates: ${results.size - toolCandidates.size} endpoints")
            
            if (toolCandidates.isNotEmpty()) {
                appendLine("\n🛠️ ENDPOINTS FOR @Tool GENERATION:")
                toolCandidates.forEach { result ->
                    appendLine("   → ${result.endpoint.method.uppercase()} ${result.endpoint.path}")
                }
            }
        }
    }
}