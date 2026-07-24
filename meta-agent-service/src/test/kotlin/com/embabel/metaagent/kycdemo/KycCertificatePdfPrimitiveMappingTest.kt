package com.embabel.metaagent.kycdemo

import com.embabel.agent.rag.ingestion.TikaHierarchicalContentReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month
import java.nio.file.Paths

/**
 * Primitive deterministic baseline for transforming certificate PDFs into the KYC ontology.
 *
 * This test deliberately stops before LLM extraction and uses a hardcoded fixture mapper. It proves only
 * that Tika can extract enough certificate text for a minimal hand-written mapping into [KycCase].
 *
 * PDF certificate -> Tika text extraction -> primitive fixture mapper -> KycCase.
 *
 * The production-shaped test is [KycCertificatePdfExtractionIntegrationTest], which uses Embabel [com.embabel.agent.api.common.Ai],
 * ToolishRag, structured output, and Jackson materialization instead of this hardcoded mapper.
 */
class KycCertificatePdfPrimitiveMappingTest {

    private val reader = TikaHierarchicalContentReader()
    private val extractor = CertificateOnlyKycCaseExtractor()

    @Test
    fun `transform Irish certificate PDF into KycCase`() {
        val document = parseCertificate("kycdemo/090151b2807a4b8f.pdf")

        val kycCase = extractor.extract(document)

        assertInstanceOf(LegalEntity::class.java, kycCase.subject)
        val subject = kycCase.subject as LegalEntity
        assertEquals("WUXI VACCINES IRELAND LIMITED", subject.displayName)
        assertEquals("652131", subject.registrationNumber)
        assertEquals("IE", subject.jurisdictionOfIncorporation)
        assertEquals(LocalDate.of(2019, Month.JUNE, 20), subject.incorporationDate)
        assertEquals(setOf(PartyRole.CUSTOMER), subject.roles)

        assertEquals(DocumentType.CERTIFICATE_OF_INCORPORATION, kycCase.evidence.single().type)
        assertEquals("Registrar of Companies", kycCase.evidence.single().issuer)
        assertEquals("Dublin", kycCase.evidence.single().issuePlace)
        assertEquals(RiskLevel.UNKNOWN, kycCase.riskAssessment?.overallRisk)
        assertEquals(KycRecommendation.MANUAL_REVIEW, kycCase.recommendation)
        assertEquals("BENEFICIAL_OWNERSHIP_MISSING", kycCase.issues.single().code)

        assertEquals(emptyList<DataQualityIssue>(), RequiredSubjectFieldsRule.evaluate(kycCase))
    }

    @Test
    fun `transform UK certificate PDF into KycCase`() {
        val document = parseCertificate("kycdemo/Certificate-of-Incorporation-of-a-Private-Limited-Company.pdf")

        val kycCase = extractor.extract(document)

        assertInstanceOf(LegalEntity::class.java, kycCase.subject)
        val subject = kycCase.subject as LegalEntity
        assertEquals("THE DE CURCI TRUST", subject.displayName)
        assertEquals("10646541", subject.registrationNumber)
        assertEquals("GB", subject.jurisdictionOfIncorporation)
        assertEquals(LocalDate.of(2017, Month.MARCH, 1), subject.incorporationDate)
        assertEquals(setOf(PartyRole.CUSTOMER), subject.roles)

        assertEquals(DocumentType.CERTIFICATE_OF_INCORPORATION, kycCase.evidence.single().type)
        assertEquals("Companies House", kycCase.evidence.single().issuer)
        assertEquals("Cardiff", kycCase.evidence.single().issuePlace)
        assertEquals(RiskLevel.UNKNOWN, kycCase.riskAssessment?.overallRisk)
        assertEquals(KycRecommendation.MANUAL_REVIEW, kycCase.recommendation)
        assertEquals("BENEFICIAL_OWNERSHIP_MISSING", kycCase.issues.single().code)

        assertEquals(emptyList<DataQualityIssue>(), RequiredSubjectFieldsRule.evaluate(kycCase))
    }

    private fun parseCertificate(resourcePath: String): SourceDocument {
        val resource = requireNotNull(javaClass.classLoader.getResource(resourcePath)) {
            "Missing test resource: $resourcePath"
        }
        val file = Paths.get(resource.toURI()).toFile()
        val materializedDocument = reader.parseFile(file, resource.toURI().toString())
        val text = materializedDocument.leaves().joinToString("\n") { it.content }
        assertTrue(text.isNotBlank())
        return SourceDocument(file.name, DocumentType.CERTIFICATE_OF_INCORPORATION, text)
    }

    private class CertificateOnlyKycCaseExtractor : KycExtractor {

        override fun extract(documents: List<SourceDocument>): KycCase {
            require(documents.size == 1) { "Certificate-only extractor expects one document" }
            val document = documents.single()
            require(document.type == DocumentType.CERTIFICATE_OF_INCORPORATION) {
                "Expected certificate of incorporation document"
            }
            return extract(document)
        }

        fun extract(document: SourceDocument): KycCase {
            val normalized = document.text.normalized()
            val company = when {
                normalized.contains("wuxi vaccines ireland limited") -> CertificateFields(
                    displayName = "WUXI VACCINES IRELAND LIMITED",
                    registrationNumber = "652131",
                    jurisdiction = "IE",
                    incorporationDate = LocalDate.of(2019, Month.JUNE, 20),
                    issuer = "Registrar of Companies",
                    issuePlace = "Dublin",
                    excerpt = "company number 652131 WUXI VACCINES IRELAND LIMITED was Incorporated under the Companies Act 2014",
                )

                normalized.contains("the de curci trust") -> CertificateFields(
                    displayName = "THE DE CURCI TRUST",
                    registrationNumber = "10646541",
                    jurisdiction = "GB",
                    incorporationDate = LocalDate.of(2017, Month.MARCH, 1),
                    issuer = "Companies House",
                    issuePlace = "Cardiff",
                    excerpt = "Company Number 10646541 THE DE CURCI TRUST is this day incorporated under the Companies Act 2006",
                )

                else -> error("Unsupported certificate fixture: ${document.fileName}")
            }

            val subjectId = "entity-" + company.displayName.lowercase()
                .replace(Regex("[^a-z0-9]+"), "-")
                .trim('-')

            return KycCase(
                caseId = "kyc-certificate-${company.registrationNumber}",
                subject = LegalEntity(
                    id = subjectId,
                    displayName = company.displayName,
                    registrationNumber = company.registrationNumber,
                    jurisdictionOfIncorporation = company.jurisdiction,
                    incorporationDate = company.incorporationDate,
                    addresses = emptyList(),
                    roles = setOf(PartyRole.CUSTOMER),
                ),
                relatedParties = emptyList(),
                ownership = emptyList(),
                evidence = listOf(
                    DocumentEvidence(
                        documentId = document.fileName.removeSuffix(".pdf"),
                        type = DocumentType.CERTIFICATE_OF_INCORPORATION,
                        fileName = document.fileName,
                        issuer = company.issuer,
                        issuePlace = company.issuePlace,
                        issueDate = company.incorporationDate,
                        expiryDate = null,
                    ),
                ),
                screeningResults = emptyList(),
                riskAssessment = RiskAssessment(
                    overallRisk = RiskLevel.UNKNOWN,
                    factors = listOf(
                        RiskFactor(
                            type = RiskFactorType.DOCUMENT_QUALITY,
                            level = RiskLevel.LOW,
                            rationale = "Certificate of incorporation text was extracted from PDF.",
                            evidence = EvidenceReference(
                                documentId = document.fileName.removeSuffix(".pdf"),
                                page = 1,
                                excerpt = company.excerpt,
                                confidence = 0.95,
                            ),
                        ),
                    ),
                    rationale = "Certificate identifies the legal entity, but ownership, control, screening, and source-of-funds evidence are not present.",
                ),
                recommendation = KycRecommendation.MANUAL_REVIEW,
                issues = listOf(
                    DataQualityIssue(
                        severity = IssueSeverity.WARNING,
                        code = "BENEFICIAL_OWNERSHIP_MISSING",
                        message = "Certificate of incorporation does not identify beneficial owners or controllers.",
                        fieldPath = "ownership",
                    ),
                ),
            )
        }

        private data class CertificateFields(
            val displayName: String,
            val registrationNumber: String,
            val jurisdiction: String,
            val incorporationDate: LocalDate,
            val issuer: String,
            val issuePlace: String,
            val excerpt: String,
        )

        private fun String.normalized(): String =
            lowercase().replace(Regex("\\s+"), " ").trim()
    }
}
