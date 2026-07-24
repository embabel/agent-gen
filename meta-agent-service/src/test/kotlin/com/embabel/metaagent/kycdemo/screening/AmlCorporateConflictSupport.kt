package com.embabel.metaagent.kycdemo.screening

import com.embabel.agent.rag.ingestion.TikaHierarchicalContentReader
import com.embabel.metaagent.kycdemo.AmlConflictCertificatePdfFixture
import org.junit.jupiter.api.Assertions.assertTrue
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Demo support for the KYC + AML conflict scenario.
 *
 * Intended end-to-end demo pipeline:
 *
 * 1. Create visible input evidence.
 *    The synthetic incorporation certificate is available as a named demo PDF under
 *    `src/test/resources/kycdemo/screening/` and copied to `target/kycdemo/aml/input/`
 *    when the full pipeline test runs.
 *
 * 2. Ingest the input PDF into a KYC case.
 *    The KYC subject remains the customer from the supplied documents, for example
 *    `KINGS ROMANS GROUP LIMITED`, with its extracted registration number,
 *    jurisdiction, incorporation date, and registered office.
 *
 * 3. Compute baseline KYC risk from internal evidence only.
 *    This captures document quality, jurisdiction, business activity, ownership
 *    completeness, source-of-funds evidence, and source-of-wealth evidence before
 *    external AML screening changes the risk picture.
 *
 * 4. Run deterministic AML candidate retrieval.
 *    The customer is screened against official sanctions data, currently the real
 *    local OFAC SDN XML file supplied through `AML_OFAC_SDN_XML`. Retrieval keeps
 *    candidate records separate from final analyst disposition because the system
 *    does not yet know whether the KYC subject and a sanctions candidate are the
 *    same entity.
 *
 * 5. Run LLM AML disposition for ambiguous candidates.
 *    The LLM receives the KYC subject and all candidate hits, then computes a
 *    per-candidate `confidenceScore`, `confidenceLevel`, disposition, rationale,
 *    missing evidence, and analyst action. The numeric confidence in this logical
 *    assessment is an analyst-style LLM estimate, not the deterministic matcher score.
 *
 * 6. Select the primary AML concern.
 *    The highest-confidence candidate becomes the primary AML concern, while lower
 *    confidence candidates remain attached as secondary findings. The KYC subject
 *    name is not overwritten with a sanctions candidate name.
 *
 * 7. Amend the KYC case.
 *    The case is enriched with AML screening results, candidate evidence, issues
 *    such as possible sanctions match or missing ownership evidence, and the
 *    rationale for the selected primary concern.
 *
 * 8. Recompute final risk.
 *    Baseline KYC risk is recomputed after AML enrichment. A possible sanctions
 *    match with meaningful confidence should increase final risk and normally lead
 *    to enhanced due diligence or manual review.
 *
 * 9. Generate the final demo PDF.
 *    The final report should print the input KYC facts, baseline risk, AML
 *    candidates, LLM confidence scores, primary AML concern, amended KYC case,
 *    final risk, and recommendation.
 */
object AmlCorporateConflictSupport {

    fun createAndScreenCorporateConflict(): Pair<Path, ScreeningAssessment> {
        val certificatePdf = AmlConflictCertificatePdfFixture.create(
            demoInputDirectory().resolve(AmlConflictCertificatePdfFixture.INPUT_FILE_NAME),
        )
        val certificateText = parsePdf(certificatePdf).normalized()
        assertTrue(certificateText.contains("kings romans group limited"))
        assertTrue(certificateText.contains("krg-2026-041"))

        val assessment = AmlScreeningService(
            providers = listOf(
                emptyEuProvider(),
                OfacSanctionsXmlProvider(
                    providerId = "ofac-real-file",
                    xmlPath = requiredRealFile("AML_OFAC_SDN_XML"),
                ),
            ),
        ).screen(
            ScreeningSubject(
                id = "kings-romans-group-limited",
                displayName = AmlConflictCertificatePdfFixture.COMPANY_NAME,
                subjectType = ScreeningSubjectType.LEGAL_ENTITY,
                countries = setOf(AmlConflictCertificatePdfFixture.JURISDICTION),
                identifiers = setOf(
                    ScreeningIdentifier(
                        type = ScreeningIdentifierType.COMPANY_REGISTRATION_NUMBER,
                        value = AmlConflictCertificatePdfFixture.REGISTRATION_NUMBER,
                        issuingCountry = AmlConflictCertificatePdfFixture.JURISDICTION,
                    ),
                ),
            ),
        )
        return certificatePdf to assessment
    }

    fun genericPossibleCorporateScreeningExample(): AmlLogicalScreeningAssessment =
        AmlLogicalScreeningAssessment(
            subjectName = "Example Trading Limited",
            overallStatus = ScreeningAssessmentStatus.MANUAL_REVIEW_REQUIRED,
            candidateAssessments = listOf(
                AmlLogicalCandidateAssessment(
                    sourceRecordId = "EXAMPLE-1",
                    candidateName = "Example Trading International Limited",
                    confidenceScore = 0.74,
                    confidenceLevel = AmlLlmConfidenceLevel.MEDIUM,
                    recommendedDisposition = ScreeningDisposition.POSSIBLE_MATCH,
                    rationale = "The legal name and alias are close, but no matching registration number or address confirmation is available.",
                    missingEvidence = listOf("Matching company registration number", "Ownership confirmation"),
                ),
                AmlLogicalCandidateAssessment(
                    sourceRecordId = "EXAMPLE-2",
                    candidateName = "Example Investment Company",
                    confidenceScore = 0.48,
                    confidenceLevel = AmlLlmConfidenceLevel.MEDIUM,
                    recommendedDisposition = ScreeningDisposition.POSSIBLE_MATCH,
                    rationale = "The name has partial overlap, but the business descriptor and missing secondary identifiers reduce confidence.",
                    missingEvidence = listOf("Address confirmation", "Control relationship evidence"),
                ),
                AmlLogicalCandidateAssessment(
                    sourceRecordId = "EXAMPLE-3",
                    candidateName = "Unrelated Example Holdings",
                    confidenceScore = 0.22,
                    confidenceLevel = AmlLlmConfidenceLevel.LOW,
                    recommendedDisposition = ScreeningDisposition.LIKELY_FALSE_POSITIVE,
                    rationale = "The candidate shares a generic token only and has conflicting jurisdiction evidence.",
                    missingEvidence = listOf("No matching alias", "Conflicting jurisdiction evidence"),
                ),
            ),
            rationale = "The example keeps candidates separate and does not confirm any match without secondary identifier agreement.",
            analystAction = "Escalate possible matches for registry, ownership, and address evidence; retain likely false positives for audit.",
        )

    private fun parsePdf(path: Path): String {
        val document = TikaHierarchicalContentReader().parseFile(path.toFile(), path.toUri().toString())
        return document.leaves().joinToString("\n") { it.content }
    }

    private fun demoInputDirectory(): Path =
        moduleDirectory()
            .resolve("target")
            .resolve("kycdemo")
            .resolve("aml")
            .resolve("input")

    private fun moduleDirectory(): Path {
        val currentDirectory = Paths.get("").toAbsolutePath()
        return if (currentDirectory.fileName.toString() == "meta-agent-service") {
            currentDirectory
        } else {
            currentDirectory.resolve("meta-agent-service")
        }
    }

    private fun emptyEuProvider(): NamedScreeningProvider =
        StaticScreeningProvider(
            providerId = "eu-not-loaded-for-ofac-corporate-conflict-test",
            source = ScreeningSource.EU_FINANCIAL_SANCTIONS,
            entries = emptyList(),
        )

    private fun requiredRealFile(environmentVariable: String): Path {
        val configured = requireNotNull(System.getenv(environmentVariable)) {
            "$environmentVariable must point to a local sanctions XML file"
        }
        val path = Paths.get(configured)
        require(Files.isRegularFile(path)) { "$environmentVariable must point to an existing regular file: $path" }
        return path
    }

    private fun String.normalized(): String =
        lowercase().replace(Regex("\\s+"), " ").trim()
}
