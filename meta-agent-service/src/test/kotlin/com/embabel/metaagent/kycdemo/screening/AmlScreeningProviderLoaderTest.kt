package com.embabel.metaagent.kycdemo.screening

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.time.LocalDate

class AmlScreeningProviderLoaderTest {

    @Test
    fun `loads EU and OFAC file providers from configured source files`() {
        val loader = AmlScreeningProviderLoader(sampleSources())

        val providers = loader.loadProviders()

        assertEquals(
            setOf(ScreeningSource.EU_FINANCIAL_SANCTIONS, ScreeningSource.OFAC_SANCTIONS),
            providers.map { it.source }.toSet(),
        )
        assertTrue(providers.all { it.providerId.isNotBlank() })

        val euProvider = providers.single { it.source == ScreeningSource.EU_FINANCIAL_SANCTIONS }
        val ofacProvider = providers.single { it.source == ScreeningSource.OFAC_SANCTIONS }

        assertEquals(1, euProvider.search(sampleSubject(ScreeningSubjectType.PERSON)).size)
        assertEquals(1, euProvider.search(sampleSubject(ScreeningSubjectType.LEGAL_ENTITY)).size)
        assertEquals(1, ofacProvider.search(sampleSubject(ScreeningSubjectType.PERSON)).size)
        assertEquals(1, ofacProvider.search(sampleSubject(ScreeningSubjectType.LEGAL_ENTITY)).size)
    }

    @Test
    fun `loaded file providers back AML screening service`() {
        val service = AmlScreeningProviderLoader(sampleSources()).loadService()

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
        assertEquals(ScreeningSource.OFAC_SANCTIONS, assessment.hits.single().source)
    }

    @Test
    fun `configured source files must exist`() {
        assertThrows(IllegalArgumentException::class.java) {
            AmlSanctionsFileSources(
                euFinancialSanctionsXml = Paths.get("missing-eu.xml"),
                ofacSdnXml = sampleOfacPath(),
            )
        }
    }

    private fun sampleSources(): AmlSanctionsFileSources =
        AmlSanctionsFileSources(
            euFinancialSanctionsXml = sampleEuPath(),
            ofacSdnXml = sampleOfacPath(),
        )

    private fun sampleEuPath() =
        Paths.get("src/test/resources/kycdemo/screening/eu-financial-sanctions-sample.xml")

    private fun sampleOfacPath() =
        Paths.get("src/test/resources/kycdemo/screening/ofac-sdn-sample.xml")

    private fun sampleSubject(subjectType: ScreeningSubjectType): ScreeningSubject =
        ScreeningSubject(
            id = "sample-$subjectType",
            displayName = "Sample Subject",
            subjectType = subjectType,
        )
}
