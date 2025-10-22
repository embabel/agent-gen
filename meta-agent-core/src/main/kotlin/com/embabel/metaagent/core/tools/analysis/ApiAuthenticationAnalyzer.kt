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

import com.embabel.metaagent.core.model.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Separate module for analyzing API authentication requirements.
 * 
 * Works with existing DiscoveredTool objects without modifying core discovery logic.
 * Adds authentication analysis as an enrichment step.
 */
@Component
class ApiAuthenticationAnalyzer {

    private val logger = LoggerFactory.getLogger(ApiAuthenticationAnalyzer::class.java)

    /**
     * Analyze authentication requirements from LLM suggestion data.
     * 
     * @param suggestion LLM tool suggestion with auth info
     * @return Authentication analysis or null if insufficient data
     */
    fun analyzeAuthentication(suggestion: LLMToolSuggestion): ApiAuthenticationAnalysis? {
        return try {
            val paymentRequired = suggestion.paymentRequired ?: false
            val freeTierAvailable = suggestion.freeTierAvailable ?: !paymentRequired
            val tokenMethod = parseTokenGenerationMethod(suggestion.tokenGenerationMethod)
            
            val analysis = ApiAuthenticationAnalysis(
                scheme = AuthenticationScheme.API_KEY, // Default for now
                complexity = determineComplexity(paymentRequired, tokenMethod),
                dynamicTokenGeneration = TokenGenerationCapability(
                    isSupported = tokenMethod != TokenGenerationMethod.MANUAL_ONLY,
                    method = tokenMethod,
                    automationLevel = determineAutomationLevel(tokenMethod, paymentRequired),
                    estimatedSetupTimeMinutes = estimateSetupTime(tokenMethod, paymentRequired)
                ),
                paymentRequired = paymentRequired,
                freeTierAvailable = freeTierAvailable,
                integrationScore = calculateIntegrationScore(paymentRequired, freeTierAvailable, tokenMethod),
                recommendationReason = buildRecommendationReason(paymentRequired, freeTierAvailable, tokenMethod)
            )
            
            logger.debug("🔐 Auth analysis for ${suggestion.name}: score=${analysis.integrationScore}, payment=$paymentRequired")
            analysis
            
        } catch (e: Exception) {
            logger.warn("Failed to analyze authentication for ${suggestion.name}: ${e.message}")
            null
        }
    }
    
    /**
     * Calculate integration score based on authentication factors.
     * Higher score = easier integration with minimal human intervention.
     */
    private fun calculateIntegrationScore(
        paymentRequired: Boolean, 
        freeTierAvailable: Boolean, 
        tokenMethod: TokenGenerationMethod?
    ): Double {
        var score = 1.0
        
        // Payment penalty
        if (paymentRequired && !freeTierAvailable) {
            score -= 0.7 // Major penalty for payment-only APIs
        } else if (paymentRequired && freeTierAvailable) {
            score -= 0.2 // Minor penalty for freemium APIs
        }
        
        // Token generation bonus/penalty
        when (tokenMethod) {
            TokenGenerationMethod.INSTANT_API_KEY -> score += 0.0 // Perfect
            TokenGenerationMethod.EMAIL_VERIFICATION -> score -= 0.1 // Slight penalty
            TokenGenerationMethod.OAUTH_AUTOMATED -> score -= 0.2 // OAuth complexity
            TokenGenerationMethod.MANUAL_ONLY -> score -= 0.5 // Major penalty
            null -> score -= 0.3 // Unknown method penalty
        }
        
        return score.coerceIn(0.0, 1.0)
    }
    
    private fun parseTokenGenerationMethod(methodString: String?): TokenGenerationMethod? {
        return when (methodString?.uppercase()) {
            "INSTANT_API_KEY" -> TokenGenerationMethod.INSTANT_API_KEY
            "EMAIL_VERIFICATION" -> TokenGenerationMethod.EMAIL_VERIFICATION
            "OAUTH_AUTOMATED" -> TokenGenerationMethod.OAUTH_AUTOMATED
            "MANUAL_ONLY" -> TokenGenerationMethod.MANUAL_ONLY
            else -> null
        }
    }
    
    private fun determineComplexity(paymentRequired: Boolean, tokenMethod: TokenGenerationMethod?): AuthSetupComplexity {
        return when {
            !paymentRequired && tokenMethod == TokenGenerationMethod.INSTANT_API_KEY -> AuthSetupComplexity.SIMPLE
            paymentRequired || tokenMethod == TokenGenerationMethod.MANUAL_ONLY -> AuthSetupComplexity.COMPLEX
            else -> AuthSetupComplexity.MODERATE
        }
    }
    
    private fun determineAutomationLevel(tokenMethod: TokenGenerationMethod?, paymentRequired: Boolean): AutomationLevel {
        return when {
            !paymentRequired && tokenMethod == TokenGenerationMethod.INSTANT_API_KEY -> AutomationLevel.FULLY_AUTOMATED
            paymentRequired || tokenMethod == TokenGenerationMethod.MANUAL_ONLY -> AutomationLevel.MANUAL_REQUIRED
            else -> AutomationLevel.MINIMAL_HUMAN
        }
    }
    
    private fun estimateSetupTime(tokenMethod: TokenGenerationMethod?, paymentRequired: Boolean): Int {
        return when {
            !paymentRequired && tokenMethod == TokenGenerationMethod.INSTANT_API_KEY -> 1
            tokenMethod == TokenGenerationMethod.EMAIL_VERIFICATION -> 5
            paymentRequired -> 15
            else -> 10
        }
    }
    
    private fun buildRecommendationReason(
        paymentRequired: Boolean, 
        freeTierAvailable: Boolean, 
        tokenMethod: TokenGenerationMethod?
    ): String {
        return when {
            !paymentRequired && tokenMethod == TokenGenerationMethod.INSTANT_API_KEY -> 
                "Excellent choice: Free API with instant key generation"
            !paymentRequired -> 
                "Good choice: Free API, setup may require verification"
            freeTierAvailable -> 
                "Acceptable: Has free tier available"
            else -> 
                "Consider alternatives: Requires payment for access"
        }
    }
}