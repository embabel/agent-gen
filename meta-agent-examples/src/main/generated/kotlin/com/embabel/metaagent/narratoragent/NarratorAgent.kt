package com.embabel.metaagent.narratoragent

import com.embabel.agent.api.annotation.*
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.domain.io.UserInput
import org.slf4j.LoggerFactory

@Agent(description = "Narrator Agent that rewrites markdown content as plain text suitable for text-to-speech")
class NarratorAgent {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Action(description = "Remove markdown formatting from the input text and compute word count", cost = 1.0, value = 0.8)
    @ToolGroup("default")
    fun removeMarkdownAndCount(input: UserInput, context: OperationContext): PlainTextWithWordCount {
        logger.info("removeMarkdownAndCount called with: ${input.content}")
        // Remove markdown formatting (headers, bold, italic, links, lists)
        var text = input.content

        // Remove headers (#, ##, ###, etc.)
        text = text.replace(Regex("(?m)^#{1,6}\\s*"), "")
        // Remove bold (**text** or __text__)
        text = text.replace(Regex("(\\*\\*|__)(.*?)\\1"), "$2")
        // Remove italic (*text* or _text_)
        text = text.replace(Regex("(\\*|_)(.*?)\\1"), "$2")
        // Remove inline links [text](url)
        text = text.replace(Regex("\\[(.*?)\\]\\((.*?)\\)"), "$1")
        // Remove images ![alt](url)
        text = text.replace(Regex("!\\[(.*?)\\]\\((.*?)\\)"), "")
        // Remove unordered lists markers (-, *, +) at line starts
        text = text.replace(Regex("(?m)^\\s*[-*+]\\s+"), "")
        // Remove ordered lists markers (1., 2., ...)
        text = text.replace(Regex("(?m)^\\s*\\d+\\.\\s+"), "")
        // Remove blockquotes
        text = text.replace(Regex("(?m)^>\\s*"), "")
        // Remove inline code `code`
        text = text.replace(Regex("`([^`]*)`"), "$1")
        // Remove code blocks ```code```
        text = text.replace(Regex("(?s)```.*?```"), "")
        // Remove remaining markdown symbols
        text = text.replace(Regex("[*_~`]"), "")
        // Normalize spaces
        text = text.trim().replace(Regex("\\s+"), " ")

        val wordCount = text.split(Regex("\\s+")).filter { it.isNotEmpty() }.size
        return PlainTextWithWordCount(text, wordCount)
    }

    @Action(description = "Decide whether to summarize or keep full plain text based on word count", cost = 1.0, value = 0.8)
    @ToolGroup("default")
    fun analyzeAndPrepare(input: PlainTextWithWordCount): Preparation {
        logger.info("analyzeAndPrepare called with: wordCount=${input.wordCount}")
        val exceedsLimit = input.wordCount > 350
        return Preparation(input.plainText, input.wordCount, exceedsLimit)
    }

    @AchievesGoal(description = "Produce plain text suitable for text-to-speech with optional summarization aligning with content length")
    @Action(description = "Summarize if content exceeds 350 words or faithfully convert plain text for shorter content", cost = 1.0, value = 0.8)
    @ToolGroup("default")
    fun finalizeText(preparation: Preparation): NarratorOutput {
        logger.info("finalizeText called with: exceedsLimit=${preparation.exceedsLimit}, wordCount=${preparation.wordCount}")
        val text: String
        val message: String

        if (preparation.exceedsLimit) {
            // Approximate target summary length as 30% of original word count or about 80 words minimum
            val targetWords = maxOf(80, (preparation.wordCount * 0.3).toInt())
            // Simplified summary logic (in reality, use an NLP model)
            val words = preparation.plainText.split(Regex("\\s+"))
            val summaryWords = words.take(targetWords)
            text = summaryWords.joinToString(" ").trim() + "..."
            message = "Content exceeds 350 words. Summarized main points here. Detailed information remains visible on screen."
        } else {
            text = preparation.plainText
            message = "Full text provided without summarization."
        }
        return NarratorOutput(text, message)
    }
}

data class PlainTextWithWordCount(val plainText: String, val wordCount: Int)
data class Preparation(val plainText: String, val wordCount: Int, val exceedsLimit: Boolean)
data class NarratorOutput(val plainText: String, val userMessage: String)