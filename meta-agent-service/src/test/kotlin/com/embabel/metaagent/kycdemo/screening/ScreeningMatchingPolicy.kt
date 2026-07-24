package com.embabel.metaagent.kycdemo.screening

/**
 * Configurable AML name-screening policy.
 *
 * The defaults are demo calibration values, not regulatory constants. Regulators
 * and industry guidance require fuzzy matching, alias handling, identifier
 * comparison, calibration, and human review, but they do not prescribe universal
 * numeric thresholds. A real service should externalize these values and tune them
 * against validation data, source coverage, false-positive tolerance, and
 * jurisdiction-specific risk appetite.
 */
data class ScreeningMatchingPolicy(
    val possibleMatchNameThreshold: Double = 0.72,
    val confirmedNameThreshold: Double = 0.95,
    val confirmedIdentifierThreshold: Double = 0.85,
    val nameConfidenceWeight: Double = 0.65,
    val identifierConfidenceWeight: Double = 0.35,
    val noComparableIdentifiersScore: Double = 0.5,
    val falsePositiveConflictCount: Int = 2,
    val noiseWords: Set<String> = defaultNameNoiseWords,
) {
    companion object {
        fun demoDefaults(): ScreeningMatchingPolicy =
            ScreeningMatchingPolicy()
    }

    init {
        require(possibleMatchNameThreshold in 0.0..1.0)
        require(confirmedNameThreshold in 0.0..1.0)
        require(confirmedIdentifierThreshold in 0.0..1.0)
        require(nameConfidenceWeight >= 0.0)
        require(identifierConfidenceWeight >= 0.0)
        require(nameConfidenceWeight + identifierConfidenceWeight > 0.0)
        require(noComparableIdentifiersScore in 0.0..1.0)
        require(falsePositiveConflictCount >= 1)
    }
}
