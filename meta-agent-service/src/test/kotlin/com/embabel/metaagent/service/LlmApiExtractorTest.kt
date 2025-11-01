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
 * Integration test for LLM-based API extraction with real LLM context.
 */
@SpringBootTest(classes = [MetaAgentApplication::class])
@ActiveProfiles("test")
class LlmApiExtractorTest {

    @Autowired
    lateinit var operationContext: OperationContext

    private val llmApiExtractor = LlmApiExtractor()
    private val logger = LoggerFactory.getLogger(LlmApiExtractorTest::class.java)

    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            // Set shell configuration to non-interactive mode
            System.setProperty("embabel.agent.shell.interactive.enabled", "false")
        }
    }

    @Test
    fun `test LLM extraction from OpenTable docs - Option 1 URL`() {
        val url = "https://docs.opentable.com/"
        
        logger.info("🌐 Testing Option 1: LLM extraction from URL: $url")
        
        val apiDoc = llmApiExtractor.extractFromUrl(url, operationContext)
        
        if (apiDoc != null) {
            logApiDocumentation(apiDoc, "Option 1 (URL)")
        } else {
            logger.info("⚠️ Option 1 (URL extraction) not supported or failed")
        }
    }

    @Test  
    fun `test LLM extraction from OpenTable docs - Option 2 Document`() {
        val url = "https://docs.opentable.com/"
        
        logger.info("📄 Testing Option 2: LLM extraction from document: $url")
        
        val apiDoc = llmApiExtractor.extractFromDocument(url, operationContext)
        
        if (apiDoc != null) {
            logApiDocumentation(apiDoc, "Option 2 (Document)")
        } else {
            logger.error("❌ Option 2 (Document extraction) failed")
        }
    }
    
    @Test
    fun `test LLM extraction from Yelp docs`() {
        val url = "https://docs.developer.yelp.com/docs/places-intro"
        
        logger.info("📄 Testing LLM extraction from Yelp docs: $url")
        
        val apiDoc = llmApiExtractor.extractFromDocument(url, operationContext)
        
        if (apiDoc != null) {
            logApiDocumentation(apiDoc, "Yelp Docs")
        } else {
            logger.error("❌ Yelp docs extraction failed")
        }
    }
    
    private fun logApiDocumentation(apiDoc: ApiDocumentation, source: String) {
        logger.info("""
📋 $source - API Documentation Summary:
   Title: ${apiDoc.title}
   URL: ${apiDoc.url}
   Total Endpoints: ${apiDoc.endpoints.size}
        """.trimIndent())
        
        if (apiDoc.endpoints.isNotEmpty()) {
            logger.info("\n🎯 Found API Endpoints:")
            apiDoc.endpoints.forEach { endpoint ->
                logger.info("   ${endpoint.method} ${endpoint.path}")
                
                if (endpoint.description != null) {
                    logger.info("      📝 ${endpoint.description}")
                }
                
                if (endpoint.parameters.isNotEmpty()) {
                    logger.info("      📋 Parameters:")
                    endpoint.parameters.forEach { param ->
                        val requiredFlag = if (param.required) "required" else "optional"
                        logger.info("         - ${param.name}: ${param.type} ($requiredFlag)")
                        if (param.description != null) {
                            logger.info("           ${param.description}")
                        }
                    }
                }
                logger.info("")
            }
        } else {
            logger.info("⚠️ No API endpoints found by LLM")
        }
        
        logger.info("✅ $source extraction completed")
    }
}