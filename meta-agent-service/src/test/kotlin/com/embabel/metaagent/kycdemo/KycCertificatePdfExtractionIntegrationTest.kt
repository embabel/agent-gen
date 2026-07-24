package com.embabel.metaagent.kycdemo

import com.embabel.agent.api.common.Ai
import com.embabel.metaagent.service.MetaAgentApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import java.nio.file.Path
import java.time.LocalDate

@SpringBootTest(classes = [MetaAgentApplication::class])
@ActiveProfiles("test")
@TestExecutionListeners(
    listeners = [DependencyInjectionTestExecutionListener::class],
    mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS,
)
/**
 * Production-shaped certificate extraction test.
 *
 * This test keeps document parsing and retrieval in the Embabel RAG stack, then delegates semantic mapping
 * from unstructured certificate text to the KYC ontology to the configured Embabel [Ai] bean:
 *
 * PDF certificate resources -> TikaHierarchicalContentReader -> extracted searchable text ->
 * DirectoryTextSearch -> ToolishRag via PromptRunner.withReference -> structured output -> KycCase.
 *
 * Unlike [KycCertificatePdfPrimitiveMappingTest], this test does not hardcode the document-to-field mapping.
 * The assertions are fixture-specific, but extraction is performed through Embabel and materialized into [KycCase]
 * by the Embabel/Jackson structured-output path.
 */
class KycCertificatePdfExtractionIntegrationTest {

    @Autowired
    lateinit var ai: Ai

    @TempDir
    lateinit var extractedDocumentDir: Path

    @Test
    fun `extract KycCase from incorporation certificate PDF using Embabel`() {
        val kycCase = KycCertificatePdfExtractionSupport.extractCertificateKycCase(ai, extractedDocumentDir)

        assertInstanceOf(LegalEntity::class.java, kycCase.subject)
        val subject = kycCase.subject as LegalEntity
        assertEquals("WUXI VACCINES IRELAND LIMITED", subject.displayName)
        assertEquals("652131", subject.registrationNumber)
        assertEquals("IE", subject.jurisdictionOfIncorporation)
        assertEquals(LocalDate.parse("2019-06-20"), subject.incorporationDate)
        assertEquals(setOf(PartyRole.CUSTOMER), subject.roles)
        assertTrue(subject.addresses.isEmpty())

        assertTrue(kycCase.relatedParties.isEmpty())
        assertTrue(kycCase.ownership.isEmpty())
        assertEquals(KycRecommendation.MANUAL_REVIEW, kycCase.recommendation)
        assertEquals(RiskLevel.UNKNOWN, kycCase.riskAssessment?.overallRisk)
        assertTrue(kycCase.issues.any { it.code == "BENEFICIAL_OWNERSHIP_MISSING" })
        assertTrue(kycCase.evidence.any { it.type == DocumentType.CERTIFICATE_OF_INCORPORATION })
        assertTrue(kycCase.evidence.any { it.issuePlace == "Dublin" })

        logger.info(
            """
            ===== KYC CERTIFICATE PDF EXTRACTION =====
            Customer: ${subject.displayName}
            Registration: ${subject.registrationNumber}
            Incorporation date: ${subject.incorporationDate}
            Incorporation year: ${subject.incorporationDate?.year}
            Jurisdiction: ${subject.jurisdictionOfIncorporation}
            Certificate issue place: ${kycCase.evidence.firstOrNull { it.type == DocumentType.CERTIFICATE_OF_INCORPORATION }?.issuePlace}
            Company county: not present in certificate
            Recommendation: ${kycCase.recommendation}
            Issues: ${kycCase.issues.map { it.code }}
            """.trimIndent(),
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(KycCertificatePdfExtractionIntegrationTest::class.java)

        @BeforeAll
        @JvmStatic
        fun setUp() {
            System.setProperty("embabel.agent.shell.interactive.enabled", "false")
        }
    }
}
