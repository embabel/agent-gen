package com.embabel.metaagent.kycdemo.screening

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.time.LocalDate

class EuFinancialSanctionsXmlProviderTest {

    @Test
    fun `parses EU financial sanctions XML into screening entries`() {
        val provider = provider()

        val people = provider.search(
            ScreeningSubject(
                id = "any-person",
                displayName = "Any Person",
                subjectType = ScreeningSubjectType.PERSON,
            ),
        )
        val legalEntities = provider.search(
            ScreeningSubject(
                id = "any-entity",
                displayName = "Any Entity",
                subjectType = ScreeningSubjectType.LEGAL_ENTITY,
            ),
        )

        assertEquals(1, people.size)
        assertEquals(1, legalEntities.size)

        val person = people.single()
        assertEquals(ScreeningSource.EU_FINANCIAL_SANCTIONS, person.source)
        assertEquals("EU-1001", person.sourceRecordId)
        assertEquals("Anna Viktorovna Petrova", person.primaryName)
        assertEquals(setOf("Anna Petrova"), person.aliases)
        assertEquals(LocalDate.parse("1978-04-09"), person.dateOfBirth)
        assertEquals(setOf("EE"), person.countries)
        assertEquals(setOf("EU_TEST_PROGRAM"), person.programs)
        assertTrue(
            person.identifiers.contains(
                ScreeningIdentifier(
                    type = ScreeningIdentifierType.PASSPORT,
                    value = "EE1234567",
                    issuingCountry = "EE",
                ),
            ),
        )
    }

    @Test
    fun `file-backed EU provider participates in AML screening service`() {
        val service = AmlScreeningService(
            providers = listOf(
                StaticScreeningProvider(
                    providerId = "ofac-empty-fixture",
                    source = ScreeningSource.OFAC_SANCTIONS,
                    entries = emptyList(),
                ),
                provider(),
            ),
        )

        val assessment = service.screen(
            ScreeningSubject(
                id = "anna-customer",
                displayName = "Anna Viktorovna Petrova",
                subjectType = ScreeningSubjectType.PERSON,
                dateOfBirth = LocalDate.parse("1978-04-09"),
                countries = setOf("EE"),
                identifiers = setOf(
                    ScreeningIdentifier(
                        type = ScreeningIdentifierType.PASSPORT,
                        value = "EE1234567",
                        issuingCountry = "EE",
                    ),
                ),
            ),
        )

        assertEquals(ScreeningAssessmentStatus.REJECT, assessment.status)
        assertEquals(ScreeningDisposition.CONFIRMED_MATCH, assessment.hits.single().disposition)
        assertEquals(ScreeningSource.EU_FINANCIAL_SANCTIONS, assessment.hits.single().source)
    }

    private fun provider(): EuFinancialSanctionsXmlProvider =
        EuFinancialSanctionsXmlProvider(
            providerId = "eu-file-fixture",
            xmlPath = Paths.get(
                "src/test/resources/kycdemo/screening/eu-financial-sanctions-sample.xml",
            ),
        )
}
