package com.embabel.metaagent.kycdemo.screening

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AmlScreeningServiceTest {

    @Test
    fun `screening service requires EU and OFAC sanctions providers`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            AmlScreeningService(listOf(euProvider()))
        }

        assertTrue(exception.message.orEmpty().contains("OFAC_SANCTIONS"))
    }

    @Test
    fun `clean subject is clear when no sanctions candidates match`() {
        val assessment = service().screen(
            ScreeningSubject(
                id = "michael-johnson",
                displayName = "Michael Johnson",
                subjectType = ScreeningSubjectType.PERSON,
                dateOfBirth = LocalDate.parse("1984-05-12"),
                countries = setOf("US"),
            ),
        )

        assertEquals(ScreeningAssessmentStatus.CLEAR, assessment.status)
        assertTrue(assessment.hits.isEmpty())
    }

    @Test
    fun `same name with conflicting identifiers is retained as likely false positive`() {
        val assessment = service().screen(
            ScreeningSubject(
                id = "john-smith-customer",
                displayName = "John Smith",
                subjectType = ScreeningSubjectType.PERSON,
                dateOfBirth = LocalDate.parse("1988-02-01"),
                countries = setOf("GB"),
            ),
        )

        assertEquals(ScreeningAssessmentStatus.CLEAR, assessment.status)
        assertEquals(ScreeningDisposition.LIKELY_FALSE_POSITIVE, assessment.hits.single().disposition)
    }

    @Test
    fun `same name with partial identifiers is possible match for manual review`() {
        val assessment = service().screen(
            ScreeningSubject(
                id = "anna-petrova-customer",
                displayName = "Anna Viktorovna Petrova",
                subjectType = ScreeningSubjectType.PERSON,
                countries = setOf("EE"),
            ),
        )

        assertEquals(ScreeningAssessmentStatus.MANUAL_REVIEW_REQUIRED, assessment.status)
        assertEquals(ScreeningDisposition.POSSIBLE_MATCH, assessment.hits.single().disposition)
        assertEquals(ScreeningSource.EU_FINANCIAL_SANCTIONS, assessment.hits.single().source)
    }

    @Test
    fun `same name and strong identifiers is confirmed match and reject`() {
        val assessment = service().screen(
            ScreeningSubject(
                id = "ivan-petrov-customer",
                displayName = "Ivan Petrov",
                subjectType = ScreeningSubjectType.PERSON,
                dateOfBirth = LocalDate.parse("1970-03-20"),
                countries = setOf("RU"),
                identifiers = setOf(
                    ScreeningIdentifier(
                        type = ScreeningIdentifierType.PASSPORT,
                        value = "RU1234567",
                        issuingCountry = "RU",
                    ),
                ),
            ),
        )

        assertEquals(ScreeningAssessmentStatus.REJECT, assessment.status)
        assertEquals(ScreeningDisposition.CONFIRMED_MATCH, assessment.hits.single().disposition)
        assertEquals(ScreeningSource.OFAC_SANCTIONS, assessment.hits.single().source)
    }

    private fun service(): AmlScreeningService =
        AmlScreeningService(
            providers = listOf(
                euProvider(),
                ofacProvider(),
            ),
        )

    private fun euProvider(): NamedScreeningProvider =
        StaticScreeningProvider(
            providerId = "eu-sanctions-fixture",
            source = ScreeningSource.EU_FINANCIAL_SANCTIONS,
            entries = listOf(
                ScreeningListEntry(
                    source = ScreeningSource.EU_FINANCIAL_SANCTIONS,
                    sourceRecordId = "EU-PEP-001",
                    primaryName = "Anna Petrova",
                    aliases = setOf("Anna Viktorovna Petrova"),
                    entryType = ScreeningSubjectType.PERSON,
                    countries = setOf("EE"),
                    programs = setOf("PEP-related sanctions fixture"),
                    sourceUrl = "https://webgate.ec.europa.eu/fsd/fsf",
                ),
            ),
        )

    private fun ofacProvider(): NamedScreeningProvider =
        StaticScreeningProvider(
            providerId = "ofac-sanctions-fixture",
            source = ScreeningSource.OFAC_SANCTIONS,
            entries = listOf(
                ScreeningListEntry(
                    source = ScreeningSource.OFAC_SANCTIONS,
                    sourceRecordId = "OFAC-FP-001",
                    primaryName = "John Smith",
                    entryType = ScreeningSubjectType.PERSON,
                    dateOfBirth = LocalDate.parse("1955-09-11"),
                    countries = setOf("RU"),
                    sourceUrl = "https://ofac.treasury.gov/sanctions-list-service",
                ),
                ScreeningListEntry(
                    source = ScreeningSource.OFAC_SANCTIONS,
                    sourceRecordId = "OFAC-SDN-001",
                    primaryName = "Ivan Petrov",
                    aliases = setOf("Ivan Nikolayevich Petrov"),
                    entryType = ScreeningSubjectType.PERSON,
                    dateOfBirth = LocalDate.parse("1970-03-20"),
                    countries = setOf("RU"),
                    identifiers = setOf(
                        ScreeningIdentifier(
                            type = ScreeningIdentifierType.PASSPORT,
                            value = "RU1234567",
                            issuingCountry = "RU",
                        ),
                    ),
                    programs = setOf("SDN"),
                    sourceUrl = "https://ofac.treasury.gov/sanctions-list-service",
                ),
            ),
        )
}
