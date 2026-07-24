package com.embabel.metaagent.kycdemo

import com.embabel.agent.api.tool.Tool
import com.embabel.agent.rag.ingestion.TikaHierarchicalContentReader
import com.embabel.agent.rag.service.support.DirectoryTextSearch
import com.embabel.agent.rag.tools.ToolishRag
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Verifies that realistic certificate PDFs placed under test resources can be read by the
 * Embabel RAG/Tika ingestion path as-is, then exposed to ToolishRag as searchable text.
 *
 * These fixtures are intentionally not normalized into key-value text. They preserve the prose and
 * layout style of incorporation certificates so later KYC extraction tests exercise document
 * understanding rather than only structured-field parsing.
 *
 * DirectoryTextSearch is a text search implementation; it does not parse binary PDFs itself. The
 * framework sequence exercised here is therefore:
 *
 * PDF resources -> TikaHierarchicalContentReader -> extracted text -> DirectoryTextSearch -> ToolishRag.
 */
class KycPdfResourceIngestionTest {

    private val reader = TikaHierarchicalContentReader()

    @TempDir
    lateinit var extractedDocumentDir: Path

    @Test
    fun `ingest certificate PDFs with Tika and expose extracted text through ToolishRag`() {
        val irishCertificate = parseResource("kycdemo/090151b2807a4b8f.pdf")
        val ukCertificate = parseResource("kycdemo/Certificate-of-Incorporation-of-a-Private-Limited-Company.pdf")

        val irishText = irishCertificate.normalized()
        val ukText = ukCertificate.normalized()

        assertTrue(irishText.contains("short certificate of incorporation of a company"))
        assertTrue(irishText.contains("wuxi vaccines ireland limited"))
        assertTrue(irishText.contains("company number"))
        assertTrue(irishText.contains("652131"))
        assertTrue(irishText.contains("companies act 2014"))

        assertTrue(ukText.contains("certificate of incorporation"))
        assertTrue(ukText.contains("company number"))
        assertTrue(ukText.contains("10646541"))
        assertTrue(ukText.contains("the de curci trust"))
        assertTrue(ukText.contains("companies act 2006"))

        Files.writeString(extractedDocumentDir.resolve("irish-certificate.txt"), irishCertificate)
        Files.writeString(extractedDocumentDir.resolve("uk-certificate.txt"), ukCertificate)

        val certificateDocuments = ToolishRag(
            "kycCertificateDocuments",
            "Certificate of incorporation documents parsed from PDF resources for KYC extraction",
            DirectoryTextSearch(extractedDocumentDir.toString()),
        ).withGoal("Find company names, company numbers, registrar jurisdictions, and incorporation legislation.")

        val tools = certificateDocuments.tools()
        val regexSearch = tools.searchTool("regexSearch")
        val companyNumberResult = regexSearch.call(
            """{"regex":"(652131|10646541|Companies Act 2014|Companies Act 2006)","topK":10}""",
        ).content()

        assertFalse(companyNumberResult.isBlank())
        val normalizedResult = companyNumberResult.normalized()
        assertTrue(normalizedResult.contains("652131"))
        assertTrue(normalizedResult.contains("10646541"))
    }

    private fun parseResource(resourcePath: String): String {
        val resource = requireNotNull(javaClass.classLoader.getResource(resourcePath)) {
            "Missing test resource: $resourcePath"
        }
        val file = Paths.get(resource.toURI()).toFile()
        val document = reader.parseFile(file, resource.toURI().toString())
        return document.leaves().joinToString("\n") { it.content }
    }

    private fun String.normalized(): String =
        lowercase().replace(Regex("\\s+"), " ").trim()

    private fun List<Tool>.searchTool(namePart: String): Tool =
        firstOrNull { it.definition.name.contains(namePart) }
            ?: error("No $namePart tool exposed. Tools: ${map { it.definition.name }}")

    private fun Tool.Result.content(): String =
        when (this) {
            is Tool.Result.Text -> content
            else -> toString()
        }
}
