package com.embabel.metaagent.kycdemo.screening

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class AmlRealFileIngestionTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "AML_EU_FSF_XML", matches = ".+")
    fun `ingests configured real EU financial sanctions XML file`() {
        val provider = EuFinancialSanctionsXmlProvider(
            providerId = "eu-real-file",
            xmlPath = requiredRealFile("AML_EU_FSF_XML"),
        )

        val people = provider.search(realFileSubject(ScreeningSubjectType.PERSON))
        val legalEntities = provider.search(realFileSubject(ScreeningSubjectType.LEGAL_ENTITY))

        assertTrue(people.size + legalEntities.size > 0)
        assertTrue((people + legalEntities).all { it.source == ScreeningSource.EU_FINANCIAL_SANCTIONS })
        assertTrue((people + legalEntities).all { it.primaryName.isNotBlank() })
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AML_OFAC_SDN_XML", matches = ".+")
    fun `ingests configured real OFAC SDN XML file`() {
        val provider = OfacSanctionsXmlProvider(
            providerId = "ofac-real-file",
            xmlPath = requiredRealFile("AML_OFAC_SDN_XML"),
        )

        val people = provider.search(realFileSubject(ScreeningSubjectType.PERSON))
        val legalEntities = provider.search(realFileSubject(ScreeningSubjectType.LEGAL_ENTITY))

        assertTrue(people.size + legalEntities.size > 0)
        assertTrue((people + legalEntities).all { it.source == ScreeningSource.OFAC_SANCTIONS })
        assertTrue((people + legalEntities).all { it.primaryName.isNotBlank() })
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AML_EU_FSF_XML", matches = ".+")
    @EnabledIfEnvironmentVariable(named = "AML_OFAC_SDN_XML", matches = ".+")
    fun `configured real EU and OFAC files can back the AML screening service`() {
        val service = AmlScreeningProviderLoader(
            AmlSanctionsFileSources(
                euFinancialSanctionsXml = requiredRealFile("AML_EU_FSF_XML"),
                ofacSdnXml = requiredRealFile("AML_OFAC_SDN_XML"),
            ),
        ).loadService()

        val assessment = service.screen(
            ScreeningSubject(
                id = "real-file-smoke-clean-subject",
                displayName = "Definitely Not A Sanctions Fixture Name",
                subjectType = ScreeningSubjectType.PERSON,
                countries = setOf("US"),
            ),
        )

        assertEquals(ScreeningAssessmentStatus.CLEAR, assessment.status)
    }

    private fun requiredRealFile(environmentVariable: String): Path {
        val configured = requireNotNull(System.getenv(environmentVariable)) {
            "$environmentVariable must point to a local sanctions XML file"
        }
        val path = Paths.get(configured)
        require(Files.isRegularFile(path)) { "$environmentVariable must point to an existing regular file: $path" }
        return path
    }

    private fun realFileSubject(subjectType: ScreeningSubjectType): ScreeningSubject =
        ScreeningSubject(
            id = "real-file-$subjectType",
            displayName = "Real File Smoke Subject",
            subjectType = subjectType,
        )
}
