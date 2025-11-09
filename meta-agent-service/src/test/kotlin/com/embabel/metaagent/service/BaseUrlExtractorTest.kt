/*
 * Copyright 2024-2025 Embabel Software, Inc.
 */
package com.embabel.metaagent.service

import com.embabel.agent.api.common.OperationContext
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Baby steps test: Extract base URLs from real API documentation.
 */
@SpringBootTest(classes = [MetaAgentApplication::class])
@ActiveProfiles("test")
class BaseUrlExtractorTest {

    @Autowired
    lateinit var baseUrlExtractor: BaseUrlExtractor

    @Autowired  
    lateinit var operationContext: OperationContext

    private val logger = LoggerFactory.getLogger(BaseUrlExtractorTest::class.java)

    @Test
    fun `extract base URLs from OpenTable docs`() {
        logger.info("🧪 Testing base URL extraction - OpenTable")
        
        val result = baseUrlExtractor.extractBaseUrls(
            url = "https://docs.opentable.com/", 
            context = operationContext
        )
        
        if (result != null) {
            logBaseUrlResults(result)
        } else {
            logger.error("❌ Failed to extract base URLs from OpenTable docs")
        }
    }
    
    @Test
    fun `extract base URLs from Yelp docs`() {
        logger.info("🧪 Testing base URL extraction - Yelp")
        
        val result = baseUrlExtractor.extractBaseUrls(
            url = "https://docs.developer.yelp.com/docs/places-intro",
            context = operationContext  
        )
        
        if (result != null) {
            logBaseUrlResults(result)
        } else {
            logger.error("❌ Failed to extract base URLs from Yelp docs")
        }
    }
    
    private fun logBaseUrlResults(result: BaseUrlResult) {
        logger.info("""
🌐 Base URL Extraction Results:
   Provider: ${result.provider}
   Documentation: ${result.documentationUrl}
   
📍 Found Base URLs:
   Production: ${result.baseUrls.production ?: "Not found"}
   Sandbox:    ${result.baseUrls.sandbox ?: "Not found"} 
   Test:       ${result.baseUrls.test ?: "Not found"}
   Demo:       ${result.baseUrls.demo ?: "Not found"}
        """.trimIndent())
    }
}