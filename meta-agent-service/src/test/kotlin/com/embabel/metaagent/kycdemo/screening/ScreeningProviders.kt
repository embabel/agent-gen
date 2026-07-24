package com.embabel.metaagent.kycdemo.screening

fun interface ScreeningProvider {
    fun search(subject: ScreeningSubject): List<ScreeningListEntry>
}

interface NamedScreeningProvider : ScreeningProvider {
    val providerId: String
    val source: ScreeningSource
}

data class StaticScreeningProvider(
    override val providerId: String,
    override val source: ScreeningSource,
    private val entries: List<ScreeningListEntry>,
) : NamedScreeningProvider {

    override fun search(subject: ScreeningSubject): List<ScreeningListEntry> =
        entries.filter { it.source == source && it.entryType == subject.subjectType }
}

data class ScreeningProviderResult(
    val providerId: String,
    val source: ScreeningSource,
    val entries: List<ScreeningListEntry>,
)
