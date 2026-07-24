package com.embabel.metaagent.kycdemo

import com.embabel.agent.api.common.Ai
import com.embabel.agent.rag.ingestion.TikaHierarchicalContentReader
import com.embabel.agent.rag.service.support.DirectoryTextSearch
import com.embabel.agent.rag.tools.ToolishRag
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate

object KycCertificatePdfExtractionSupport {

    private const val CERTIFICATE_RESOURCE = "kycdemo/090151b2807a4b8f.pdf"
    private const val CERTIFICATE_TEXT_FILE = "certificate-of-incorporation.txt"

    private val reader = TikaHierarchicalContentReader()

    fun extractCertificateKycCase(ai: Ai, extractedDocumentDir: Path): KycCase {
        extractPdfResourceToSearchText(CERTIFICATE_RESOURCE, CERTIFICATE_TEXT_FILE, extractedDocumentDir)

        val certificateDocuments = ToolishRag(
            "kycCertificateDocuments",
            "Certificate of incorporation PDF parsed with Tika for KYC ontology extraction",
            DirectoryTextSearch(extractedDocumentDir.toString()),
        ).withGoal(
            """
            Find legal entity names, registration numbers, incorporation dates, jurisdictions,
            legal form, issuing authority, certificate issue place, registered office information,
            and missing KYC evidence such as ownership and control.
            """.trimIndent(),
        )

        val prompt = """
            You are a KYC extraction agent.

            Use the available KYC document reference tools to inspect the parsed document text before
            producing the final object.

            Extraction rules:
            - Build one KycCase for the primary customer evidenced by the provided KYC documents.
            - Treat the primary KYC subject as the customer.
            - Extract only facts supported by the supplied documents.
            - Do not invent an address, county, beneficial owner, director, signatory, screening result,
              source of funds, or source of wealth when the documents do not contain it.
            - Generate a stable non-null subject id by slugifying the displayName.
            - If a certificate has a place of issue or certification, record it as document evidence,
              not as the customer's address, registered office, or county.
            - If the package contains only incorporation evidence and no ownership, control, screening,
              or source-of-funds evidence, set overall risk to UNKNOWN, recommend manual review, and
              add a warning that beneficial ownership evidence is missing.
        """.trimIndent()

        return ai.withDefaultLlm()
            .withReference(certificateDocuments)
            .creating(KycCase::class.java)
            .withExample("Certificate-only legal entity KYC case", certificateOnlyKycCaseExample())
            .fromPrompt(prompt)
    }

    private fun extractPdfResourceToSearchText(resourcePath: String, outputFileName: String, outputDir: Path) {
        val resource = requireNotNull(KycCertificatePdfExtractionSupport::class.java.classLoader.getResource(resourcePath)) {
            "Missing test resource: $resourcePath"
        }
        val file = Paths.get(resource.toURI()).toFile()
        val document = reader.parseFile(file, resource.toURI().toString())
        val text = document.leaves().joinToString("\n") { it.content }
        check(text.isNotBlank()) { "No extractable text found in $resourcePath" }
        Files.writeString(outputDir.resolve(outputFileName), text)
    }

    private fun certificateOnlyKycCaseExample(): KycCase =
        KycCase(
            caseId = "certificate-only-example",
            subject = LegalEntity(
                id = "example-holdings-ltd",
                displayName = "Example Holdings Ltd",
                registrationNumber = "EXAMPLE-123",
                jurisdictionOfIncorporation = "GB",
                incorporationDate = LocalDate.parse("2020-01-15"),
                roles = setOf(PartyRole.CUSTOMER),
            ),
            evidence = listOf(
                DocumentEvidence(
                    documentId = "certificate-of-incorporation.txt",
                    type = DocumentType.CERTIFICATE_OF_INCORPORATION,
                    fileName = "certificate-of-incorporation.txt",
                    issuer = "Companies Registry",
                    issuePlace = "Registry City",
                    issueDate = LocalDate.parse("2020-01-15"),
                ),
            ),
            riskAssessment = RiskAssessment(
                overallRisk = RiskLevel.UNKNOWN,
                rationale = "Certificate-only package does not provide ownership, control, screening, source-of-funds, or source-of-wealth evidence.",
            ),
            recommendation = KycRecommendation.MANUAL_REVIEW,
            issues = listOf(
                DataQualityIssue(
                    severity = IssueSeverity.WARNING,
                    code = "BENEFICIAL_OWNERSHIP_MISSING",
                    message = "Beneficial ownership evidence is missing from the supplied KYC package.",
                    fieldPath = "ownership",
                ),
            ),
        )
}
