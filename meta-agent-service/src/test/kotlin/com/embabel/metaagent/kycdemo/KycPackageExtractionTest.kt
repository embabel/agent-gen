package com.embabel.metaagent.kycdemo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class KycPackageExtractionTest {

    private val logger = LoggerFactory.getLogger(KycPackageExtractionTest::class.java)

    private val objectMapper: ObjectMapper = JsonMapper.builder()
        .addModule(JavaTimeModule())
        .addModule(KotlinModule.Builder().build())
        .build()

    @Test
    fun `document package injection produces one coherent KycCase`() {
        val documents = listOf(
            SourceDocument("passport.pdf", DocumentType.PASSPORT, "Name: John Smith\nDOB: 1983-01-12\nNationality: GB"),
            SourceDocument("proof-of-address.pdf", DocumentType.PROOF_OF_ADDRESS, "10 Market Street, London, SW1A 1AA, GB"),
            SourceDocument("certificate-of-incorporation.pdf", DocumentType.CERTIFICATE_OF_INCORPORATION, "Acme Payments Ltd ACME-123 GB 2020-01-15 Director John Smith"),
            SourceDocument("shareholder-register.pdf", DocumentType.SHAREHOLDER_REGISTER, "John Smith owns 40 percent of ordinary shares."),
            SourceDocument("pep-declaration.pdf", DocumentType.PEP_DECLARATION, "John Smith is a senior public figure. Public office: Deputy Minister of Finance."),
        )
        val extractor: KycExtractor = FakeStructuredKycExtractor(objectMapper)

        val kycCase = extractor.extract(documents)

        assertInstanceOf(LegalEntity::class.java, kycCase.subject)
        val subject = kycCase.subject as LegalEntity
        assertEquals("Acme Payments Ltd", subject.displayName)
        assertEquals("ACME-123", subject.registrationNumber)
        assertEquals(setOf(PartyRole.CUSTOMER), subject.roles)

        assertEquals(1, kycCase.relatedParties.size)
        assertInstanceOf(Person::class.java, kycCase.relatedParties.first())
        val beneficialOwner = kycCase.relatedParties.first() as Person
        assertEquals("John Smith", beneficialOwner.displayName)
        assertEquals("1983-01-12", beneficialOwner.dateOfBirth.toString())
        assertEquals(setOf("GB"), beneficialOwner.nationalities)
        assertEquals(
            setOf(
                PartyRole.BENEFICIAL_OWNER,
                PartyRole.DIRECTOR,
                PartyRole.AUTHORIZED_SIGNATORY,
                PartyRole.POLITICALLY_EXPOSED_PERSON,
            ),
            beneficialOwner.roles,
        )
        assertEquals(true, beneficialOwner.pepProfile?.seniorPublicFigure)
        assertEquals("Deputy Minister of Finance", beneficialOwner.pepProfile?.publicOffice)
        assertEquals(listOf("London"), beneficialOwner.addresses.map { it.city })

        val ownership = kycCase.ownership.single()
        assertEquals("person-john-smith", ownership.ownerPartyId)
        assertEquals("entity-acme-payments", ownership.ownedPartyId)
        assertEquals(0, ownership.directPercentage.compareTo("40".toBigDecimal()))

        assertEquals(5, kycCase.evidence.size)
        assertEquals(ScreeningStatus.POSSIBLE_MATCH, kycCase.screeningResults.single { it.type == ScreeningType.PEP }.status)
        assertEquals(RiskLevel.HIGH, kycCase.riskAssessment?.overallRisk)
        assertEquals(KycRecommendation.ENHANCED_DUE_DILIGENCE, kycCase.recommendation)
        assertEquals("SOURCE_OF_WEALTH_MISSING", kycCase.issues.single().code)
        assertEquals(emptyList<DataQualityIssue>(), RequiredSubjectFieldsRule.evaluate(kycCase))

        val report = KycReportFactory.from(kycCase)
        assertEquals("LegalEntity", report.subjectType)
        assertEquals(RiskLevel.HIGH, report.overallRisk)

        printDemoSummary(kycCase)
    }

    private class FakeStructuredKycExtractor(
        private val objectMapper: ObjectMapper,
    ) : KycExtractor {

        override fun extract(documents: List<SourceDocument>): KycCase {
            require(documents.map { it.type }.containsAll(requiredDocuments)) {
                "KYC package must include identity, address, incorporation, ownership, and PEP declaration documents"
            }
            require(documents.all { it.text.isNotBlank() }) {
                "All injected documents must contain extracted text"
            }

            return objectMapper.readValue(llmStructuredJson)
        }

        private companion object {
            private val requiredDocuments = setOf(
                DocumentType.PASSPORT,
                DocumentType.PROOF_OF_ADDRESS,
                DocumentType.CERTIFICATE_OF_INCORPORATION,
                DocumentType.SHAREHOLDER_REGISTER,
                DocumentType.PEP_DECLARATION,
            )

            private val llmStructuredJson = """
                {
                  "caseId": "kyc-demo-001",
                  "subject": {
                    "partyType": "legalEntity",
                    "id": "entity-acme-payments",
                    "displayName": "Acme Payments Ltd",
                    "registrationNumber": "ACME-123",
                    "jurisdictionOfIncorporation": "GB",
                    "incorporationDate": "2020-01-15",
                    "addresses": [],
                    "roles": ["CUSTOMER"]
                  },
                  "relatedParties": [
                    {
                      "partyType": "person",
                      "id": "person-john-smith",
                      "displayName": "John Smith",
                      "dateOfBirth": "1983-01-12",
                      "nationalities": ["GB"],
                      "addresses": [
                        {
                          "line1": "10 Market Street",
                          "line2": null,
                          "city": "London",
                          "region": null,
                          "postalCode": "SW1A 1AA",
                          "countryCode": "GB"
                        }
                      ],
                      "roles": ["BENEFICIAL_OWNER", "DIRECTOR", "AUTHORIZED_SIGNATORY", "POLITICALLY_EXPOSED_PERSON"],
                      "pepProfile": {
                        "category": "DOMESTIC",
                        "publicOffice": "Deputy Minister of Finance",
                        "organization": "Ministry of Finance",
                        "countryCode": "GB",
                        "fromDate": null,
                        "toDate": null,
                        "current": true,
                        "seniorPublicFigure": true,
                        "evidence": {
                          "documentId": "pep-declaration",
                          "page": 1,
                          "excerpt": "John Smith is a senior public figure. Public office: Deputy Minister of Finance.",
                          "confidence": 0.97
                        }
                      }
                    }
                  ],
                  "ownership": [
                    {
                      "ownerPartyId": "person-john-smith",
                      "ownedPartyId": "entity-acme-payments",
                      "directPercentage": 40,
                      "indirectPercentage": 0,
                      "controlBasis": "SHARE_OWNERSHIP",
                      "evidence": {
                        "documentId": "shareholder-register",
                        "page": 1,
                        "excerpt": "John Smith owns 40 percent of ordinary shares.",
                        "confidence": 0.98
                      }
                    }
                  ],
                  "evidence": [
                    {"documentId": "passport", "type": "PASSPORT", "fileName": "passport.pdf", "issuer": "HM Passport Office", "issueDate": null, "expiryDate": null},
                    {"documentId": "proof-of-address", "type": "PROOF_OF_ADDRESS", "fileName": "proof-of-address.pdf", "issuer": "Utility Provider", "issueDate": null, "expiryDate": null},
                    {"documentId": "certificate-of-incorporation", "type": "CERTIFICATE_OF_INCORPORATION", "fileName": "certificate-of-incorporation.pdf", "issuer": "Companies House", "issueDate": "2020-01-15", "expiryDate": null},
                    {"documentId": "shareholder-register", "type": "SHAREHOLDER_REGISTER", "fileName": "shareholder-register.pdf", "issuer": "Acme Payments Ltd", "issueDate": null, "expiryDate": null},
                    {"documentId": "pep-declaration", "type": "PEP_DECLARATION", "fileName": "pep-declaration.pdf", "issuer": "John Smith", "issueDate": null, "expiryDate": null}
                  ],
                  "screeningResults": [
                    {
                      "partyId": "person-john-smith",
                      "type": "PEP",
                      "status": "POSSIBLE_MATCH",
                      "provider": "demo-screening",
                      "matchScore": 0.91,
                      "rationale": "Self-declared senior public figure.",
                      "evidence": {
                        "documentId": "pep-declaration",
                        "page": 1,
                        "excerpt": "John Smith is a senior public figure.",
                        "confidence": 0.97
                      },
                      "screenedAt": "2026-07-21T00:00:00Z"
                    }
                  ],
                  "riskAssessment": {
                    "overallRisk": "HIGH",
                    "factors": [
                      {
                        "type": "PEP",
                        "level": "HIGH",
                        "rationale": "Beneficial owner is a current senior public figure.",
                        "evidence": {
                          "documentId": "pep-declaration",
                          "page": 1,
                          "excerpt": "John Smith is a senior public figure.",
                          "confidence": 0.97
                        }
                      },
                      {
                        "type": "OWNERSHIP",
                        "level": "MEDIUM",
                        "rationale": "John Smith owns 40 percent of the customer.",
                        "evidence": {
                          "documentId": "shareholder-register",
                          "page": 1,
                          "excerpt": "John Smith owns 40 percent of ordinary shares.",
                          "confidence": 0.98
                        }
                      }
                    ],
                    "rationale": "Enhanced due diligence required because a beneficial owner is an SPF/PEP."
                  },
                  "recommendation": "ENHANCED_DUE_DILIGENCE",
                  "issues": [
                    {
                      "severity": "WARNING",
                      "code": "SOURCE_OF_WEALTH_MISSING",
                      "message": "No source-of-wealth evidence was supplied for the senior public figure.",
                      "fieldPath": "relatedParties[0].pepProfile"
                    }
                  ]
                }
            """.trimIndent()
        }
    }

    private fun printDemoSummary(kycCase: KycCase) {
        val subject = kycCase.subject as LegalEntity
        val beneficialOwner = kycCase.relatedParties.first() as Person
        val ownership = kycCase.ownership.first()

        logger.info(
            """
            ===== KYC PACKAGE =====
            Documents: ${kycCase.evidence.size}

            Customer:
                ${subject.displayName}

            Registration:
                ${subject.registrationNumber}

            Beneficial owner:
                ${beneficialOwner.displayName}

            SPF:
                ${beneficialOwner.pepProfile?.seniorPublicFigure} (${beneficialOwner.pepProfile?.publicOffice})

            Ownership:
                ${ownership.directPercentage}% via ${ownership.controlBasis}

            Risk:
                ${kycCase.riskAssessment?.overallRisk}

            Recommendation:
                ${kycCase.recommendation}

            Issues:
                ${kycCase.issues.joinToString { it.message }}
            """.trimIndent(),
        )
    }
}
