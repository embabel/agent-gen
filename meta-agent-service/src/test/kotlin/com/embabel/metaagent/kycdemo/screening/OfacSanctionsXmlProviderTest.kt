package com.embabel.metaagent.kycdemo.screening

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.time.LocalDate

class OfacSanctionsXmlProviderTest {

    @Test
    fun `parses OFAC sanctions XML into screening entries`() {
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
        assertEquals(ScreeningSource.OFAC_SANCTIONS, person.source)
        assertEquals("OFAC-1001", person.sourceRecordId)
        assertEquals("Ivan Petrov", person.primaryName)
        assertEquals(setOf("Ivan Nikolayevich Petrov"), person.aliases)
        assertEquals(LocalDate.parse("1970-03-20"), person.dateOfBirth)
        assertEquals(setOf("RU"), person.countries)
        assertEquals(setOf("SDN"), person.programs)
        assertTrue(
            person.identifiers.contains(
                ScreeningIdentifier(
                    type = ScreeningIdentifierType.PASSPORT,
                    value = "RU1234567",
                    issuingCountry = "RU",
                ),
            ),
        )
    }

    @Test
    fun `file-backed OFAC provider participates in AML screening service`() {
        val service = AmlScreeningService(
            providers = listOf(
                StaticScreeningProvider(
                    providerId = "eu-empty-fixture",
                    source = ScreeningSource.EU_FINANCIAL_SANCTIONS,
                    entries = emptyList(),
                ),
                provider(),
            ),
        )

        val assessment = service.screen(
            ScreeningSubject(
                id = "ivan-customer",
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

    private fun provider(): OfacSanctionsXmlProvider =
        OfacSanctionsXmlProvider(
            providerId = "ofac-file-fixture",
            xmlPath = Paths.get("src/test/resources/kycdemo/screening/ofac-sdn-sample.xml"),
        )
}
