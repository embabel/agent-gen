package com.embabel.metaagent.kycdemo

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

interface KycExtractor {
    fun extract(documents: List<SourceDocument>): KycCase
}

data class SourceDocument(
    val fileName: String,
    val type: DocumentType,
    val text: String,
)

enum class DocumentType {
    PASSPORT,
    NATIONAL_ID,
    PROOF_OF_ADDRESS,
    CERTIFICATE_OF_INCORPORATION,
    SHAREHOLDER_REGISTER,
    ARTICLES_OF_ASSOCIATION,
    TAX_DOCUMENT,
    SOURCE_OF_FUNDS,
    SOURCE_OF_WEALTH,
    PEP_DECLARATION,
    KYC_QUESTIONNAIRE,
    OTHER,
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "partyType")
@JsonSubTypes(
    JsonSubTypes.Type(value = Person::class, name = "person"),
    JsonSubTypes.Type(value = LegalEntity::class, name = "legalEntity"),
)
sealed interface Party {
    val id: String
    val displayName: String
    val roles: Set<PartyRole>
}

data class Person(
    override val id: String,
    override val displayName: String,
    val dateOfBirth: LocalDate? = null,
    val nationalities: Set<String> = emptySet(),
    val addresses: List<Address> = emptyList(),
    override val roles: Set<PartyRole> = emptySet(),
    val pepProfile: PepProfile? = null,
) : Party

data class LegalEntity(
    override val id: String,
    override val displayName: String,
    val registrationNumber: String? = null,
    val jurisdictionOfIncorporation: String? = null,
    val incorporationDate: LocalDate? = null,
    val addresses: List<Address> = emptyList(),
    override val roles: Set<PartyRole> = emptySet(),
) : Party

enum class PartyRole {
    CUSTOMER,
    DIRECTOR,
    BENEFICIAL_OWNER,
    AUTHORIZED_SIGNATORY,
    POLITICALLY_EXPOSED_PERSON,
    FAMILY_MEMBER_OF_PEP,
    CLOSE_ASSOCIATE_OF_PEP,
}

data class Address(
    val line1: String,
    val line2: String? = null,
    val city: String,
    val region: String? = null,
    val postalCode: String,
    val countryCode: String,
)

data class PepProfile(
    val category: PepCategory,
    val publicOffice: String? = null,
    val organization: String? = null,
    val countryCode: String? = null,
    val fromDate: LocalDate? = null,
    val toDate: LocalDate? = null,
    val current: Boolean,
    val seniorPublicFigure: Boolean,
    val evidence: EvidenceReference? = null,
)

enum class PepCategory {
    DOMESTIC,
    FOREIGN,
    INTERNATIONAL_ORGANIZATION,
    FAMILY_MEMBER,
    CLOSE_ASSOCIATE,
}

data class OwnershipInterest(
    val ownerPartyId: String,
    val ownedPartyId: String,
    val directPercentage: BigDecimal,
    val indirectPercentage: BigDecimal?,
    val controlBasis: ControlBasis,
    val evidence: EvidenceReference,
)

enum class ControlBasis {
    SHARE_OWNERSHIP,
    VOTING_RIGHTS,
    BOARD_CONTROL,
    CONTRACTUAL_CONTROL,
    OTHER,
}

data class DocumentEvidence(
    val documentId: String,
    val type: DocumentType,
    val fileName: String,
    val issuer: String? = null,
    val issuePlace: String? = null,
    val issueDate: LocalDate? = null,
    val expiryDate: LocalDate? = null,
)

data class EvidenceReference(
    val documentId: String,
    val page: Int,
    val excerpt: String,
    val confidence: Double,
) {
    init {
        require(page >= 1) { "page must be >= 1" }
        require(confidence in 0.0..1.0) { "confidence must be between 0 and 1" }
    }
}

data class ScreeningResult(
    val partyId: String,
    val type: ScreeningType,
    val status: ScreeningStatus,
    val provider: String,
    val matchScore: Double? = null,
    val rationale: String? = null,
    val evidence: EvidenceReference? = null,
    val screenedAt: Instant? = null,
)

enum class ScreeningType {
    SANCTIONS,
    PEP,
    ADVERSE_MEDIA,
    INTERNAL_WATCHLIST,
}

enum class ScreeningStatus {
    CLEAR,
    POSSIBLE_MATCH,
    CONFIRMED_MATCH,
    NOT_PERFORMED,
    MANUAL_REVIEW_REQUIRED,
}

data class RiskFactor(
    val type: RiskFactorType,
    val level: RiskLevel,
    val rationale: String,
    val evidence: EvidenceReference? = null,
)

enum class RiskFactorType {
    GEOGRAPHY,
    INDUSTRY,
    PRODUCT,
    OWNERSHIP,
    PEP,
    SANCTIONS,
    ADVERSE_MEDIA,
    DOCUMENT_QUALITY,
    SOURCE_OF_FUNDS,
    SOURCE_OF_WEALTH,
    OTHER,
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    UNKNOWN,
}

data class RiskAssessment(
    val overallRisk: RiskLevel,
    val score: Int? = null,
    val factors: List<RiskFactor> = emptyList(),
    val rationale: String = "",
    val methodology: String = "",
)

enum class KycRecommendation {
    APPROVE,
    APPROVE_WITH_CONDITIONS,
    ENHANCED_DUE_DILIGENCE,
    MANUAL_REVIEW,
    REJECT,
}

data class DataQualityIssue(
    val severity: IssueSeverity,
    val code: String,
    val message: String,
    val fieldPath: String,
)

enum class IssueSeverity {
    INFO,
    WARNING,
    ERROR,
}

data class KycCase(
    val caseId: String,
    val subject: Party,
    val relatedParties: List<Party> = emptyList(),
    val ownership: List<OwnershipInterest> = emptyList(),
    val evidence: List<DocumentEvidence> = emptyList(),
    val screeningResults: List<ScreeningResult> = emptyList(),
    val riskAssessment: RiskAssessment? = null,
    val recommendation: KycRecommendation? = null,
    val issues: List<DataQualityIssue> = emptyList(),
)

fun interface KycRule {
    fun evaluate(kycCase: KycCase): List<DataQualityIssue>
}

fun interface KycRiskRule {
    fun evaluate(kycCase: KycCase): List<RiskFactor>
}

object RequiredSubjectFieldsRule : KycRule {
    override fun evaluate(kycCase: KycCase): List<DataQualityIssue> {
        val subject = kycCase.subject
        val issues = mutableListOf<DataQualityIssue>()

        if (subject.displayName.isBlank()) {
            issues += DataQualityIssue(
                IssueSeverity.ERROR,
                "DISPLAY_NAME_MISSING",
                "Subject display name is required",
                "subject.displayName",
            )
        }

        if (subject is LegalEntity) {
            if (subject.registrationNumber.isNullOrBlank()) {
                issues += DataQualityIssue(
                    IssueSeverity.ERROR,
                    "REGISTRATION_NUMBER_MISSING",
                    "Legal entity registration number is required",
                    "subject.registrationNumber",
                )
            }
            if (subject.jurisdictionOfIncorporation.isNullOrBlank()) {
                issues += DataQualityIssue(
                    IssueSeverity.ERROR,
                    "INCORPORATION_JURISDICTION_MISSING",
                    "Jurisdiction of incorporation is required",
                    "subject.jurisdictionOfIncorporation",
                )
            }
        }

        return issues
    }
}

data class KycReport(
    val caseId: String,
    val generatedAt: Instant,
    val subjectName: String,
    val subjectType: String,
    val overallRisk: RiskLevel,
    val recommendation: KycRecommendation?,
    val riskAssessment: RiskAssessment?,
    val issues: List<DataQualityIssue>,
    val evidenceReferences: List<EvidenceReference>,
    val executiveSummary: String,
)

object KycReportFactory {
    fun from(kycCase: KycCase): KycReport {
        val factors = kycCase.riskAssessment?.factors.orEmpty()
        val references = buildList {
            addAll(kycCase.ownership.map { it.evidence })
            addAll(kycCase.screeningResults.mapNotNull { it.evidence })
            addAll(factors.mapNotNull { it.evidence })
        }
        val risk = kycCase.riskAssessment?.overallRisk ?: RiskLevel.UNKNOWN
        val subjectType = when (kycCase.subject) {
            is LegalEntity -> "LegalEntity"
            is Person -> "Person"
        }

        return KycReport(
            caseId = kycCase.caseId,
            generatedAt = Instant.now(),
            subjectName = kycCase.subject.displayName,
            subjectType = subjectType,
            overallRisk = risk,
            recommendation = kycCase.recommendation,
            riskAssessment = kycCase.riskAssessment,
            issues = kycCase.issues,
            evidenceReferences = references,
            executiveSummary = "KYC assessment for ${kycCase.subject.displayName}: overall risk=$risk, recommendation=${kycCase.recommendation}, validation errors=${kycCase.issues.count { it.severity == IssueSeverity.ERROR }}.",
        )
    }
}
