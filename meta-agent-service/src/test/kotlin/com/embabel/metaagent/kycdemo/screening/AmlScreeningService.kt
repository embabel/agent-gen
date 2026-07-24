package com.embabel.metaagent.kycdemo.screening

class AmlScreeningService(
    private val providers: List<NamedScreeningProvider>,
    private val matcher: ScreeningMatcher = ScreeningMatcher(),
) {

    init {
        val configuredSources = providers.map { it.source }.toSet()
        val missingSources = ScreeningMethodology.requiredSanctionsSources - configuredSources
        require(missingSources.isEmpty()) {
            "AML screening MVP requires providers for ${ScreeningMethodology.requiredSanctionsSources}; missing $missingSources"
        }
    }

    fun screen(subject: ScreeningSubject): ScreeningAssessment {
        val hits = providers.flatMap { provider ->
            provider.search(subject).mapNotNull { entry ->
                matcher.match(subject, provider.providerId, entry)
            }
        }.sortedWith(
            compareByDescending<ScreeningHit> { it.disposition.priority() }
                .thenByDescending { it.confidenceScore }
                .thenBy { it.source.name },
        )

        return ScreeningAssessment(
            subject = subject,
            status = hits.assessmentStatus(),
            hits = hits,
        )
    }

    private fun List<ScreeningHit>.assessmentStatus(): ScreeningAssessmentStatus =
        when {
            any { it.disposition == ScreeningDisposition.CONFIRMED_MATCH } -> ScreeningAssessmentStatus.REJECT
            any { it.disposition == ScreeningDisposition.POSSIBLE_MATCH } -> ScreeningAssessmentStatus.MANUAL_REVIEW_REQUIRED
            else -> ScreeningAssessmentStatus.CLEAR
        }

    private fun ScreeningDisposition.priority(): Int =
        when (this) {
            ScreeningDisposition.CONFIRMED_MATCH -> 3
            ScreeningDisposition.POSSIBLE_MATCH -> 2
            ScreeningDisposition.LIKELY_FALSE_POSITIVE -> 1
        }
}
