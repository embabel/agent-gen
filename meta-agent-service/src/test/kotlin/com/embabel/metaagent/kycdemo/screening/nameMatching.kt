package com.embabel.metaagent.kycdemo.screening

import java.text.Normalizer
import kotlin.math.max
import kotlin.math.min

fun nameSimilarity(left: String, right: String, policy: ScreeningMatchingPolicy = ScreeningMatchingPolicy.demoDefaults()): Double {
    val leftTokens = left.normalizedNameTokens(policy.noiseWords)
    val rightTokens = right.normalizedNameTokens(policy.noiseWords)
    if (leftTokens.isEmpty() || rightTokens.isEmpty()) {
        return 0.0
    }

    val tokenScore = leftTokens.intersect(rightTokens).size.toDouble() /
        leftTokens.union(rightTokens).size.toDouble()
    val editScore = normalizedLevenshtein(leftTokens.joinToString(" "), rightTokens.joinToString(" "))
    return max(tokenScore, editScore)
}

val defaultNameNoiseWords = setOf(
    "limited",
    "ltd",
    "inc",
    "corp",
    "corporation",
    "company",
    "co",
    "plc",
    "llc",
)

private fun String.normalizedNameTokens(noiseWords: Set<String>): Set<String> =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase()
        .replace(Regex("[^a-z0-9\\s]+"), " ")
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .filterNot { it in noiseWords }
        .toSet()

private fun normalizedLevenshtein(left: String, right: String): Double {
    val distance = levenshtein(left, right)
    val maxLength = max(left.length, right.length)
    if (maxLength == 0) {
        return 1.0
    }
    return 1.0 - min(distance.toDouble() / maxLength.toDouble(), 1.0)
}

private fun levenshtein(left: String, right: String): Int {
    val previous = IntArray(right.length + 1) { it }
    val current = IntArray(right.length + 1)

    left.forEachIndexed { leftIndex, leftChar ->
        current[0] = leftIndex + 1
        right.forEachIndexed { rightIndex, rightChar ->
            val substitutionCost = if (leftChar == rightChar) 0 else 1
            current[rightIndex + 1] = minOf(
                current[rightIndex] + 1,
                previous[rightIndex + 1] + 1,
                previous[rightIndex] + substitutionCost,
            )
        }
        current.copyInto(previous)
    }

    return previous[right.length]
}
