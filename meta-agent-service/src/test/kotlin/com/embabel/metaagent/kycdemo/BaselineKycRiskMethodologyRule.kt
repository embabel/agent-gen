package com.embabel.metaagent.kycdemo

object BaselineKycRiskMethodologyRule : KycRiskRule {

    override fun evaluate(kycCase: KycCase): List<RiskFactor> =
        listOfNotNull(
            geographyRisk(kycCase),
            ownershipRisk(kycCase),
            documentQualityRisk(kycCase),
            sourceOfFundsRisk(kycCase),
            sourceOfWealthRisk(kycCase),
            preScreeningRisk(kycCase),
        )

    fun assess(kycCase: KycCase): RiskAssessment {
        val factors = evaluate(kycCase)
        val score = aggregateScore(factors)
        val overallRisk = when {
            factors.any { it.level == RiskLevel.HIGH } -> RiskLevel.HIGH
            factors.any { it.level == RiskLevel.MEDIUM } -> RiskLevel.MEDIUM
            factors.any { it.level == RiskLevel.UNKNOWN } -> RiskLevel.UNKNOWN
            else -> RiskLevel.LOW
        }
        return RiskAssessment(
            overallRisk = overallRisk,
            score = score,
            factors = factors,
            rationale = "Baseline pre-screening KYC risk is composed from geography, ownership/control, document quality, source-of-funds, source-of-wealth, and AML-screening readiness factors.",
            methodology = KycRiskScoringMethodology.description +
                " Baseline assessment uses a pre-screening AML/SANCTIONS factor of UNKNOWN until screening is performed.",
        )
    }

    private fun geographyRisk(kycCase: KycCase): RiskFactor? {
        val subject = kycCase.subject
        val jurisdiction = (subject as? LegalEntity)?.jurisdictionOfIncorporation ?: return null
        val countryRisk = CountryRiskMethodology.levelFor(jurisdiction)
        return RiskFactor(
            type = RiskFactorType.GEOGRAPHY,
            level = countryRisk,
            rationale = "Jurisdiction of incorporation is $jurisdiction; demo country-risk methodology classifies it as $countryRisk based on the configured country risk table.",
            evidence = certificateEvidenceReference(kycCase, "Certificate identifies jurisdiction of incorporation as $jurisdiction."),
        )
    }

    private fun ownershipRisk(kycCase: KycCase): RiskFactor =
        if (kycCase.ownership.isEmpty()) {
            RiskFactor(
                type = RiskFactorType.OWNERSHIP,
                level = RiskLevel.HIGH,
                rationale = "No beneficial ownership or control evidence is present in the supplied KYC package.",
            )
        } else {
            RiskFactor(
                type = RiskFactorType.OWNERSHIP,
                level = RiskLevel.LOW,
                rationale = "Beneficial ownership or control evidence is present.",
            )
        }

    private fun documentQualityRisk(kycCase: KycCase): RiskFactor {
        val hasIncorporationEvidence = kycCase.evidence.any { it.type == DocumentType.CERTIFICATE_OF_INCORPORATION }
        return RiskFactor(
            type = RiskFactorType.DOCUMENT_QUALITY,
            level = if (hasIncorporationEvidence) RiskLevel.LOW else RiskLevel.MEDIUM,
            rationale = if (hasIncorporationEvidence) {
                "Certificate of incorporation evidence is present and contains extractable structured facts."
            } else {
                "No certificate of incorporation evidence is present."
            },
            evidence = certificateEvidenceReference(
                kycCase,
                "Certificate of incorporation evidence is present and contains extractable structured facts.",
            ),
        )
    }

    private fun sourceOfFundsRisk(kycCase: KycCase): RiskFactor =
        missingEvidenceRisk(
            hasEvidence = kycCase.evidence.any { it.type == DocumentType.SOURCE_OF_FUNDS },
            type = RiskFactorType.SOURCE_OF_FUNDS,
            presentRationale = "Source-of-funds evidence is present.",
            missingRationale = "Source-of-funds evidence is not present in the supplied KYC package.",
        )

    private fun sourceOfWealthRisk(kycCase: KycCase): RiskFactor =
        missingEvidenceRisk(
            hasEvidence = kycCase.evidence.any { it.type == DocumentType.SOURCE_OF_WEALTH },
            type = RiskFactorType.SOURCE_OF_WEALTH,
            presentRationale = "Source-of-wealth evidence is present.",
            missingRationale = "Source-of-wealth evidence is not present in the supplied KYC package.",
        )

    private fun preScreeningRisk(kycCase: KycCase): RiskFactor {
        val screeningTypes = kycCase.screeningResults.map { it.type }.toSet()
        val allScreeningPresent = ScreeningType.entries.all { it in screeningTypes }
        return RiskFactor(
            type = RiskFactorType.SANCTIONS,
            level = if (allScreeningPresent) RiskLevel.LOW else RiskLevel.UNKNOWN,
            rationale = if (allScreeningPresent) {
                "AML screening results are present for sanctions, PEP, adverse media, and internal watchlist checks."
            } else {
                "AML screening is not part of this certificate-ingestion test; sanctions, PEP, adverse-media, and watchlist risk remain unassessed."
            },
        )
    }

    private fun missingEvidenceRisk(
        hasEvidence: Boolean,
        type: RiskFactorType,
        presentRationale: String,
        missingRationale: String,
    ): RiskFactor =
        RiskFactor(
            type = type,
            level = if (hasEvidence) RiskLevel.LOW else RiskLevel.MEDIUM,
            rationale = if (hasEvidence) presentRationale else missingRationale,
        )

    private fun certificateEvidenceReference(kycCase: KycCase, excerpt: String): EvidenceReference? {
        val evidence = kycCase.evidence.firstOrNull { it.type == DocumentType.CERTIFICATE_OF_INCORPORATION }
            ?: return null
        return EvidenceReference(
            documentId = evidence.documentId,
            page = 1,
            excerpt = excerpt,
            confidence = 0.9,
        )
    }

    private fun aggregateScore(factors: List<RiskFactor>): Int {
        val weighted = factors.map { factor ->
            val weight = factor.type.weight()
            KycRiskScoringMethodology.levelScore(factor.level) * weight to weight
        }
        val totalWeight = weighted.sumOf { it.second }
        if (totalWeight == 0) {
            return KycRiskScoringMethodology.levelScore(RiskLevel.UNKNOWN)
        }
        return (weighted.sumOf { it.first }.toDouble() / totalWeight).toInt()
    }

    private fun RiskFactorType.weight(): Int =
        KycRiskScoringMethodology.factorWeight(this)
}

object CountryRiskMethodology {

    private val lowRiskJurisdictions = setOf(
        "IE", "GB", "FR", "DE", "NL", "ES", "IT", "SE", "NO", "DK", "FI", "BE", "LU", "AT", "PT",
    )

    private val mediumRiskJurisdictions = setOf(
        "HK",
    )

    fun levelFor(countryCode: String): RiskLevel =
        when (countryCode.uppercase()) {
            in lowRiskJurisdictions -> RiskLevel.LOW
            in mediumRiskJurisdictions -> RiskLevel.MEDIUM
            else -> RiskLevel.UNKNOWN
        }
}

object KycRiskScoringMethodology {

    const val description =
        "Aggregate risk formula: assign each factor a categorical level LOW, MEDIUM, HIGH, or UNKNOWN and a weighted numeric contribution. Numeric score is the weighted average rounded to 0-100. Categorical overall risk remains conservative: HIGH if any factor is HIGH; otherwise MEDIUM if any factor is MEDIUM; otherwise UNKNOWN if any factor is UNKNOWN; otherwise LOW."

    val levelScoreDescription: String =
        RiskLevel.entries.joinToString { "${it.name}=${levelScore(it)}" }

    val factorWeightDescription: String =
        listOf(
            RiskFactorType.GEOGRAPHY,
            RiskFactorType.OWNERSHIP,
            RiskFactorType.DOCUMENT_QUALITY,
            RiskFactorType.SOURCE_OF_FUNDS,
            RiskFactorType.SOURCE_OF_WEALTH,
            RiskFactorType.SANCTIONS,
        ).joinToString { type ->
            val label = if (type == RiskFactorType.SANCTIONS) "AML/SANCTIONS" else type.name
            "$label=${factorWeight(type)}"
        }

    fun levelScore(level: RiskLevel): Int =
        when (level) {
            RiskLevel.LOW -> 20
            RiskLevel.MEDIUM -> 55
            RiskLevel.HIGH -> 90
            RiskLevel.UNKNOWN -> 50
        }

    fun factorWeight(type: RiskFactorType): Int =
        when (type) {
            RiskFactorType.GEOGRAPHY -> 15
            RiskFactorType.OWNERSHIP -> 30
            RiskFactorType.DOCUMENT_QUALITY -> 10
            RiskFactorType.SOURCE_OF_FUNDS -> 15
            RiskFactorType.SOURCE_OF_WEALTH -> 15
            RiskFactorType.SANCTIONS -> 15
            else -> 10
        }
}
