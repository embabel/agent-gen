package com.embabel.metaagent.kycdemo.screening

class ScreeningMatcher(
    private val policy: ScreeningMatchingPolicy = ScreeningMatchingPolicy.demoDefaults(),
) {

    fun match(
        subject: ScreeningSubject,
        providerId: String,
        entry: ScreeningListEntry,
    ): ScreeningHit? {
        if (subject.subjectType != entry.entryType) {
            return null
        }

        val bestName = entry.names()
            .map { candidateName -> candidateName to nameSimilarity(subject.displayName, candidateName, policy) }
            .maxByOrNull { it.second }
            ?: return null

        if (bestName.second < policy.possibleMatchNameThreshold) {
            return null
        }

        val identifierScore = identifierScore(subject, entry)
        val confidenceScore = (
            (bestName.second * policy.nameConfidenceWeight) +
                (identifierScore * policy.identifierConfidenceWeight)
            ).coerceIn(0.0, 1.0)
        val disposition = disposition(bestName.second, identifierScore, subject, entry)

        return ScreeningHit(
            providerId = providerId,
            source = entry.source,
            sourceRecordId = entry.sourceRecordId,
            matchedName = bestName.first,
            primaryName = entry.primaryName,
            nameScore = bestName.second,
            identifierScore = identifierScore,
            confidenceScore = confidenceScore,
            countries = entry.countries,
            addresses = entry.addresses,
            disposition = disposition,
            rationale = rationale(subject, entry, bestName.first, bestName.second, identifierScore, disposition),
            sourceUrl = entry.sourceUrl,
        )
    }

    private fun disposition(
        nameScore: Double,
        identifierScore: Double,
        subject: ScreeningSubject,
        entry: ScreeningListEntry,
    ): ScreeningDisposition =
        when {
            hasHardIdentifierConflict(subject, entry) -> ScreeningDisposition.LIKELY_FALSE_POSITIVE
            nameScore >= policy.confirmedNameThreshold &&
                identifierScore >= policy.confirmedIdentifierThreshold &&
                hasStrongIdentifierMatch(subject, entry) ->
                ScreeningDisposition.CONFIRMED_MATCH
            else -> ScreeningDisposition.POSSIBLE_MATCH
        }

    private fun identifierScore(subject: ScreeningSubject, entry: ScreeningListEntry): Double {
        var matched = 0
        var compared = 0

        if (subject.dateOfBirth != null && entry.dateOfBirth != null) {
            compared += 1
            if (sameDate(subject.dateOfBirth, entry.dateOfBirth)) {
                matched += 1
            }
        }

        if (subject.countries.isNotEmpty() && entry.countries.isNotEmpty()) {
            compared += 1
            if (subject.countries.normalizedCodes().intersect(entry.countries.normalizedCodes()).isNotEmpty()) {
                matched += 1
            }
        }

        val subjectIdentifiers = subject.identifiers.normalizedIdentifiers()
        val entryIdentifiers = entry.identifiers.normalizedIdentifiers()
        if (subjectIdentifiers.isNotEmpty() && entryIdentifiers.isNotEmpty()) {
            compared += 1
            if (subjectIdentifiers.intersect(entryIdentifiers).isNotEmpty()) {
                matched += 1
            }
        }

        if (compared == 0) {
            return policy.noComparableIdentifiersScore
        }
        return matched.toDouble() / compared.toDouble()
    }

    private fun hasHardIdentifierConflict(subject: ScreeningSubject, entry: ScreeningListEntry): Boolean {
        val dateOfBirthConflict = subject.dateOfBirth != null &&
            entry.dateOfBirth != null &&
            !sameDate(subject.dateOfBirth, entry.dateOfBirth)
        val countryConflict = subject.countries.isNotEmpty() &&
            entry.countries.isNotEmpty() &&
            subject.countries.normalizedCodes().intersect(entry.countries.normalizedCodes()).isEmpty()
        val identifierConflict = subject.identifiers.isNotEmpty() &&
            entry.identifiers.isNotEmpty() &&
            subject.identifiers.normalizedIdentifiers().intersect(entry.identifiers.normalizedIdentifiers()).isEmpty()

        return listOf(dateOfBirthConflict, countryConflict, identifierConflict).count { it } >=
            policy.falsePositiveConflictCount
    }

    private fun hasStrongIdentifierMatch(subject: ScreeningSubject, entry: ScreeningListEntry): Boolean {
        val dateOfBirthMatch = sameDate(subject.dateOfBirth, entry.dateOfBirth)
        val identifierMatch = subject.identifiers.isNotEmpty() &&
            entry.identifiers.isNotEmpty() &&
            subject.identifiers.normalizedIdentifiers().intersect(entry.identifiers.normalizedIdentifiers()).isNotEmpty()

        return dateOfBirthMatch || identifierMatch
    }

    private fun rationale(
        subject: ScreeningSubject,
        entry: ScreeningListEntry,
        matchedName: String,
        nameScore: Double,
        identifierScore: Double,
        disposition: ScreeningDisposition,
    ): String =
        "Subject '${subject.displayName}' matched '${entry.primaryName}' using candidate name '$matchedName'. " +
            "Name score=${nameScore.formatScore()}, identifier score=${identifierScore.formatScore()}, " +
            "source=${entry.source}, disposition=$disposition."

    private fun ScreeningListEntry.names(): Set<String> =
        aliases + primaryName

    private fun sameDate(left: java.time.LocalDate?, right: java.time.LocalDate?): Boolean =
        left != null && right != null && left.isEqual(right)

    private fun Set<String>.normalizedCodes(): Set<String> =
        map { it.normalizedCountryCode() }.filter { it.isNotBlank() }.toSet()

    private fun Set<ScreeningIdentifier>.normalizedIdentifiers(): Set<String> =
        map { "${it.type}:${it.issuingCountry.orEmpty().uppercase()}:${it.value.normalizedIdentifierValue()}" }.toSet()

    private fun String.normalizedIdentifierValue(): String =
        uppercase().filter { it.isLetterOrDigit() }

    private fun Double.formatScore(): String =
        "%.2f".format(this)
}
