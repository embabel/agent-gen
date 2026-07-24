package com.embabel.metaagent.kycdemo.screening

import com.embabel.agent.api.common.Ai
import com.embabel.agent.rag.ingestion.TikaHierarchicalContentReader
import com.embabel.agent.rag.service.support.DirectoryTextSearch
import com.embabel.agent.rag.tools.ToolishRag
import com.embabel.metaagent.kycdemo.*
import com.embabel.metaagent.service.MetaAgentApplication
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.text.PDFTextStripper
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import java.awt.Color
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@SpringBootTest(classes = [MetaAgentApplication::class])
@ActiveProfiles("test")
@TestExecutionListeners(
    listeners = [DependencyInjectionTestExecutionListener::class],
    mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS,
)
/**
 * End-to-end demo pipeline for KYC certificate ingestion, AML sanctions
 * screening, AML-aware risk recomputation, and structured PDF reporting.
 *
 * The test intentionally uses two separate LLM calls:
 *
 * 1. KYC extraction: the input certificate PDF is parsed with Tika, exposed through
 *    [ToolishRag], and mapped by the LLM into a [KycCase]. This mirrors the
 *    certificate ingestion tests and avoids procedural document-to-field mapping
 *    in the AML pipeline.
 * 2. AML disposition: deterministic OFAC candidate retrieval supplies candidate
 *    hits, while the LLM assigns per-candidate confidence, disposition, missing
 *    evidence, and analyst action. This keeps retrieval deterministic and
 *    reserves judgment over ambiguous matches for the LLM.
 *
 * Deterministic rules remain responsible for baseline risk scoring, AML-adjusted
 * risk scoring, merge treatment by LLM confidence/disposition, and report
 * rendering. Demo policy numbers are centralized in [AmlRiskMethodology] and
 * [com.embabel.metaagent.kycdemo.KycRiskScoringMethodology].
 */
class KycAmlRiskPipelineIntegrationTest {

    @Autowired
    lateinit var ai: Ai

    @Test
    @EnabledIfEnvironmentVariable(named = "AML_OFAC_SDN_XML", matches = ".+")
    fun `ingests KYC PDF screens AML candidates amends case recomputes risk and generates report PDF`() {
        val inputPdf = copyInputPdfResourceToTarget()
        val initialCase = extractKycCaseWithLlm(inputPdf)
        val baselineRisk = BaselineKycRiskMethodologyRule.assess(initialCase)

        val baselineCase = initialCase.copy(
            riskAssessment = baselineRisk,
            recommendation = KycRecommendation.MANUAL_REVIEW,
        )
        val amlScreening = screenAml(baselineCase)
        val amlLogicalAssessment = assessAmlWithLlm(amlScreening)
        val primaryConcern = requireNotNull(
            amlLogicalAssessment.candidateAssessments.maxByOrNull { it.confidenceScore },
        )

        val amendedCase = amendKycCase(
            kycCase = baselineCase,
            amlScreening = amlScreening,
            amlLogicalAssessment = amlLogicalAssessment,
            primaryConcern = primaryConcern,
        )
        val finalRisk = requireNotNull(amendedCase.riskAssessment)
        val outputPdf = outputPdfPath(inputPdf)
        KycAmlPipelinePdfRenderer.render(
            inputPdf = inputPdf,
            kycCase = amendedCase,
            baselineRisk = baselineRisk,
            amlScreening = amlScreening,
            amlLogicalAssessment = amlLogicalAssessment,
            primaryConcern = primaryConcern,
            output = outputPdf,
        )

        assertTrue(Files.exists(inputPdf))
        assertTrue(Files.size(inputPdf) > 1_000)
        assertTrue(Files.exists(outputPdf))
        assertTrue(Files.size(outputPdf) > 1_000)
        assertTrue(
            amendedCase.subject.displayName == AmlConflictCertificatePdfFixture.COMPANY_NAME,
            "Expected KYC subject to be ${AmlConflictCertificatePdfFixture.COMPANY_NAME}, got ${amendedCase.subject.displayName}",
        )
        assertTrue(
            amlScreening.hits.count { it.source == ScreeningSource.OFAC_SANCTIONS } == 3,
            "Expected 3 OFAC hits, got ${amlScreening.hits.count { it.source == ScreeningSource.OFAC_SANCTIONS }}",
        )
        assertTrue(
            amlLogicalAssessment.candidateAssessments.size == 3,
            "Expected 3 LLM candidate assessments, got ${amlLogicalAssessment.candidateAssessments.size}",
        )
        assertTrue(
            amlLogicalAssessment.overallStatus == ScreeningAssessmentStatus.MANUAL_REVIEW_REQUIRED,
            "Expected manual review status, got ${amlLogicalAssessment.overallStatus}",
        )
        assertTrue(
            finalRisk.overallRisk == RiskLevel.HIGH,
            "Expected final risk HIGH, got ${finalRisk.overallRisk}",
        )
        assertTrue(
            amendedCase.recommendation == KycRecommendation.ENHANCED_DUE_DILIGENCE,
            "Expected EDD recommendation, got ${amendedCase.recommendation}",
        )
        assertTrue(amendedCase.screeningResults.any { it.provider == "OFAC" && it.matchScore == primaryConcern.confidenceScore })
        assertTrue(amendedCase.issues.any { it.code == "POSSIBLE_SANCTIONS_MATCH" })

        val reportText = Loader.loadPDF(outputPdf.toFile()).use { document ->
            PDFTextStripper().getText(document)
        }
        assertTrue(reportText.contains("KYC AML Risk Pipeline Report"))
        assertTrue(reportText.contains(AmlConflictCertificatePdfFixture.COMPANY_NAME))
        assertTrue(reportText.contains("Baseline Risk Factors by Category"))
        assertTrue(reportText.contains("Pass 1: Baseline KYC risk before AML"))
        assertTrue(reportText.contains("Pass 2: Final KYC risk after AML enrichment"))
        assertTrue(reportText.contains("Final Risk Factors by Category"))
        assertTrue(reportText.contains("AML / SANCTIONS"))
        assertTrue(reportText.contains("Cross-factor weighted score"))
        assertTrue(reportText.contains("Required Compliance Actions"))
        assertTrue(reportText.contains("Manual AML disposition"))
        assertTrue(reportText.contains("Periodic screening cadence"))
        assertTrue(reportText.contains("Final score pie chart"))
        assertTrue(reportText.contains("Merge treatment"))
        assertTrue(reportText.contains("OFAC primary name"))
        assertTrue(reportText.contains("Primary AML Concern"))
        assertTrue(reportText.contains(primaryConcern.candidateName))
        assertTrue(reportText.contains("Final Risk: HIGH"))

        logger.info(
            "Generated KYC AML risk pipeline demo: inputPdf={}, outputPdf={}, primaryConcern={} confidence={}/{} finalRisk={}",
            inputPdf.toAbsolutePath(),
            outputPdf.toAbsolutePath(),
            primaryConcern.candidateName,
            primaryConcern.confidenceScore,
            primaryConcern.confidenceLevel,
            finalRisk.overallRisk,
        )
    }

    /**
     * First LLM call in the demo pipeline.
     *
     * The only deterministic preprocessing here is format conversion: Tika turns
     * the PDF into searchable text so ToolishRAG can expose search tools to the
     * LLM. The KYC ontology mapping itself is performed by structured LLM output.
     */
    private fun extractKycCaseWithLlm(inputPdf: Path): KycCase {
        val searchDirectory = searchableKycDirectory()
        Files.createDirectories(searchDirectory)
        Files.list(searchDirectory).use { paths ->
            paths.filter { Files.isRegularFile(it) }.forEach(Files::delete)
        }
        val searchableTextFile = searchDirectory.resolve(inputPdf.fileName.toString().removeSuffix(".pdf") + ".txt")
        Files.writeString(searchableTextFile, parsePdf(inputPdf))

        val kycDocuments = ToolishRag(
            "amlPipelineKycCertificateDocuments",
            "KYC certificate PDF parsed with Tika for legal-entity KYC extraction",
            DirectoryTextSearch(searchDirectory.toString()),
        ).withGoal(
            """
            Find the legal entity name, registration or company number, incorporation date,
            jurisdiction, registered office, issuing authority, issue place, and missing
            KYC evidence such as beneficial ownership, source of funds, and source of wealth.
            """.trimIndent(),
        )

        val prompt = """
            You are a KYC extraction agent.

            Use the available KYC document reference tools to inspect the parsed certificate
            text before producing the final KycCase object. The certificate may belong to any
            legal entity; extract only facts supported by the supplied document.

            Extraction rules:
            - Treat the primary legal entity in the certificate as the KYC customer.
            - Generate stable non-null ids by slugifying display names.
            - Use partyType value "legalEntity" for the customer.
            - Include role CUSTOMER for the KYC subject.
            - Extract registration number, jurisdiction of incorporation, incorporation date,
              registered office address, certificate issuer, issue place, and issue date when present.
            - If the certificate contains a registered office, put it in subject.addresses.
            - Do not invent beneficial owners, directors, screening results, source-of-funds, or
              source-of-wealth evidence when the certificate does not contain them.
            - If ownership evidence is missing, add a BENEFICIAL_OWNERSHIP_MISSING warning.
            - If source-of-funds or source-of-wealth evidence is missing, leave the evidence absent;
              later deterministic risk rules will score missing evidence.
            - Recommend MANUAL_REVIEW for certificate-only KYC packages.
            - Required top-level fields: caseId, subject, relatedParties, ownership, evidence,
              screeningResults, riskAssessment, recommendation, issues.
        """.trimIndent()

        return ai.withDefaultLlm()
            .withReference(kycDocuments)
            .creating(KycCase::class.java)
            .withExample("Generic certificate-only legal entity KYC case", genericCertificateOnlyKycCaseExample())
            .fromPrompt(prompt)
    }

    private fun screenAml(kycCase: KycCase): ScreeningAssessment {
        val subject = requireNotNull(kycCase.subject as? LegalEntity)
        val screeningSubject = ScreeningSubject(
            id = subject.id,
            displayName = subject.displayName,
            subjectType = ScreeningSubjectType.LEGAL_ENTITY,
            countries = setOfNotNull(subject.jurisdictionOfIncorporation),
            identifiers = setOfNotNull(
                subject.registrationNumber?.let {
                    ScreeningIdentifier(
                        type = ScreeningIdentifierType.COMPANY_REGISTRATION_NUMBER,
                        value = it,
                        issuingCountry = subject.jurisdictionOfIncorporation,
                    )
                },
            ),
        )

        return AmlScreeningService(
            providers = listOf(
                StaticScreeningProvider(
                    providerId = "eu-not-loaded-for-ofac-pipeline-test",
                    source = ScreeningSource.EU_FINANCIAL_SANCTIONS,
                    entries = emptyList(),
                ),
                OfacSanctionsXmlProvider(
                    providerId = "ofac-real-file",
                    xmlPath = requiredRealFile("AML_OFAC_SDN_XML"),
                ),
            ),
        ).screen(screeningSubject)
    }

    private fun assessAmlWithLlm(amlScreening: ScreeningAssessment): AmlLogicalScreeningAssessment {
        val candidateEvidence = amlScreening.hits
            .filter { it.source == ScreeningSource.OFAC_SANCTIONS }
            .joinToString("\n\n") { hit ->
                """
                    Candidate:
                    OFAC source record id: ${hit.sourceRecordId}
                    OFAC primary name: ${hit.primaryName}
                    Matched OFAC alias/name: ${hit.matchedName}
                    Deterministic disposition: ${hit.disposition}
                    Deterministic rationale: ${hit.rationale}
                """.trimIndent()
            }

        val prompt = """
            You are assisting an AML analyst with case disposition for sanctions screening hits.

            Apply these logical rules. Do not use numeric weights:
            - Assess each candidate separately because the KYC subject may or may not be the same entity as any
              individual sanctions candidate.
            - A near-exact legal name or sanctions-list alias match is material and must not be ignored.
            - Missing company registration-number agreement prevents confirming the match.
            - Conflicting jurisdiction or country evidence lowers confidence.
            - Do not recommend CONFIRMED_MATCH unless strong secondary identifiers agree.
            - Do not recommend LIKELY_FALSE_POSITIVE when the name or alias evidence is very strong and
              the only problems are missing or conflicting secondary evidence.
            - Compute confidenceScore yourself as a number from 0.0 to 1.0 using the logical evidence.
            - ConfidenceScore is an LLM analyst estimate, not a deterministic matcher score.
            - Return exactly one candidate assessment for each candidate listed below.

            Candidate evidence:
            Subject legal name: ${amlScreening.subject.displayName}
            Subject jurisdiction: ${amlScreening.subject.countries.joinToString()}
            Subject registration number: ${amlScreening.subject.identifiers.joinToString { it.value }}
            Overall deterministic status: ${amlScreening.status}

            $candidateEvidence

            Return a structured subject-level assessment with:
            subjectName, overallStatus, candidateAssessments, rationale, analystAction.
            Each candidate assessment must include:
            sourceRecordId, candidateName, confidenceScore, confidenceLevel, recommendedDisposition, rationale, missingEvidence.
        """.trimIndent()

        return ai.withDefaultLlm()
            .creating(AmlLogicalScreeningAssessment::class.java)
            .withExample(
                "Generic possible corporate sanctions matches",
                AmlCorporateConflictSupport.genericPossibleCorporateScreeningExample(),
            )
            .fromPrompt(prompt)
    }

    private fun amendKycCase(
        kycCase: KycCase,
        amlScreening: ScreeningAssessment,
        amlLogicalAssessment: AmlLogicalScreeningAssessment,
        primaryConcern: AmlLogicalCandidateAssessment,
    ): KycCase {
        val screeningResults = amlLogicalAssessment.candidateAssessments.map { candidate ->
            val hit = amlScreening.hitFor(candidate)
            val merge = amlMergeDecision(kycCase, hit, candidate)
            ScreeningResult(
                partyId = kycCase.subject.id,
                type = ScreeningType.SANCTIONS,
                status = candidate.recommendedDisposition.toKycScreeningStatus(),
                provider = "OFAC",
                matchScore = candidate.confidenceScore,
                rationale = merge.rationale,
                evidence = EvidenceReference(
                    documentId = "OFAC:${candidate.sourceRecordId}",
                    page = 1,
                    excerpt = merge.evidenceExcerpt,
                    confidence = candidate.confidenceScore,
                ),
                screenedAt = Instant.now(),
            )
        }
        val caseWithAmlScreening = kycCase.copy(screeningResults = kycCase.screeningResults + screeningResults)
        val finalRisk = finalRiskAssessment(caseWithAmlScreening, amlScreening, amlLogicalAssessment, primaryConcern)

        return caseWithAmlScreening.copy(
            riskAssessment = finalRisk,
            recommendation = KycRecommendation.ENHANCED_DUE_DILIGENCE,
            issues = kycCase.issues + DataQualityIssue(
                severity = IssueSeverity.WARNING,
                code = "POSSIBLE_SANCTIONS_MATCH",
                message = "Primary AML concern '${primaryConcern.candidateName}' requires manual review.",
                fieldPath = "screeningResults",
            ),
        )
    }

    private fun finalRiskAssessment(
        kycCase: KycCase,
        amlScreening: ScreeningAssessment,
        amlLogicalAssessment: AmlLogicalScreeningAssessment,
        primaryConcern: AmlLogicalCandidateAssessment,
    ): RiskAssessment {
        val baseline = BaselineKycRiskMethodologyRule.assess(kycCase)
        val retainedFactors = baseline.factors.filterNot { it.type == RiskFactorType.SANCTIONS }
        val primaryHit = amlScreening.hitFor(primaryConcern)
        val merge = amlMergeDecision(kycCase, primaryHit, primaryConcern)
        val sanctionsLevel = AmlRiskMethodology.sanctionsRiskLevel(primaryConcern)
        val factors = retainedFactors + RiskFactor(
            type = RiskFactorType.SANCTIONS,
            level = sanctionsLevel,
            rationale = "OFAC sanctions screening produced ${amlScreening.hits.size} candidate hits. " +
                "The highest-confidence LLM assessment is ${primaryConcern.confidenceScore}/${primaryConcern.confidenceLevel} " +
                "with disposition ${primaryConcern.recommendedDisposition}; manual review is required before identity confirmation.",
            evidence = EvidenceReference(
                documentId = "OFAC:${primaryConcern.sourceRecordId}",
                page = 1,
                excerpt = merge.evidenceExcerpt,
                confidence = primaryConcern.confidenceScore,
            ),
        )
        return RiskAssessment(
            overallRisk = if (factors.any { it.level == RiskLevel.HIGH }) RiskLevel.HIGH else RiskLevel.MEDIUM,
            score = aggregateScore(factors),
            factors = factors,
            rationale = "Final risk recomputed after AML enrichment. Possible OFAC matches increase the case to high risk and require enhanced due diligence.",
            methodology = KycRiskScoringMethodology.description +
                " Final AML-adjusted risk starts from baseline KYC factors, replaces the pre-screening sanctions readiness factor with LLM-dispositioned AML sanctions risk, applies merge rules based on LLM confidence/disposition, and recomputes the cross-factor weighted 0-100 score.",
        )
    }

    private fun amlMergeDecision(
        kycCase: KycCase,
        hit: ScreeningHit,
        candidate: AmlLogicalCandidateAssessment,
    ): AmlMergeDecision {
        val subject = requireNotNull(kycCase.subject as? LegalEntity)
        val kycAddresses = subject.addresses.map { it.reportLine() }.ifEmpty { listOf("not present in KYC case") }
        val ofacAddresses = hit.addresses.ifEmpty { setOf("not available in normalized OFAC hit") }
        val treatment = when {
            candidate.recommendedDisposition == ScreeningDisposition.CONFIRMED_MATCH ||
                candidate.confidenceLevel == AmlLlmConfidenceLevel.HIGH ->
                "confirmed AML identity enrichment"
            candidate.recommendedDisposition == ScreeningDisposition.POSSIBLE_MATCH ||
                candidate.confidenceLevel == AmlLlmConfidenceLevel.MEDIUM ->
                "provisional AML enrichment for manual review"
            else ->
                "screening evidence retained without identity merge"
        }
        val missingEvidence = candidate.missingEvidence
            .takeIf { it.isNotEmpty() }
            ?.joinToString()
            ?: "none stated"
        val rationale =
            "Merge treatment: $treatment based on LLM confidence ${candidate.confidenceScore}/${candidate.confidenceLevel} " +
                "and disposition ${candidate.recommendedDisposition}. KYC subject name='${subject.displayName}'. " +
                "OFAC primary name='${hit.primaryName}'. Matched OFAC alias/name='${hit.matchedName}'. " +
                "KYC registration='${subject.registrationNumber.orEmpty()}'. OFAC identifiers='${hit.identifierSummary()}'. " +
                "KYC addresses='${kycAddresses.joinToString(" | ")}'. OFAC addresses='${ofacAddresses.joinToString(" | ")}'. " +
                "Conflicts/missing evidence: $missingEvidence. ${candidate.rationale}"
        return AmlMergeDecision(
            treatment = treatment,
            rationale = rationale,
            evidenceExcerpt = "KYC '${subject.displayName}' screened against OFAC '${hit.primaryName}' using matched alias '${hit.matchedName}'. Treatment=$treatment; confidence=${candidate.confidenceScore}/${candidate.confidenceLevel}; disposition=${candidate.recommendedDisposition}.",
        )
    }

    private fun ScreeningAssessment.hitFor(candidate: AmlLogicalCandidateAssessment): ScreeningHit =
        requireNotNull(hits.firstOrNull { it.sourceRecordId == candidate.sourceRecordId }) {
            "No screening hit found for LLM candidate ${candidate.sourceRecordId}"
        }

    private fun ScreeningHit.identifierSummary(): String =
        "not available in normalized screening hit"

    private fun genericCertificateOnlyKycCaseExample(): KycCase =
        KycCase(
            caseId = "certificate-only-example",
            subject = LegalEntity(
                id = "example-holdings-limited",
                displayName = "Example Holdings Limited",
                registrationNumber = "EXAMPLE-123",
                jurisdictionOfIncorporation = "GB",
                incorporationDate = LocalDate.parse("2020-01-15"),
                addresses = listOf(
                    Address(
                        line1 = "1 Registry Street",
                        city = "London",
                        postalCode = "EC1A 1AA",
                        countryCode = "GB",
                    ),
                ),
                roles = setOf(PartyRole.CUSTOMER),
            ),
            evidence = listOf(
                DocumentEvidence(
                    documentId = "example-certificate.txt",
                    type = DocumentType.CERTIFICATE_OF_INCORPORATION,
                    fileName = "example-certificate.txt",
                    issuer = "Example Companies Registry",
                    issuePlace = "London",
                    issueDate = LocalDate.parse("2020-01-15"),
                ),
            ),
            recommendation = KycRecommendation.MANUAL_REVIEW,
            issues = listOf(
                DataQualityIssue(
                    severity = IssueSeverity.WARNING,
                    code = "BENEFICIAL_OWNERSHIP_MISSING",
                    message = "Beneficial ownership evidence is missing from the supplied certificate-only KYC package.",
                    fieldPath = "ownership",
                ),
            ),
        )

    private fun Address.reportLine(): String =
        (
            listOfNotNull(line1, line2) +
            listOfNotNull(city, region, postalCode, countryCode)
                .filter { it.isNotBlank() && it != "N/A" && !line1.contains(it, ignoreCase = true) }
            ).joinToString(", ")

    private fun parsePdf(path: Path): String {
        val document = TikaHierarchicalContentReader().parseFile(path.toFile(), path.toUri().toString())
        return document.leaves().joinToString("\n") { it.content }
    }

    private fun inputPdfPath(): Path =
        moduleDirectory()
            .resolve("target")
            .resolve("kycdemo")
            .resolve("aml")
            .resolve("input")
            .resolve(AmlConflictCertificatePdfFixture.INPUT_FILE_NAME)

    private fun searchableKycDirectory(): Path =
        moduleDirectory()
            .resolve("target")
            .resolve("kycdemo")
            .resolve("aml")
            .resolve("rag")

    private fun copyInputPdfResourceToTarget(): Path {
        val resource = requireNotNull(javaClass.classLoader.getResource(KINGS_ROMANS_CERTIFICATE_RESOURCE)) {
            "Missing test resource: $KINGS_ROMANS_CERTIFICATE_RESOURCE"
        }
        val output = inputPdfPath()
        Files.createDirectories(output.parent)
        Files.copy(Paths.get(resource.toURI()), output, StandardCopyOption.REPLACE_EXISTING)
        return output
    }

    private fun outputPdfPath(inputPdf: Path): Path =
        moduleDirectory()
            .resolve("target")
            .resolve("kycdemo")
            .resolve("aml")
            .resolve("output")
            .resolve("${inputPdf.fileName.toString().removeSuffix(".pdf")}-kyc-aml-risk-report.pdf")

    private fun moduleDirectory(): Path {
        val currentDirectory = Paths.get("").toAbsolutePath()
        return if (currentDirectory.fileName.toString() == "meta-agent-service") {
            currentDirectory
        } else {
            currentDirectory.resolve("meta-agent-service")
        }
    }

    private fun requiredRealFile(environmentVariable: String): Path {
        val configured = requireNotNull(System.getenv(environmentVariable)) {
            "$environmentVariable must point to a local sanctions XML file"
        }
        val path = Paths.get(configured)
        require(Files.isRegularFile(path)) { "$environmentVariable must point to an existing regular file: $path" }
        return path
    }

    private fun ScreeningDisposition.toKycScreeningStatus(): ScreeningStatus =
        when (this) {
            ScreeningDisposition.CONFIRMED_MATCH -> ScreeningStatus.CONFIRMED_MATCH
            ScreeningDisposition.POSSIBLE_MATCH -> ScreeningStatus.POSSIBLE_MATCH
            ScreeningDisposition.LIKELY_FALSE_POSITIVE -> ScreeningStatus.CLEAR
        }

    private fun aggregateScore(factors: List<RiskFactor>): Int {
        val weighted = factors.map { factor ->
            val weight = factor.type.weight()
            KycRiskScoringMethodology.levelScore(factor.level) * weight to weight
        }
        val totalWeight = weighted.sumOf { it.second }
        if (totalWeight == 0) {
            return KycRiskScoringMethodology.levelScore(RiskLevel.UNKNOWN)
        }
        return (weighted.sumOf { it.first }.toDouble() / totalWeight).toInt()
    }

    private fun RiskFactorType.weight(): Int =
        KycRiskScoringMethodology.factorWeight(this)

    companion object {
        private const val KINGS_ROMANS_CERTIFICATE_RESOURCE =
            "kycdemo/screening/${AmlConflictCertificatePdfFixture.INPUT_FILE_NAME}"
        private val logger = LoggerFactory.getLogger(KycAmlRiskPipelineIntegrationTest::class.java)

        @BeforeAll
        @JvmStatic
        fun setUp() {
            System.setProperty("embabel.agent.shell.interactive.enabled", "false")
        }
    }
}

private data class AmlMergeDecision(
    val treatment: String,
    val rationale: String,
    val evidenceExcerpt: String,
)

private object KycAmlPipelinePdfRenderer {

    private val regularFont = PDType1Font(Standard14Fonts.FontName.HELVETICA)
    private val boldFont = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)

    fun render(
        inputPdf: Path,
        kycCase: KycCase,
        baselineRisk: RiskAssessment,
        amlScreening: ScreeningAssessment,
        amlLogicalAssessment: AmlLogicalScreeningAssessment,
        primaryConcern: AmlLogicalCandidateAssessment,
        output: Path,
    ) {
        Files.createDirectories(output.parent)
        PDDocument().use { document ->
            val writer = PdfWriter(document)
            writer.heading("KYC AML Risk Pipeline Report")
            writer.line("Input PDF", inputPdf.toAbsolutePath().toString())
            writer.line("Generated at", Instant.now().toString())

            writer.section("KYC Subject")
            writer.line("Name", kycCase.subject.displayName)
            val subject = requireNotNull(kycCase.subject as? LegalEntity)
            writer.line("Registration number", subject.registrationNumber.orEmpty())
            writer.line("Jurisdiction", subject.jurisdictionOfIncorporation.orEmpty())
            writer.line("Incorporation date", subject.incorporationDate.toString())

            writer.section("Pass 1: Baseline KYC risk before AML")
            writer.line("Overall Risk", baselineRisk.overallRisk.name)
            writer.line("Score", baselineRisk.score.toString())
            writer.crossFactorScore(baselineRisk)
            writer.riskFactorsByCategory("Baseline Risk Factors by Category", baselineRisk)

            writer.section("AML Candidates")
            amlLogicalAssessment.candidateAssessments.forEach { candidate ->
                writer.line(
                    "Candidate ${candidate.sourceRecordId}",
                    "${candidate.candidateName} ${candidate.confidenceScore}/${candidate.confidenceLevel} ${candidate.recommendedDisposition}",
                )
                writer.paragraph(candidate.rationale)
            }

            writer.section("Primary AML Concern")
            writer.line("Candidate", primaryConcern.candidateName)
            writer.line("OFAC record", primaryConcern.sourceRecordId)
            writer.line("Confidence", "${primaryConcern.confidenceScore}/${primaryConcern.confidenceLevel}")
            writer.line("Disposition", primaryConcern.recommendedDisposition.name)
            writer.paragraph(primaryConcern.rationale)

            writer.section("Pass 2: Final KYC risk after AML enrichment")
            val finalRisk = requireNotNull(kycCase.riskAssessment)
            writer.line("Final Risk", finalRisk.overallRisk.name)
            writer.line("Score", finalRisk.score.toString())
            writer.line("Recommendation", kycCase.recommendation.toString())
            writer.paragraph(finalRisk.rationale)
            writer.requiredComplianceActions(kycCase, amlLogicalAssessment)
            writer.crossFactorScore(finalRisk)
            writer.finalScorePieChart(finalRisk)
            writer.riskFactorsByCategory(
                title = "Final Risk Factors by Category",
                riskAssessment = finalRisk,
                amlScreening = amlScreening,
                amlLogicalAssessment = amlLogicalAssessment,
                primaryConcern = primaryConcern,
            )

            writer.section("Amended KYC Case")
            writer.amlMergeResults(kycCase, amlScreening, amlLogicalAssessment)
            writer.line("Screening results", kycCase.screeningResults.size.toString())
            writer.line("Issues", kycCase.issues.joinToString { it.code })
            writer.line("Raw AML hits", amlScreening.hits.size.toString())

            writer.close()
            document.save(output.toFile())
        }
    }

    private class PdfWriter(
        private val document: PDDocument,
    ) {
        private val margin = 54f
        private var y = 0f
        private var contentStream: PDPageContentStream? = null

        init {
            newPage()
        }

        fun heading(text: String) {
            write(text, boldFont, 18f, 24f)
        }

        fun section(text: String) {
            y -= 8f
            write(text, boldFont, 13f, 18f)
        }

        fun line(label: String, value: String) {
            paragraph("$label: $value")
        }

        fun paragraph(text: String) {
            wrappedLines(text, 92).forEach {
                write(it, regularFont, 10.5f, 14f)
            }
        }

        fun riskFactorsByCategory(
            title: String,
            riskAssessment: RiskAssessment,
            amlScreening: ScreeningAssessment? = null,
            amlLogicalAssessment: AmlLogicalScreeningAssessment? = null,
            primaryConcern: AmlLogicalCandidateAssessment? = null,
        ) {
            section(title)
            line("Methodology", riskAssessment.methodology.ifBlank { "not specified" })
            RiskFactorType.entries.forEach { category ->
                val factors = riskAssessment.factors.filter { it.type == category }
                if (factors.isNotEmpty()) {
                    riskCategory(category, factors, amlScreening, amlLogicalAssessment, primaryConcern)
                }
            }
        }

        fun crossFactorScore(riskAssessment: RiskAssessment) {
            section("Cross-factor weighted score")
            line("Formula", "sum(weighted points) / sum(factor weights)")
            line("Weighted points", "levelScore x factor weight; this is the numerator contribution, not the final score")
            line("Level scores", KycRiskScoringMethodology.levelScoreDescription)
            line("Weights", KycRiskScoringMethodology.factorWeightDescription)
            val totalWeight = riskAssessment.factors.sumOf { it.type.reportWeight() }
            val totalWeightedPoints = riskAssessment.factors.sumOf {
                it.level.reportScore() * it.type.reportWeight()
            }
            riskAssessment.factors.forEach { factor ->
                val weight = factor.type.reportWeight()
                val levelScore = factor.level.reportScore()
                line(
                    factor.type.reportLabel(),
                    "level=${factor.level}, levelScore=$levelScore, weight=$weight, weightedPoints=${levelScore * weight}",
                )
            }
            line("Total weighted points", totalWeightedPoints.toString())
            line("Total weight", totalWeight.toString())
            line("Computed score", riskAssessment.score?.toString().orEmpty())
        }

        fun finalScorePieChart(riskAssessment: RiskAssessment) {
            section("Final score pie chart")
            paragraph("Slice size is each factor's weighted points as a percentage of total weighted points.")
            val slices = riskAssessment.factors.map { factor ->
                PieSlice(
                    label = factor.type.reportLabel(),
                    points = factor.level.reportScore() * factor.type.reportWeight(),
                    color = factor.type.reportColor(),
                )
            }
            val totalPoints = slices.sumOf { it.points }
            if (totalPoints <= 0) {
                line("Chart", "not available")
                return
            }

            ensureSpace(170f)
            val stream = requireNotNull(contentStream)
            val centerX = margin + 74f
            val centerY = y - 78f
            val radius = 48f
            var startAngle = -90.0
            slices.forEach { slice ->
                val sweep = 360.0 * slice.points.toDouble() / totalPoints.toDouble()
                stream.setNonStrokingColor(slice.color)
                drawPieSlice(stream, centerX, centerY, radius, startAngle, sweep)
                startAngle += sweep
            }

            var legendY = y - 24f
            val legendX = margin + 150f
            slices.forEach { slice ->
                val percent = 100.0 * slice.points.toDouble() / totalPoints.toDouble()
                stream.setNonStrokingColor(slice.color)
                stream.addRect(legendX, legendY - 8f, 8f, 8f)
                stream.fill()
                writeAt(
                    text = "${slice.label}: ${"%.1f".format(percent)}% (${slice.points} weighted points)",
                    x = legendX + 14f,
                    y = legendY - 7f,
                    font = regularFont,
                    fontSize = 8.5f,
                )
                legendY -= 14f
            }
            stream.setNonStrokingColor(Color.BLACK)
            y -= 160f
        }

        fun amlMergeResults(
            kycCase: KycCase,
            amlScreening: ScreeningAssessment,
            amlLogicalAssessment: AmlLogicalScreeningAssessment,
        ) {
            section("AML Merge Results")
            val subject = requireNotNull(kycCase.subject as? LegalEntity)
            val kycAddress = subject.addresses.joinToString(" | ") { it.reportLine() }.ifBlank { "not present" }

            amlLogicalAssessment.candidateAssessments
                .sortedByDescending { it.confidenceScore }
                .forEachIndexed { index, candidate ->
                    val hit = amlScreening.hits.firstOrNull { it.sourceRecordId == candidate.sourceRecordId }
                    section("Candidate ${index + 1}: ${candidate.sourceRecordId}")
                    line("Merge treatment", candidate.mergeTreatment())
                    line("Disposition", candidate.recommendedDisposition.name)
                    line("LLM confidence", "${candidate.confidenceScore}/${candidate.confidenceLevel}")
                    line("KYC subject", subject.displayName)
                    line("OFAC primary name", candidate.candidateName)
                    line("Matched alias", hit?.matchedName ?: "not available")
                    line("KYC registration", subject.registrationNumber.orEmpty())
                    line("KYC address", kycAddress)
                    line("OFAC address", hit?.addresses?.joinToString(" | ").orEmpty().ifBlank { "not available" })
                    line("Missing evidence", candidate.missingEvidence.joinToString().ifBlank { "none stated" })
                    writerSafeParagraph("LLM rationale", candidate.rationale)
                }
        }

        fun requiredComplianceActions(
            kycCase: KycCase,
            amlLogicalAssessment: AmlLogicalScreeningAssessment,
        ) {
            section("Required Compliance Actions")
            line("Case recommendation", kycCase.recommendation.toString())
            line("Manual AML disposition", manualDispositionRequirement(amlLogicalAssessment))
            line("Periodic screening cadence", periodicScreeningCadence(kycCase, amlLogicalAssessment))
            line("EDD action", "Obtain beneficial ownership, registration corroboration, and address evidence before onboarding decision")
        }

        private fun writerSafeParagraph(label: String, text: String) {
            line(label, text)
        }

        private fun riskCategory(
            category: RiskFactorType,
            factors: List<RiskFactor>,
            amlScreening: ScreeningAssessment?,
            amlLogicalAssessment: AmlLogicalScreeningAssessment?,
            primaryConcern: AmlLogicalCandidateAssessment?,
        ) {
            section(category.reportLabel())
            factors.forEach { factor ->
                line("Level", factor.level.name)
                factor.evidence?.let { evidence ->
                    line("Evidence source", evidence.documentId)
                    line("Evidence extraction confidence", evidence.confidence.toString())
                }
                if (category == RiskFactorType.SANCTIONS && amlLogicalAssessment != null) {
                    amlSanctionsFactor(factor, amlScreening, amlLogicalAssessment, primaryConcern)
                } else {
                    paragraph(factor.rationale)
                }
            }
            if (category == RiskFactorType.SANCTIONS && amlLogicalAssessment != null) {
                line("AML disposition", amlLogicalAssessment.overallStatus.name)
                primaryConcern?.let {
                    line("Primary AML subcategory", "OFAC sanctions possible match")
                    line("Primary candidate", "${it.sourceRecordId} ${it.candidateName}")
                    line("LLM confidence", "${it.confidenceScore}/${it.confidenceLevel}")
                    paragraph(it.rationale)
                }
                amlLogicalAssessment.candidateAssessments
                    .sortedByDescending { it.confidenceScore }
                    .forEach { candidate ->
                        line(
                            "AML candidate ${candidate.sourceRecordId}",
                            "${candidate.candidateName} ${candidate.confidenceScore}/${candidate.confidenceLevel} ${candidate.recommendedDisposition}",
                        )
                    }
            }
        }

        private fun amlSanctionsFactor(
            factor: RiskFactor,
            amlScreening: ScreeningAssessment?,
            amlLogicalAssessment: AmlLogicalScreeningAssessment,
            primaryConcern: AmlLogicalCandidateAssessment?,
        ) {
            paragraph(factor.rationale)
            primaryConcern?.let { candidate ->
                val hit = amlScreening?.hits?.firstOrNull { it.sourceRecordId == candidate.sourceRecordId }
                line("Merge treatment", candidate.mergeTreatment())
                line("Subject legal name", amlScreening?.subject?.displayName.orEmpty().ifBlank { amlLogicalAssessment.subjectName })
                line("OFAC primary name", hit?.primaryName ?: candidate.candidateName)
                line("Matched OFAC alias/name", hit?.matchedName ?: "not available")
                line("OFAC record", candidate.sourceRecordId)
                line("LLM confidence", "${candidate.confidenceScore}/${candidate.confidenceLevel}")
                line("Disposition", candidate.recommendedDisposition.name)
                line("OFAC address", hit?.addresses?.joinToString(" | ").orEmpty().ifBlank { "not available" })
                line("Missing evidence", candidate.missingEvidence.joinToString().ifBlank { "none stated" })
                line("Analyst action", amlLogicalAssessment.analystAction)
            }
        }

        fun close() {
            contentStream?.close()
            contentStream = null
        }

        private fun write(text: String, font: PDType1Font, fontSize: Float, lineHeight: Float) {
            if (y < margin + lineHeight) {
                newPage()
            }
            val stream = requireNotNull(contentStream)
            stream.beginText()
            stream.setFont(font, fontSize)
            stream.newLineAtOffset(margin, y)
            stream.showText(text.pdfSafe())
            stream.endText()
            y -= lineHeight
        }

        private fun writeAt(text: String, x: Float, y: Float, font: PDType1Font, fontSize: Float) {
            val stream = requireNotNull(contentStream)
            stream.beginText()
            stream.setFont(font, fontSize)
            stream.newLineAtOffset(x, y)
            stream.showText(text.pdfSafe())
            stream.endText()
        }

        private fun ensureSpace(height: Float) {
            if (y < margin + height) {
                newPage()
            }
        }

        private fun drawPieSlice(
            stream: PDPageContentStream,
            centerX: Float,
            centerY: Float,
            radius: Float,
            startAngle: Double,
            sweep: Double,
        ) {
            val segments = maxOf(2, (sweep / 10.0).toInt())
            stream.moveTo(centerX, centerY)
            for (index in 0..segments) {
                val angle = (startAngle + (sweep * index / segments)) * PI / 180.0
                stream.lineTo(
                    centerX + (cos(angle) * radius).toFloat(),
                    centerY + (sin(angle) * radius).toFloat(),
                )
            }
            stream.closePath()
            stream.fill()
        }

        private fun newPage() {
            contentStream?.close()
            val page = PDPage(PDRectangle.LETTER)
            document.addPage(page)
            contentStream = PDPageContentStream(document, page)
            y = PDRectangle.LETTER.height - margin
        }

        private fun wrappedLines(text: String, maxLength: Int): List<String> {
            val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
            val lines = mutableListOf<String>()
            var current = ""
            words.forEach { word ->
                val candidate = if (current.isBlank()) word else "$current $word"
                if (candidate.length > maxLength) {
                    if (current.isNotBlank()) {
                        lines += current
                    }
                    current = word
                } else {
                    current = candidate
                }
            }
            if (current.isNotBlank()) {
                lines += current
            }
            return lines
        }

        private fun String.pdfSafe(): String =
            replace(Regex("[\\r\\n\\t]+"), " ")
                .replace(Regex("[^\\x20-\\x7E]"), "?")
                .take(110)

        private fun RiskFactorType.reportLabel(): String =
            when (this) {
                RiskFactorType.SANCTIONS -> "AML / SANCTIONS"
                RiskFactorType.SOURCE_OF_FUNDS -> "SOURCE OF FUNDS"
                RiskFactorType.SOURCE_OF_WEALTH -> "SOURCE OF WEALTH"
                RiskFactorType.DOCUMENT_QUALITY -> "DOCUMENT QUALITY"
                else -> name
            }

        private fun RiskLevel.reportScore(): Int =
            KycRiskScoringMethodology.levelScore(this)

        private fun RiskFactorType.reportWeight(): Int =
            KycRiskScoringMethodology.factorWeight(this)

        private fun RiskFactorType.reportColor(): Color =
            when (this) {
                RiskFactorType.GEOGRAPHY -> Color(0x4E, 0x79, 0xA7)
                RiskFactorType.OWNERSHIP -> Color(0xE1, 0x57, 0x59)
                RiskFactorType.DOCUMENT_QUALITY -> Color(0x59, 0xA1, 0x4F)
                RiskFactorType.SOURCE_OF_FUNDS -> Color(0xF2, 0x8E, 0x2B)
                RiskFactorType.SOURCE_OF_WEALTH -> Color(0xB0, 0x7A, 0xA1)
                RiskFactorType.SANCTIONS -> Color(0xD3, 0x72, 0x95)
                else -> Color(0x76, 0xB7, 0xB2)
            }

        private fun AmlLogicalCandidateAssessment.mergeTreatment(): String =
            when {
                recommendedDisposition == ScreeningDisposition.CONFIRMED_MATCH ||
                    confidenceLevel == AmlLlmConfidenceLevel.HIGH ->
                    "confirmed AML identity enrichment"
                recommendedDisposition == ScreeningDisposition.POSSIBLE_MATCH ||
                    confidenceLevel == AmlLlmConfidenceLevel.MEDIUM ->
                    "provisional AML enrichment for manual review"
                else ->
                    "screening evidence retained without identity merge"
            }

        private fun manualDispositionRequirement(amlLogicalAssessment: AmlLogicalScreeningAssessment): String =
            when (amlLogicalAssessment.overallStatus) {
                ScreeningAssessmentStatus.CLEAR -> "not required by AML screening result"
                ScreeningAssessmentStatus.MANUAL_REVIEW_REQUIRED -> "required before approval because one or more AML candidates remain possible matches"
                ScreeningAssessmentStatus.REJECT -> "required for rejection approval and audit record"
            }

        private fun periodicScreeningCadence(
            kycCase: KycCase,
            amlLogicalAssessment: AmlLogicalScreeningAssessment,
        ): String {
            val risk = kycCase.riskAssessment?.overallRisk ?: RiskLevel.UNKNOWN
            return when {
                amlLogicalAssessment.overallStatus == ScreeningAssessmentStatus.REJECT ->
                    AmlRiskMethodology.rejectCadence
                amlLogicalAssessment.overallStatus == ScreeningAssessmentStatus.MANUAL_REVIEW_REQUIRED || risk == RiskLevel.HIGH ->
                    AmlRiskMethodology.highRiskCadence
                risk == RiskLevel.MEDIUM ->
                    AmlRiskMethodology.mediumRiskCadence
                risk == RiskLevel.LOW ->
                    AmlRiskMethodology.lowRiskCadence
                else ->
                    AmlRiskMethodology.unknownRiskCadence
            }
        }

        private fun Address.reportLine(): String {
            val suffix = listOf(city, region, postalCode, countryCode)
                .filterNotNull()
                .filter { it.isNotBlank() && it != "N/A" && !line1.contains(it, ignoreCase = true) }
            return (listOf(line1, line2).filterNotNull() + suffix).joinToString(", ")
        }

        private data class PieSlice(
            val label: String,
            val points: Int,
            val color: Color,
        )
    }
}
