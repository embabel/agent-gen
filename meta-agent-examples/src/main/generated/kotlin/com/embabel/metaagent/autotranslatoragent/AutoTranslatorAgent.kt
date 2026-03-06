package com.embabel.metaagent.autotranslatoragent

import com.embabel.agent.api.annotation.*
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.domain.io.UserInput
import org.slf4j.LoggerFactory

@Agent(description = "Rewrites markdown content into plain text suitable for text-to-speech applications by removing formatting and summarizing if needed")
class AutoTranslatorAgent {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Action(description = "Remove all markdown formatting from input text", cost = 1.0, value = 0.8)
    @ToolGroup("default")
    fun removeMarkdown(input: UserInput, context: OperationContext): PlainText {
        logger.info("removeMarkdown called with: ${input.content}")
        val plain = input.content
            .replace(Regex("""(?m)^#{1,6}\s*"""), "")           // headers
            .replace(Regex("""(\*\*|__)(.*?)\1"""), "$2")       // bold
            .replace(Regex("""(\*|_)(.*?)\1"""), "$2")          // italic
            .replace(Regex("""\[(.*?)\]\(.*?\)"""), "$1")       // links
            .replace(Regex("""(?m)^\s*([-*+]|\d+\.)\s+"""), "") // lists
            .replace(Regex("`{1,3}.*?`{1,3}"), "")              // inline code blocks
            .replace(Regex(""">\s*"""), "")                      // blockquotes
            .replace("\n\n", "\n")
            .trim()
        return PlainText(plain)
    }

    @Action(description = "Summarize content if exceeding 350 words, else preserve original plain text", cost = 1.0, value = 0.8)
    @ToolGroup("default")
    fun summarizeIfNeeded(plainText: PlainText): SummarizedText {
        logger.info("summarizeIfNeeded called with text length: ${plainText.text.split("\\s+".toRegex()).size} words")
        val words = plainText.text.split("\\s+".toRegex())
        return if (words.size > 350) {
            val targetCount = 150.coerceAtMost(words.size / 2)
            val summary = words.take(targetCount).joinToString(" ") + "..."
            SummarizedText(
                summary = summary,
                detailedAvailableNotice = "Note: Detailed content remains available on screen."
            )
        } else {
            SummarizedText(
                summary = plainText.text,
                detailedAvailableNotice = ""
            )
        }
    }

    @AchievesGoal(description = "Provide plain text or summarized output without markdown formatting, with user notification if summarized")
    @Action(description = "Deliver final plain text suitable for text-to-speech with summary notice if applicable", cost = 1.0, value = 0.8)
    @ToolGroup("default")
    fun produceOutput(summarizedText: SummarizedText): FinalOutput {
        logger.info("produceOutput called with summary size: ${summarizedText.summary.split("\\s+".toRegex()).size} words")
        val outputText = if (summarizedText.detailedAvailableNotice.isNotEmpty()) {
            "${summarizedText.summary}\n\n${summarizedText.detailedAvailableNotice}"
        } else {
            summarizedText.summary
        }
        return FinalOutput(outputText)
    }
}

data class PlainText(val text: String)
data class SummarizedText(val summary: String, val detailedAvailableNotice: String)
data class FinalOutput(val text: String)