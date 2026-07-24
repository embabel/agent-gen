package com.embabel.metaagent.kycdemo.screening

import org.w3c.dom.Element
import java.nio.file.Path
import java.time.LocalDate

class OfacSanctionsXmlProvider(
    override val providerId: String,
    private val xmlPath: Path,
    private val sourceUrl: String = "https://ofac.treasury.gov/sanctions-list-service",
) : NamedScreeningProvider {

    override val source: ScreeningSource = ScreeningSource.OFAC_SANCTIONS

    private val entries: List<ScreeningListEntry> by lazy {
        parseEntries(xmlPath)
    }

    override fun search(subject: ScreeningSubject): List<ScreeningListEntry> =
        entries.filter { it.entryType == subject.subjectType }

    private fun parseEntries(path: Path): List<ScreeningListEntry> =
        path.parseXmlDocument()
            .getElementsByTagName("sdnEntry")
            .asElements()
            .mapNotNull(::parseEntry)

    private fun parseEntry(entry: Element): ScreeningListEntry? {
        val primaryName = entry.primaryName() ?: return null

        return ScreeningListEntry(
            source = ScreeningSource.OFAC_SANCTIONS,
            sourceRecordId = entry.firstChildTextOrNull("uid") ?: primaryName,
            primaryName = primaryName,
            aliases = entry.aliases(),
            entryType = entry.screeningSubjectType(),
            dateOfBirth = entry.dateOfBirth(),
            countries = entry.countryCodes(),
            addresses = entry.addresses(),
            identifiers = entry.identifiers(),
            programs = entry.childTextValues("program").toSet(),
            sourceUrl = sourceUrl,
        )
    }

    private fun Element.primaryName(): String? {
        val firstName = firstChildTextOrNull("firstName")
        val lastName = firstChildTextOrNull("lastName")

        return listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .trim()
            .takeIf { it.isNotBlank() }
    }

    private fun Element.aliases(): Set<String> =
        childElements("aka")
            .mapNotNull { aka ->
                listOfNotNull(
                    aka.firstChildTextOrNull("firstName"),
                    aka.firstChildTextOrNull("lastName"),
                ).joinToString(" ").trim().takeIf { it.isNotBlank() }
            }
            .toSet()

    private fun Element.screeningSubjectType(): ScreeningSubjectType =
        when (firstChildTextOrNull("sdnType")?.lowercase()) {
            "entity" -> ScreeningSubjectType.LEGAL_ENTITY
            else -> ScreeningSubjectType.PERSON
        }

    private fun Element.dateOfBirth(): LocalDate? =
        childTextValues("dateOfBirth")
            .firstNotNullOfOrNull { it.toScreeningDateOrNull() }

    private fun Element.countryCodes(): Set<String> =
        (childTextValues("country") + childTextValues("idCountry"))
            .map { it.uppercase() }
            .filter { it.isNotBlank() }
            .toSet()

    private fun Element.addresses(): Set<String> =
        childElements("address")
            .mapNotNull { address ->
                listOfNotNull(
                    address.firstChildTextOrNull("address1"),
                    address.firstChildTextOrNull("address2"),
                    address.firstChildTextOrNull("address3"),
                    address.firstChildTextOrNull("city"),
                    address.firstChildTextOrNull("stateOrProvince"),
                    address.firstChildTextOrNull("postalCode"),
                    address.firstChildTextOrNull("country"),
                )
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
                    .takeIf { it.isNotBlank() }
            }
            .toSet()

    private fun Element.identifiers(): Set<ScreeningIdentifier> =
        childElements("id")
            .mapNotNull { id ->
                val value = id.firstChildTextOrNull("idNumber") ?: return@mapNotNull null
                ScreeningIdentifier(
                    type = id.firstChildTextOrNull("idType").toScreeningIdentifierType(),
                    value = value,
                    issuingCountry = id.firstChildTextOrNull("idCountry"),
                )
            }
            .toSet()

    private fun String?.toScreeningIdentifierType(): ScreeningIdentifierType =
        when (this?.lowercase()) {
            "passport" -> ScreeningIdentifierType.PASSPORT
            "national id", "national_id", "nationalid" -> ScreeningIdentifierType.NATIONAL_ID
            "tax id", "tax_id", "taxid" -> ScreeningIdentifierType.TAX_ID
            "registration number", "registration_number", "company registration number",
            "registration id", "certificate of incorporation number" ->
                ScreeningIdentifierType.COMPANY_REGISTRATION_NUMBER
            else -> ScreeningIdentifierType.OTHER
        }
}
