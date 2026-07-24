package com.embabel.metaagent.kycdemo

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import java.nio.file.Files
import java.nio.file.Path

object AmlConflictCertificatePdfFixture {

    const val COMPANY_NAME = "KINGS ROMANS GROUP LIMITED"
    const val REGISTRATION_NUMBER = "KRG-2026-041"
    const val JURISDICTION = "HK"
    const val INCORPORATION_DATE = "2026-07-21"
    const val REGISTERED_OFFICE = "88 Queensway, Admiralty, Hong Kong"
    const val ISSUE_PLACE = "Hong Kong"
    const val ISSUER = "Companies Registry"
    const val INPUT_FILE_NAME = "kings-romans-group-limited-certificate.pdf"

    fun create(output: Path): Path {
        Files.createDirectories(output.parent)
        Files.deleteIfExists(output)

        PDDocument().use { document ->
            val page = PDPage(PDRectangle.LETTER)
            document.addPage(page)

            PDPageContentStream(document, page).use { content ->
                val writer = PdfTextWriter(content)
                writer.line("Certificate of Incorporation", 18f, true)
                writer.blank()
                writer.line("The Registrar of Companies certifies that", 12f)
                writer.line(COMPANY_NAME, 16f, true)
                writer.line("is incorporated under the Companies Act 2006 as a private limited company.", 12f)
                writer.blank()
                writer.line("Company number: $REGISTRATION_NUMBER", 12f)
                writer.line("Jurisdiction of incorporation: $JURISDICTION", 12f)
                writer.line("Date of incorporation: $INCORPORATION_DATE", 12f)
                writer.line("Registered office: $REGISTERED_OFFICE", 12f)
                writer.blank()
                writer.line("The company provides hospitality investment and cross-border settlement services.", 12f)
                writer.blank()
                writer.line("Issued at $ISSUE_PLACE by the $ISSUER.", 12f)
            }

            document.save(output.toFile())
        }

        return output
    }

    private class PdfTextWriter(
        private val content: PDPageContentStream,
    ) {
        private val regularFont = PDType1Font(Standard14Fonts.FontName.HELVETICA)
        private val boldFont = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
        private var y = PDRectangle.LETTER.height - 72f

        fun line(text: String, fontSize: Float, bold: Boolean = false) {
            content.beginText()
            content.setFont(if (bold) boldFont else regularFont, fontSize)
            content.newLineAtOffset(72f, y)
            content.showText(text)
            content.endText()
            y -= fontSize + 8f
        }

        fun blank() {
            y -= 10f
        }
    }
}
