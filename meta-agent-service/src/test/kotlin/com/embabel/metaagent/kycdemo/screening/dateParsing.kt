package com.embabel.metaagent.kycdemo.screening

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

private val screeningDateFormats = listOf(
    DateTimeFormatter.ISO_LOCAL_DATE,
    DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("d MMM uuuu")
        .toFormatter(Locale.ENGLISH)
        .withResolverStyle(ResolverStyle.SMART),
    DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("d MMMM uuuu")
        .toFormatter(Locale.ENGLISH)
        .withResolverStyle(ResolverStyle.SMART),
    DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("MMM d, uuuu")
        .toFormatter(Locale.ENGLISH)
        .withResolverStyle(ResolverStyle.SMART),
    DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("MMMM d, uuuu")
        .toFormatter(Locale.ENGLISH)
        .withResolverStyle(ResolverStyle.SMART),
)

fun String.toScreeningDateOrNull(): LocalDate? {
    val value = trim()
    if (value.isBlank() || value.matches(Regex("\\d{4}"))) {
        return null
    }

    return screeningDateFormats.firstNotNullOfOrNull { formatter ->
        try {
            LocalDate.parse(value, formatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
