package com.embabel.metaagent.kycdemo.screening

import java.nio.file.Files
import java.nio.file.Path

data class AmlSanctionsFileSources(
    val euFinancialSanctionsXml: Path,
    val ofacSdnXml: Path,
) {
    init {
        requireReadableFile(euFinancialSanctionsXml, "EU financial sanctions XML")
        requireReadableFile(ofacSdnXml, "OFAC SDN XML")
    }

    private fun requireReadableFile(path: Path, label: String) {
        require(Files.isRegularFile(path)) { "$label must be an existing regular file: $path" }
        require(Files.isReadable(path)) { "$label must be readable: $path" }
    }
}

class AmlScreeningProviderLoader(
    private val sources: AmlSanctionsFileSources,
) {

    fun loadProviders(): List<NamedScreeningProvider> =
        listOf(
            EuFinancialSanctionsXmlProvider(
                providerId = "eu-financial-sanctions-file",
                xmlPath = sources.euFinancialSanctionsXml,
            ),
            OfacSanctionsXmlProvider(
                providerId = "ofac-sdn-file",
                xmlPath = sources.ofacSdnXml,
            ),
        )

    fun loadService(): AmlScreeningService =
        AmlScreeningService(loadProviders())
}
