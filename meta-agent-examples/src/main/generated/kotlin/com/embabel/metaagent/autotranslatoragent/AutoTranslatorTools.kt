package com.embabel.metaagent.autotranslatoragent

import com.embabel.agent.api.annotation.LlmTool
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AutoTranslatorTools(
    @Value("\${API_KEY:}") private val apiKey: String = "",
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @LlmTool(description = "Remove markdown")
    fun removeMarkdown(input: String, context: String): String {
        logger.info("removeMarkdown called")
        // TODO: Implement API call / web scraping
        TODO("Implement removeMarkdown")
    }

    @LlmTool(description = "Summarize if needed")
    fun summarizeIfNeeded(plainText: String): String {
        logger.info("summarizeIfNeeded called")
        // TODO: Implement API call / web scraping
        TODO("Implement summarizeIfNeeded")
    }

    @LlmTool(description = "Produce output")
    fun produceOutput(summarizedText: String): String {
        logger.info("produceOutput called")
        // TODO: Implement API call / web scraping
        TODO("Implement produceOutput")
    }
}
