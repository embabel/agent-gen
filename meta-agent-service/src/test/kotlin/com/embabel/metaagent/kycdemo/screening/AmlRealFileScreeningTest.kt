package com.embabel.metaagent.kycdemo.screening

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class AmlRealFileScreeningTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "AML_OFAC_SDN_XML", matches = ".+")
    fun `screens confirmed person match against real OFAC SDN file`() {
        val ofacProvider = OfacSanctionsXmlProvider(
            providerId = "ofac-real-file",
            xmlPath = requiredRealFile("AML_OFAC_SDN_XML"),
        )
        val entry = firstScreenablePersonEntry(ofacProvider)
        val service = AmlScreeningService(
            providers = listOf(emptyEuProvider(), ofacProvider),
        )

        val assessment = service.screen(
            ScreeningSubject(
                id = "real-ofac-${entry.sourceRecordId}",
                displayName = entry.primaryName,
                subjectType = ScreeningSubjectType.PERSON,
                dateOfBirth = entry.dateOfBirth,
                countries = entry.countries,
                identifiers = entry.identifiers,
            ),
        )

        assertEquals(ScreeningAssessmentStatus.REJECT, assessment.status)
        assertTrue(assessment.hits.isNotEmpty())

        val confirmedHit = assessment.hits.first()
        logger.info(
            "Real OFAC confirmed screening result: subject='{}', sourceRecordId={}, status={}, " +
                "disposition={}, confidence={}, nameScore={}, identifierScore={}, programs={}, rationale={}",
            assessment.subject.displayName,
            confirmedHit.sourceRecordId,
            assessment.status,
            confirmedHit.disposition,
            confirmedHit.confidenceScore,
            confirmedHit.nameScore,
            confirmedHit.identifierScore,
            entry.programs,
            confirmedHit.rationale,
        )
        assertEquals(ScreeningSource.OFAC_SANCTIONS, confirmedHit.source)
        assertEquals(entry.sourceRecordId, confirmedHit.sourceRecordId)
        assertEquals(ScreeningDisposition.CONFIRMED_MATCH, confirmedHit.disposition)
        assertEquals(entry.primaryName, confirmedHit.primaryName)
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AML_OFAC_SDN_XML", matches = ".+")
    fun `screens clean subject against real OFAC SDN file`() {
        val service = AmlScreeningService(
            providers = listOf(
                emptyEuProvider(),
                OfacSanctionsXmlProvider(
                    providerId = "ofac-real-file",
                    xmlPath = requiredRealFile("AML_OFAC_SDN_XML"),
                ),
            ),
        )

        val assessment = service.screen(
            ScreeningSubject(
                id = "real-ofac-clean-subject",
                displayName = "Definitely Not A Sanctions List Person",
                subjectType = ScreeningSubjectType.PERSON,
                countries = setOf("US"),
            ),
        )

        logger.info(
            "Real OFAC clean screening result: subject='{}', status={}, hits={}",
            assessment.subject.displayName,
            assessment.status,
            assessment.hits.size,
        )
        assertEquals(ScreeningAssessmentStatus.CLEAR, assessment.status)
        assertTrue(assessment.hits.isEmpty())
    }

    private fun firstScreenablePersonEntry(provider: OfacSanctionsXmlProvider): ScreeningListEntry {
        val entries = provider.search(
            ScreeningSubject(
                id = "real-ofac-person-search",
                displayName = "unused",
                subjectType = ScreeningSubjectType.PERSON,
            ),
        )
        val entry = entries.firstOrNull { it.dateOfBirth != null || it.identifiers.isNotEmpty() }
        assumeTrue(
            entry != null,
            "Real OFAC file must contain at least one person with date of birth or identifier evidence",
        )
        return entry!!
    }

    private fun emptyEuProvider(): NamedScreeningProvider =
        StaticScreeningProvider(
            providerId = "eu-not-loaded-for-ofac-real-file-test",
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

    companion object {
        private val logger = LoggerFactory.getLogger(AmlRealFileScreeningTest::class.java)
    }
}
