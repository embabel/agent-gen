package com.embabel.metaagent.kycdemo

import com.embabel.agent.api.common.Ai
import com.embabel.metaagent.service.MetaAgentApplication
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.text.PDFTextStripper
import org.junit.jupiter.api.Assertions.assertEquals
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
import java.lang.reflect.Method
import java.awt.Color
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.temporal.TemporalAccessor
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@SpringBootTest(classes = [MetaAgentApplication::class])
@ActiveProfiles("test")
@TestExecutionListeners(
    listeners = [DependencyInjectionTestExecutionListener::class],
    mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS,
)
/**
 * End-to-end KYC demo report test.
 *
 * This test reuses the certificate extraction flow from [KycCertificatePdfExtractionSupport],
 * converts the resulting [KycCase] into a report-oriented [KycReport], and renders a structured
 * PDF report using PDFBox.
 */
class KycCertificatePdfReportGenerationIntegrationTest {

    @Autowired
    lateinit var ai: Ai

    @TempDir
    lateinit var extractedDocumentDir: Path

    @Test
    fun `generate structured KYC report PDF from extracted certificate case`() {
        val kycCase = KycCertificatePdfExtractionSupport.extractCertificateKycCase(ai, extractedDocumentDir)
        val report = KycReportFactory.from(kycCase)

        val output = Paths.get("target", "kycdemo", "kyc-certificate-report.pdf")
        Files.createDirectories(output.parent)

        KycStructuredPdfRenderer.render(
            kycCase = kycCase,
            report = report,
            output = output,
        )

        assertTrue(Files.exists(output))
        assertTrue(Files.size(output) > 1_000)

        val text = Loader.loadPDF(output.toFile()).use { document ->
            PDFTextStripper().getText(document)
        }
        assertTrue(text.contains("KYC Structured Report"))
        assertTrue(text.contains(report.subjectName))
        assertTrue(text.contains("caseId"))

        logger.info("Generated structured KYC PDF: {}", output.toAbsolutePath())
    }

    @Test
    fun `generate structured KYC report PDF from extracted certificate case withRiskAssessment`() {
        val extractedCase = KycCertificatePdfExtractionSupport.extractCertificateKycCase(ai, extractedDocumentDir)

        val riskAssessment = BaselineKycRiskMethodologyRule.assess(extractedCase)
        val assessedCase = extractedCase.copy(
            riskAssessment = riskAssessment,
            recommendation = KycRecommendation.MANUAL_REVIEW,
        )
        val report = KycReportFactory.from(assessedCase)
        val score = requireNotNull(report.riskAssessment?.score)

        assertTrue(riskAssessment.factors.isNotEmpty())
        assertTrue(report.riskAssessment?.factors?.isNotEmpty() == true)
        assertEquals(RiskLevel.HIGH, report.riskAssessment?.overallRisk)
        assertTrue(score in 0..100)

        val output = Paths.get("target", "kycdemo", "kyc-certificate-report-with-risk-assessment.pdf")
        Files.createDirectories(output.parent)

        KycStructuredPdfRenderer.render(
            kycCase = assessedCase,
            report = report,
            output = output,
        )

        assertTrue(Files.exists(output))
        assertTrue(Files.size(output) > 1_000)

        val text = Loader.loadPDF(output.toFile()).use { document ->
            PDFTextStripper().getText(document)
        }
        assertTrue(text.contains("Baseline pre-screening KYC risk"))
        assertTrue(text.contains("Aggregate risk formula"))
        assertTrue(text.contains("score: $score"))
        assertTrue(text.contains("Risk category pie chart"))
        assertTrue(text.contains("kycCase: KycCase"))
        assertTrue(text.contains("evidence"))
        assertTrue(text.contains("CERTIFICATE_OF_INCORPORATION"))
        assertTrue(text.contains("SOURCE_OF_FUNDS"))
        assertTrue(text.contains("AML screening is not part of this certificate-ingestion test"))

        logger.info("Generated structured KYC risk PDF: {}", output.toAbsolutePath())
    }

    companion object {
        private val logger = LoggerFactory.getLogger(KycCertificatePdfReportGenerationIntegrationTest::class.java)

        @BeforeAll
        @JvmStatic
        fun setUp() {
            System.setProperty("embabel.agent.shell.interactive.enabled", "false")
        }
    }
}

private object KycStructuredPdfRenderer {

    private val regularFont = PDType1Font(Standard14Fonts.FontName.HELVETICA)
    private val boldFont = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
    private val renderedTypes = setOf(
        KycReport::class,
        KycCase::class,
        LegalEntity::class,
        Person::class,
        Address::class,
        PepProfile::class,
        OwnershipInterest::class,
        DocumentEvidence::class,
        EvidenceReference::class,
        ScreeningResult::class,
        RiskAssessment::class,
        RiskFactor::class,
        DataQualityIssue::class,
    )

    fun render(
        kycCase: KycCase,
        report: KycReport,
        output: Path,
    ) {
        PDDocument().use { document ->
            val writer = PdfReportWriter(document)

            writer.heading("KYC Structured Report")
            writer.field("Subject", kycCase.subject.displayName, 0)
            writer.field("Recommendation", kycCase.recommendation.toString(), 0)
            writer.riskCategoryDiagram(report.riskAssessment)
            writer.structured("kycCase", kycCase)
            writer.structured("report", report)

            writer.close()
            document.save(output.toFile())
        }
    }

    private fun PdfReportWriter.structured(label: String, value: Any?, depth: Int = 0) {
        when {
            value == null -> field(label, "null", depth)
            value.isScalar() -> field(label, value.toString(), depth)
            value is Iterable<*> -> collection(label, value.toList(), depth)
            value is Array<*> -> collection(label, value.toList(), depth)
            value::class in renderedTypes -> typedObject(label, value, depth)
            else -> field(label, value.toString(), depth)
        }
    }

    private fun PdfReportWriter.typedObject(label: String, value: Any, depth: Int) {
        section("$label: ${value::class.simpleName}", depth)
        value.javaClass.readableAccessors().forEach { accessor ->
            structured(accessor.propertyName(), accessor.invoke(value), depth + 1)
        }
    }

    private fun PdfReportWriter.collection(label: String, values: List<Any?>, depth: Int) {
        section("$label[${values.size}]", depth)
        if (values.isEmpty()) {
            field("items", "[]", depth + 1)
        } else {
            values.forEachIndexed { index, item ->
                structured("[$index]", item, depth + 1)
            }
        }
    }

    private fun Any.isScalar(): Boolean =
        this is String ||
            this is Number ||
            this is Boolean ||
            this is Enum<*> ||
            this is TemporalAccessor

    private fun Class<*>.readableAccessors(): List<Method> =
        methods
            .filter { it.parameterCount == 0 }
            .filter { it.name.startsWith("get") }
            .filterNot { it.name == "getClass" }
            .sortedBy { it.propertyName() }

    private fun Method.propertyName(): String =
        name.removePrefix("get").replaceFirstChar { it.lowercase() }

    private class PdfReportWriter(
        private val document: PDDocument,
    ) {

        private val margin = 54f
        private val pageWidth = PDRectangle.LETTER.width
        private val pageHeight = PDRectangle.LETTER.height
        private var y = 0f
        private var contentStream: PDPageContentStream? = null

        init {
            newPage()
        }

        fun heading(text: String) {
            write(text, boldFont, 18f, 24f)
        }

        fun riskCategoryDiagram(riskAssessment: RiskAssessment?) {
            val assessment = riskAssessment ?: return
            val factors = assessment.factors
            if (factors.isEmpty()) {
                return
            }

            val weightedFactors = factors
                .sortedBy { it.type.name }
                .map { factor -> WeightedRiskFactor(factor, factor.type.weight()) }
            val totalWeight = weightedFactors.sumOf { it.weight }
            if (totalWeight == 0) {
                return
            }

            val requiredHeight = 230f
            if (y < margin + requiredHeight) {
                newPage()
            }

            val stream = requireNotNull(contentStream)
            write("Risk category pie chart", boldFont, 13f, 18f)
            write(
                "Overall risk: ${assessment.overallRisk}, score: ${assessment.score ?: "not scored"}",
                regularFont,
                10.5f,
                16f,
            )

            val chartTop = y - 8f
            val centerX = margin + 92f
            val centerY = chartTop - 78f
            val radius = 62f
            var startDegrees = -90.0

            weightedFactors.forEachIndexed { index, weightedFactor ->
                val sweepDegrees = weightedFactor.weight.toDouble() / totalWeight.toDouble() * 360.0
                stream.pieSlice(
                    centerX = centerX,
                    centerY = centerY,
                    radius = radius,
                    startDegrees = startDegrees,
                    sweepDegrees = sweepDegrees,
                    color = weightedFactor.factor.type.chartColor(index),
                )
                startDegrees += sweepDegrees
            }
            stream.circleOutline(centerX, centerY, radius)

            val legendX = centerX + radius + 42f
            var legendY = chartTop - 14f
            weightedFactors.forEachIndexed { index, weightedFactor ->
                val factor = weightedFactor.factor
                val percentage = (weightedFactor.weight.toDouble() / totalWeight.toDouble() * 100.0).roundToInt()
                stream.legendSwatch(legendX, legendY - 3f, factor.type.chartColor(index))
                stream.textAt(
                    "${factor.type.name}: $percentage%, level ${factor.level}",
                    regularFont,
                    9f,
                    legendX + 16f,
                    legendY,
                    Color.BLACK,
                )
                legendY -= 18f
            }

            y = chartTop - 170f
        }

        fun section(text: String, depth: Int = 0) {
            y -= 8f
            write("${"  ".repeat(depth)}$text", boldFont, 13f, 18f)
        }

        fun field(label: String, value: String, depth: Int) {
            wrappedLines("${"  ".repeat(depth)}$label: $value", 92).forEach {
                write(it, regularFont, 10.5f, 14f)
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

        private fun newPage() {
            contentStream?.close()
            val page = PDPage(PDRectangle.LETTER)
            document.addPage(page)
            contentStream = PDPageContentStream(document, page)
            y = pageHeight - margin
        }

        private fun PDPageContentStream.pieSlice(
            centerX: Float,
            centerY: Float,
            radius: Float,
            startDegrees: Double,
            sweepDegrees: Double,
            color: Color,
        ) {
            val segments = 24
            setNonStrokingColor(color)
            moveTo(centerX, centerY)
            (0..segments).forEach { segment ->
                val angle = Math.toRadians(startDegrees + sweepDegrees * segment / segments)
                val x = centerX + cos(angle).toFloat() * radius
                val y = centerY + sin(angle).toFloat() * radius
                lineTo(x, y)
            }
            closePath()
            fill()
            setNonStrokingColor(Color.BLACK)
        }

        private fun PDPageContentStream.circleOutline(centerX: Float, centerY: Float, radius: Float) {
            val k = 0.55228475f
            setStrokingColor(Color(70, 70, 70))
            moveTo(centerX + radius, centerY)
            curveTo(centerX + radius, centerY + radius * k, centerX + radius * k, centerY + radius, centerX, centerY + radius)
            curveTo(centerX - radius * k, centerY + radius, centerX - radius, centerY + radius * k, centerX - radius, centerY)
            curveTo(centerX - radius, centerY - radius * k, centerX - radius * k, centerY - radius, centerX, centerY - radius)
            curveTo(centerX + radius * k, centerY - radius, centerX + radius, centerY - radius * k, centerX + radius, centerY)
            closePath()
            stroke()
            setStrokingColor(Color.BLACK)
        }

        private fun PDPageContentStream.legendSwatch(x: Float, y: Float, color: Color) {
            setNonStrokingColor(color)
            addRect(x, y, 10f, 10f)
            fill()
            setNonStrokingColor(Color.BLACK)
        }

        private fun PDPageContentStream.textAt(
            text: String,
            font: PDType1Font,
            fontSize: Float,
            x: Float,
            y: Float,
            color: Color,
        ) {
            setNonStrokingColor(color)
            beginText()
            setFont(font, fontSize)
            newLineAtOffset(x, y)
            showText(text.pdfSafe())
            endText()
            setNonStrokingColor(Color.BLACK)
        }

        private data class WeightedRiskFactor(
            val factor: RiskFactor,
            val weight: Int,
        )

        private fun RiskFactorType.weight(): Int =
            when (this) {
                RiskFactorType.GEOGRAPHY -> 15
                RiskFactorType.OWNERSHIP -> 30
                RiskFactorType.DOCUMENT_QUALITY -> 10
                RiskFactorType.SOURCE_OF_FUNDS -> 15
                RiskFactorType.SOURCE_OF_WEALTH -> 15
                RiskFactorType.SANCTIONS -> 15
                else -> 10
            }

        private fun RiskFactorType.chartColor(index: Int): Color {
            val palette = listOf(
                Color(46, 125, 50),
                Color(21, 101, 192),
                Color(239, 108, 0),
                Color(106, 27, 154),
                Color(0, 121, 107),
                Color(198, 40, 40),
                Color(97, 97, 97),
            )
            return palette[index % palette.size]
        }

        private fun wrappedLines(text: String, maxLength: Int): List<String> {
            val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
            val lines = mutableListOf<String>()
            var current = ""
            words.forEach { word ->
                val candidate = if (current.isBlank()) word else "$current $word"
                if (candidate.length > maxLength) {
                    lines += current
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
                .take((pageWidth / 5).toInt())
    }
}
