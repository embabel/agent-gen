package com.embabel.metaagent.kycdemo

import com.embabel.agent.api.common.Ai
import com.embabel.agent.rag.ingestion.TikaHierarchicalContentReader
import com.embabel.agent.rag.service.support.DirectoryTextSearch
import com.embabel.agent.rag.tools.ToolishRag
import com.embabel.metaagent.service.MetaAgentApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
import java.nio.file.Files
import java.nio.file.Path

@SpringBootTest(classes = [MetaAgentApplication::class])
@ActiveProfiles("test")
@TestExecutionListeners(
    listeners = [DependencyInjectionTestExecutionListener::class],
    mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS,
)
/**
 * End-to-end KYC package extraction smoke test using the Embabel stack.
 *
 * The test writes a small synthetic KYC package to a temporary directory, parses each document with the
 * RAG/Tika reader, exposes the package as a [ToolishRag] reference, and asks the configured Embabel [Ai]
 * bean to use that reference through the fluent [com.embabel.agent.api.common.PromptRunner.withReference]
 * API while mapping the discovered evidence into the KYC ontology.
 *
 * The prompt is intentionally generic: it describes how a KYC package should be interpreted, but does not
 * hard-code the fixture's customer or beneficial owner names. The assertions are fixture-specific because
 * they verify that this package was merged into the expected legal-entity customer, natural-person UBO/SPF,
 * ownership, risk, and recommendation structure.
 *
 * This is an integration test, not a deterministic unit test. It depends on the configured default LLM and
 * can fail if model credentials, network access, or structured-output behavior are unavailable.
 */
class KycEmbabelLlmExtractionIntegrationTest {

    @Autowired
    lateinit var ai: Ai

    @TempDir
    lateinit var documentPackageDir: Path

    @Test
    fun `read documents with RAG modules and map to KycCase with Embabel LLM`() {
        writeDocument(
            "passport.txt",
            """
            Passport
            Name: John Smith
            Date of birth: 1983-01-12
            Nationality: GB
            Passport number: 123456789
            """.trimIndent(),
        )
        writeDocument(
            "proof-of-address.txt",
            """
            Utility Bill
            Customer: John Smith
            Address: 10 Market Street, London, GB, SW1A 1AA
            """.trimIndent(),
        )
        writeDocument(
            "certificate-of-incorporation.txt",
            """
            Certificate of Incorporation
            Company: Acme Payments Ltd
            Registration number: ACME-123
            Jurisdiction: GB
            Incorporated: 2020-01-15
            Director: John Smith
            """.trimIndent(),
        )
        writeDocument(
            "shareholder-register.txt",
            """
            Shareholder Register
            Acme Payments Ltd
            John Smith owns 40 percent of ordinary shares.
            """.trimIndent(),
        )
        writeDocument(
            "pep-declaration.txt",
            """
            PEP Declaration
            John Smith is a senior public figure.
            Public office: Deputy Minister of Finance
            Country: GB
            Current: true
            """.trimIndent(),
        )

        val reader = TikaHierarchicalContentReader()
        val parsedDocuments = Files.list(documentPackageDir)
            .map { reader.parseFile(it.toFile(), it.toUri().toString()) }
            .toList()
        assertTrue(parsedDocuments.all { it.leaves().any() })

        val kycDocuments = ToolishRag(
            "kycDocuments",
            "KYC package documents for extraction into the KYC ontology",
            DirectoryTextSearch(documentPackageDir.toString()),
        ).withGoal(
            """
            Find identity, address, incorporation, registration, directorship, signatory, ownership,
            PEP/SPF, screening, and source-of-wealth evidence needed to populate a KycCase.
            """.trimIndent(),
        )

        val prompt = """
            You are a KYC extraction agent.

            Use the available KYC document reference tools to search the supplied KYC package, then map
            the evidence you find into one KycCase object.
            Search for identity, address, incorporation, registration, ownership, director/signatory,
            PEP/SPF, screening, and source-of-wealth evidence before producing the final object.
            Do not invent missing values. Use null for unknown nullable fields.
            Use partyType values "person" and "legalEntity".
            Enum collections must be JSON arrays of enum strings, never objects.
            For example, use "roles": ["CUSTOMER"], not "roles": [{"role": "CUSTOMER"}].
            Identify the KYC customer from the package. If incorporation or registration evidence identifies
            a legal entity customer, use that legal entity as subject and place natural persons such as owners,
            directors, signatories, and PEPs in relatedParties.
            Generate stable non-null party ids by slugifying each party displayName, lower-case with hyphens.
            Use those ids consistently in ownership.ownerPartyId, ownership.ownedPartyId, and screeningResults.partyId.
            Evidence page numbers are 1-based. If a source chunk has no page number, use page 1, never 0.
            Include seniorPublicFigure=true when a document says the person is a senior public figure.
            Assign party roles only when supported by the supplied chunks.
            If any beneficial owner, director, signatory, or customer is an SPF/PEP and no source-of-wealth
            evidence is supplied, set overallRisk=HIGH, recommendation=ENHANCED_DUE_DILIGENCE,
            and add a SOURCE_OF_WEALTH_MISSING warning.

            Required top-level fields:
            caseId, subject, relatedParties, ownership, evidence, screeningResults, riskAssessment, recommendation, issues.
        """.trimIndent()

        val kycCase = ai.withDefaultLlm()
            .withReference(kycDocuments)
            .creating(KycCase::class.java)
            .fromPrompt(prompt)

        assertInstanceOf(LegalEntity::class.java, kycCase.subject)
        val subject = kycCase.subject as LegalEntity
        assertEquals("Acme Payments Ltd", subject.displayName)
        assertEquals("ACME-123", subject.registrationNumber)

        assertFalse(kycCase.relatedParties.isEmpty())
        val johnSmith = kycCase.relatedParties.filterIsInstance<Person>().first()
        assertEquals("John Smith", johnSmith.displayName)
        assertTrue(kycCase.containsSeniorPublicFigureEvidence(johnSmith))
        assertTrue(kycCase.containsPublicOfficeEvidence("Deputy Minister of Finance"))
        assertTrue(
            kycCase.recommendation == KycRecommendation.ENHANCED_DUE_DILIGENCE ||
                kycCase.riskAssessment?.overallRisk == RiskLevel.HIGH ||
                kycCase.issues.any { it.code == "SOURCE_OF_WEALTH_MISSING" },
        )

        logger.info(
            """
            ===== EMBABEL KYC LLM EXTRACTION =====
            Customer: ${subject.displayName}
            Beneficial owner: ${johnSmith.displayName}
            SPF: ${johnSmith.pepProfile?.seniorPublicFigure}
            Recommendation: ${kycCase.recommendation}
            """.trimIndent(),
        )
    }

    private fun writeDocument(fileName: String, content: String) {
        Files.writeString(documentPackageDir.resolve(fileName), content)
    }

    private fun KycCase.containsSeniorPublicFigureEvidence(person: Person): Boolean =
        person.pepProfile?.seniorPublicFigure == true ||
            PartyRole.POLITICALLY_EXPOSED_PERSON in person.roles ||
            toString().contains("senior public figure", ignoreCase = true) ||
            toString().contains("SPF", ignoreCase = true) ||
            toString().contains("PEP", ignoreCase = true)

    private fun KycCase.containsPublicOfficeEvidence(publicOffice: String): Boolean =
        relatedParties
            .filterIsInstance<Person>()
            .any { it.pepProfile?.publicOffice.equals(publicOffice, ignoreCase = true) } ||
            toString().contains(publicOffice, ignoreCase = true)

    companion object {
        private val logger = LoggerFactory.getLogger(KycEmbabelLlmExtractionIntegrationTest::class.java)

        @BeforeAll
        @JvmStatic
        fun setUp() {
            System.setProperty("embabel.agent.shell.interactive.enabled", "false")
        }
    }
}
