package com.embabel.metaagent.kycdemo.screening

import com.embabel.metaagent.kycdemo.RiskLevel

/**
 * Demo AML risk methodology used after deterministic screening and LLM
 * disposition. These values are explainable demo policy settings, not regulatory
 * constants; production systems should externalize them and validate them against
 * screening performance, source coverage, jurisdiction, and risk appetite.
 */
object AmlRiskMethodology {

    const val highRiskConfidenceThreshold = 0.6
    const val mediumRiskConfidenceThreshold = 0.4

    const val rejectCadence = "before any override request and at each list update"
    const val highRiskCadence =
        "every 3 months until AML disposition is cleared; then follow high-risk periodic review schedule"
    const val mediumRiskCadence = "every 6 months"
    const val lowRiskCadence = "every 12 months"
    const val unknownRiskCadence = "manual cadence required because risk remains unknown"

    fun sanctionsRiskLevel(candidate: AmlLogicalCandidateAssessment): RiskLevel =
        when {
            candidate.recommendedDisposition == ScreeningDisposition.CONFIRMED_MATCH -> RiskLevel.HIGH
            candidate.confidenceScore >= highRiskConfidenceThreshold -> RiskLevel.HIGH
            candidate.confidenceScore >= mediumRiskConfidenceThreshold -> RiskLevel.MEDIUM
            else -> RiskLevel.UNKNOWN
        }
}
