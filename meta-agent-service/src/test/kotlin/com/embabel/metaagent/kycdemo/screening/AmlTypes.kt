package com.embabel.metaagent.kycdemo.screening

import java.time.Instant
import java.time.LocalDate

data class ScreeningSubject(
    val id: String,
    val displayName: String,
    val subjectType: ScreeningSubjectType,
    val dateOfBirth: LocalDate? = null,
    val countries: Set<String> = emptySet(),
    val identifiers: Set<ScreeningIdentifier> = emptySet(),
)

enum class ScreeningSubjectType {
    PERSON,
    LEGAL_ENTITY,
}

data class ScreeningIdentifier(
    val type: ScreeningIdentifierType,
    val value: String,
    val issuingCountry: String? = null,
)

enum class ScreeningIdentifierType {
    PASSPORT,
    NATIONAL_ID,
    TAX_ID,
    COMPANY_REGISTRATION_NUMBER,
    OTHER,
}

data class ScreeningListEntry(
    val source: ScreeningSource,
    val sourceRecordId: String,
    val primaryName: String,
    val aliases: Set<String> = emptySet(),
    val entryType: ScreeningSubjectType,
    val dateOfBirth: LocalDate? = null,
    val countries: Set<String> = emptySet(),
    val addresses: Set<String> = emptySet(),
    val identifiers: Set<ScreeningIdentifier> = emptySet(),
    val programs: Set<String> = emptySet(),
    val sourceUrl: String,
)

enum class ScreeningSource {
    EU_FINANCIAL_SANCTIONS,
    OFAC_SANCTIONS,
    UN_SANCTIONS,
    UK_SANCTIONS,
    PEP_WEB_RESEARCH,
    ADVERSE_MEDIA,
    COMPANY_REGISTRY,
    INTERNAL_DOCUMENTS,
}

data class ScreeningHit(
    val providerId: String,
    val source: ScreeningSource,
    val sourceRecordId: String,
    val matchedName: String,
    val primaryName: String,
    val nameScore: Double,
    val identifierScore: Double,
    val confidenceScore: Double,
    val countries: Set<String> = emptySet(),
    val addresses: Set<String> = emptySet(),
    val disposition: ScreeningDisposition,
    val rationale: String,
    val sourceUrl: String,
)

enum class ScreeningDisposition {
    LIKELY_FALSE_POSITIVE,
    POSSIBLE_MATCH,
    CONFIRMED_MATCH,
}

data class ScreeningAssessment(
    val subject: ScreeningSubject,
    val status: ScreeningAssessmentStatus,
    val hits: List<ScreeningHit>,
    val screenedAt: Instant = Instant.now(),
    val methodology: String = ScreeningMethodology.description,
)

enum class ScreeningAssessmentStatus {
    CLEAR,
    MANUAL_REVIEW_REQUIRED,
    REJECT,
}

data class AmlLogicalScreeningAssessment(
    val subjectName: String,
    val overallStatus: ScreeningAssessmentStatus,
    val candidateAssessments: List<AmlLogicalCandidateAssessment>,
    val rationale: String,
    val analystAction: String,
)

data class AmlLogicalCandidateAssessment(
    val sourceRecordId: String,
    val candidateName: String,
    val confidenceScore: Double,
    val confidenceLevel: AmlLlmConfidenceLevel,
    val recommendedDisposition: ScreeningDisposition,
    val rationale: String,
    val missingEvidence: List<String>,
)

enum class AmlLlmConfidenceLevel {
    LOW,
    MEDIUM,
    HIGH,
}

object ScreeningMethodology {
    val requiredSanctionsSources = setOf(
        ScreeningSource.EU_FINANCIAL_SANCTIONS,
        ScreeningSource.OFAC_SANCTIONS,
    )

    const val description =
        "MVP AML screening searches at least EU Financial Sanctions and OFAC sanctions sources, " +
            "keeps candidate hits separate from final officer decisions, and uses fuzzy name matching plus " +
            "available identifiers such as date of birth, country, passport, and registration number to classify " +
            "candidate disposition as likely false positive, possible match, or confirmed match."
}
