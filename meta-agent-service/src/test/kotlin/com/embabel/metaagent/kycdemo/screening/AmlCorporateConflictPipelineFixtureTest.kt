package com.embabel.metaagent.kycdemo.screening

import com.embabel.metaagent.kycdemo.AmlConflictCertificatePdfFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.slf4j.LoggerFactory

class AmlCorporateConflictPipelineFixtureTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "AML_OFAC_SDN_XML", matches = ".+")
    fun `fabricated certificate company produces multiple possible OFAC corporate matches`() {
        val (certificatePdf, assessment) = AmlCorporateConflictSupport.createAndScreenCorporateConflict()

        assertEquals(ScreeningAssessmentStatus.MANUAL_REVIEW_REQUIRED, assessment.status)
        val ofacHits = assessment.hits.filter { it.source == ScreeningSource.OFAC_SANCTIONS }
        assertEquals(3, ofacHits.size)
        assertEquals(setOf("23648", "23652", "23653"), ofacHits.map { it.sourceRecordId }.toSet())
        assertTrue(ofacHits.any { it.disposition == ScreeningDisposition.POSSIBLE_MATCH })
        assertTrue(ofacHits.all { it.confidenceScore in 0.0..1.0 })

        logger.info(
            "Corporate AML conflict fixture: pdf={}, subject='{}', status={}, candidates={}",
            certificatePdf,
            AmlConflictCertificatePdfFixture.COMPANY_NAME,
            assessment.status,
            ofacHits.joinToString { hit ->
                "recordId=${hit.sourceRecordId}, primaryName='${hit.primaryName}', matchedName='${hit.matchedName}', " +
                    "disposition=${hit.disposition}, confidence=${hit.confidenceScore}, rationale=${hit.rationale}"
            },
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AmlCorporateConflictPipelineFixtureTest::class.java)
    }
}
