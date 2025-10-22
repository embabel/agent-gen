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

import jakarta.validation.constraints.NotBlank
import java.time.Instant

/**
 * API documentation analysis result from LLM.
 * 
 * Contains comprehensive analysis of API documentation including
 * Swagger/OpenAPI specs, developer documentation, and real-world usage patterns.
 */
data class ApiDocumentationAnalysis(
    @field:NotBlank(message = "Official API name cannot be blank")
    val officialName: String,
    
    val documentationUrl: String? = null,
    
    val swaggerUrl: String? = null,
    
    val apiStatus: ApiStatus,
    
    val endpoints: List<AnalyzedEndpoint> = emptyList(),
    
    val authenticationMethods: List<AuthenticationMethod> = emptyList(),
    
    val rateLimits: RateLimitInfo? = null,
    
    val dataModels: List<ApiDataModel> = emptyList(),
    
    val analysisTimestamp: Instant = Instant.now(),
    
    val analysisConfidence: Double = 0.0
)

/**
 * Individual API endpoint discovered through documentation analysis.
 */
data class AnalyzedEndpoint(
    val path: String,
    
    val method: HttpMethod,
    
    val operation: String,
    
    val description: String = "",
    
    val parameters: List<EndpointParameter> = emptyList(),
    
    val responseSchema: String? = null,
    
    val requiresAuthentication: Boolean = true,
    
    val actionCategories: Set<String> = emptySet()
)

/**
 * Authentication method details from API documentation.
 */
data class AuthenticationMethod(
    val type: AuthenticationScheme,
    
    val description: String = "",
    
    val setupComplexity: AuthSetupComplexity,
    
    val isRecommended: Boolean = false
)

/**
 * Rate limiting information from API documentation.
 */
data class RateLimitInfo(
    val requestsPerMinute: Int? = null,
    
    val requestsPerHour: Int? = null,
    
    val requestsPerDay: Int? = null,
    
    val burstLimit: Int? = null,
    
    val description: String = ""
)

/**
 * API data model/schema information.
 */
data class ApiDataModel(
    val name: String,
    
    val description: String = "",
    
    val fields: List<ModelField> = emptyList(),
    
    val usageContext: String = ""
)

/**
 * Individual field in an API data model.
 */
data class ModelField(
    val name: String,
    
    val type: String,
    
    val required: Boolean = true,
    
    val description: String = ""
)

/**
 * API status as determined from documentation analysis.
 */
enum class ApiStatus {
    /** API is active and well-maintained */
    ACTIVE,
    
    /** API is deprecated but still functional */
    DEPRECATED,
    
    /** API is in beta/preview state */
    BETA,
    
    /** API status could not be determined */
    UNKNOWN,
    
    /** API appears to be discontinued */
    DISCONTINUED
}

/**
 * Authentication setup complexity levels.
 */
enum class AuthSetupComplexity {
    /** Simple API key or basic auth */
    SIMPLE,
    
    /** OAuth2 or moderate setup required */
    MODERATE,
    
    /** Complex authentication flow */
    COMPLEX
}