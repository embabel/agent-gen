package com.embabel.metaagent.kycdemo.screening

import java.nio.file.Path
import java.time.LocalDate
import org.w3c.dom.Element

class EuFinancialSanctionsXmlProvider(
    override val providerId: String,
    private val xmlPath: Path,
    private val sourceUrl: String = "https://webgate.ec.europa.eu/fsd/fsf",
) : NamedScreeningProvider {

    override val source: ScreeningSource = ScreeningSource.EU_FINANCIAL_SANCTIONS

    private val entries: List<ScreeningListEntry> by lazy {
        parseEntries(xmlPath)
    }

    override fun search(subject: ScreeningSubject): List<ScreeningListEntry> =
        entries.filter { it.entryType == subject.subjectType }

    private fun parseEntries(path: Path): List<ScreeningListEntry> {
        val document = path.parseXmlDocument()

        return document.getElementsByTagName("sanctionEntity")
            .asElements()
            .mapNotNull(::parseEntry)
    }

    private fun parseEntry(entity: Element): ScreeningListEntry? {
        val names = entity.childElements("nameAlias")
            .mapNotNull { it.attributeOrNull("wholeName") ?: it.attributeOrNull("name") }
            .filter { it.isNotBlank() }
            .distinct()
        val primaryName = names.firstOrNull() ?: return null

        return ScreeningListEntry(
            source = ScreeningSource.EU_FINANCIAL_SANCTIONS,
            sourceRecordId = entity.attributeOrNull("logicalId")
                ?: entity.attributeOrNull("euReferenceNumber")
                ?: primaryName,
            primaryName = primaryName,
            aliases = names.drop(1).toSet(),
            entryType = entity.screeningSubjectType(),
            dateOfBirth = entity.birthDate(),
            countries = entity.countryCodes(),
            identifiers = entity.identifiers(),
            programs = entity.programs(),
            sourceUrl = sourceUrl,
        )
    }

    private fun Element.screeningSubjectType(): ScreeningSubjectType {
        val subjectTypeCode = childElements("subjectType")
            .firstOrNull()
            ?.attributeOrNull("code")
            ?.lowercase()
            .orEmpty()

        return when {
            subjectTypeCode.contains("enterprise") ||
                subjectTypeCode.contains("entity") ||
                subjectTypeCode.contains("legal") ->
                ScreeningSubjectType.LEGAL_ENTITY
            else -> ScreeningSubjectType.PERSON
        }
    }

    private fun Element.birthDate(): LocalDate? =
        childElements("birthdate")
            .firstNotNullOfOrNull { birthdate ->
                birthdate.attributeOrNull("birthdate")?.toScreeningDateOrNull()
            }

    private fun Element.countryCodes(): Set<String> =
        (childElements("citizenship") + childElements("address") + childElements("identification"))
            .mapNotNull { it.attributeOrNull("countryIso2Code") }
            .map { it.uppercase() }
            .filter { it.isNotBlank() }
            .toSet()

    private fun Element.identifiers(): Set<ScreeningIdentifier> =
        childElements("identification")
            .mapNotNull { identification ->
                val value = identification.attributeOrNull("documentNumber") ?: return@mapNotNull null
                ScreeningIdentifier(
                    type = identification.attributeOrNull("documentTypeCode").toScreeningIdentifierType(),
                    value = value,
                    issuingCountry = identification.attributeOrNull("countryIso2Code"),
                )
            }
            .toSet()

    private fun Element.programs(): Set<String> =
        childElements("regulation")
            .mapNotNull { it.attributeOrNull("programme") }
            .filter { it.isNotBlank() }
            .toSet()

    private fun String?.toScreeningIdentifierType(): ScreeningIdentifierType =
        when (this?.lowercase()) {
            "passport" -> ScreeningIdentifierType.PASSPORT
            "nationalid", "national_id", "identitycard" -> ScreeningIdentifierType.NATIONAL_ID
            "taxid", "tax_id" -> ScreeningIdentifierType.TAX_ID
            "registrationnumber", "registration_number", "companyregistrationnumber" ->
                ScreeningIdentifierType.COMPANY_REGISTRATION_NUMBER
            else -> ScreeningIdentifierType.OTHER
        }
}
