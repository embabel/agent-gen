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
package com.embabel.metaagent.core.model

/**
 * Simplified authentication and payment analysis for APIs.
 * 
 * Focuses on the key factors for minimal human intervention:
 * payment requirements, automation capabilities, and integration complexity.
 */
data class ApiAuthenticationAnalysis(
    val scheme: AuthenticationScheme,
    
    val complexity: AuthSetupComplexity,
    
    val dynamicTokenGeneration: TokenGenerationCapability,
    
    val paymentRequired: Boolean,
    
    val freeTierAvailable: Boolean,
    
    val integrationScore: Double,
    
    val recommendationReason: String = ""
)

/**
 * Dynamic token generation capabilities for minimal human intervention.
 */
data class TokenGenerationCapability(
    val isSupported: Boolean,
    
    val method: TokenGenerationMethod?,
    
    val automationLevel: AutomationLevel,
    
    val estimatedSetupTimeMinutes: Int = 0
)

/**
 * Token generation methods for automation.
 */
enum class TokenGenerationMethod {
    /** API key generated instantly without human verification */
    INSTANT_API_KEY,
    
    /** Requires email verification but can be automated */
    EMAIL_VERIFICATION,
    
    /** OAuth2 with automatic token refresh */
    OAUTH_AUTOMATED,
    
    /** Requires manual intervention */
    MANUAL_ONLY
}

/**
 * Level of automation possible for setup.
 */
enum class AutomationLevel {
    /** Fully automated - zero human intervention */
    FULLY_AUTOMATED,
    
    /** Minimal human steps - one-click setup */
    MINIMAL_HUMAN,
    
    /** Manual setup required */
    MANUAL_REQUIRED
}