package com.embabel.metaagent.kycdemo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BaselineKycRiskMethodologyRuleTest {

    @Test
    fun `certificate-only package receives deterministic pre-screening risk assessment`() {
        val kycCase = KycCase(
            caseId = "risk-methodology-certificate-only",
            subject = LegalEntity(
                id = "example-payments-ltd",
                displayName = "Example Payments Ltd",
                registrationNumber = "EX-123",
                jurisdictionOfIncorporation = "IE",
                incorporationDate = LocalDate.parse("2020-01-15"),
                roles = setOf(PartyRole.CUSTOMER),
            ),
            evidence = listOf(
                DocumentEvidence(
                    documentId = "certificate-of-incorporation.txt",
                    type = DocumentType.CERTIFICATE_OF_INCORPORATION,
                    fileName = "certificate-of-incorporation.txt",
                    issuer = "Companies Registry",
                    issueDate = LocalDate.parse("2020-01-15"),
                ),
            ),
        )

        val riskAssessment = BaselineKycRiskMethodologyRule.assess(kycCase)

        assertEquals(RiskLevel.HIGH, riskAssessment.overallRisk)
        assertEquals(56, riskAssessment.score)
        assertTrue(riskAssessment.methodology.contains("Categorical overall risk remains conservative"))
        assertTrue(riskAssessment.methodology.contains("Numeric score is the weighted average"))
        assertRiskFactor(riskAssessment, RiskFactorType.GEOGRAPHY, RiskLevel.LOW)
        assertRiskFactor(riskAssessment, RiskFactorType.OWNERSHIP, RiskLevel.HIGH)
        assertRiskFactor(riskAssessment, RiskFactorType.DOCUMENT_QUALITY, RiskLevel.LOW)
        assertRiskFactor(riskAssessment, RiskFactorType.SOURCE_OF_FUNDS, RiskLevel.MEDIUM)
        assertRiskFactor(riskAssessment, RiskFactorType.SOURCE_OF_WEALTH, RiskLevel.MEDIUM)
        assertRiskFactor(riskAssessment, RiskFactorType.SANCTIONS, RiskLevel.UNKNOWN)

        assertNotNull(riskAssessment.factor(RiskFactorType.GEOGRAPHY).evidence)
        assertNotNull(riskAssessment.factor(RiskFactorType.DOCUMENT_QUALITY).evidence)
        assertNull(riskAssessment.factor(RiskFactorType.OWNERSHIP).evidence)
        assertNull(riskAssessment.factor(RiskFactorType.SOURCE_OF_FUNDS).evidence)
    }

    private fun assertRiskFactor(
        riskAssessment: RiskAssessment,
        type: RiskFactorType,
        level: RiskLevel,
    ) {
        assertTrue(
            riskAssessment.factors.any { it.type == type && it.level == level },
            "Expected $type risk factor with level $level in ${riskAssessment.factors}",
        )
    }

    private fun RiskAssessment.factor(type: RiskFactorType): RiskFactor =
        factors.single { it.type == type }
}
