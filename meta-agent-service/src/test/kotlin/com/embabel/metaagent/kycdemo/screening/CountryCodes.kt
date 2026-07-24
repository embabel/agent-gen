package com.embabel.metaagent.kycdemo.screening

import java.util.Locale

fun String.normalizedCountryCode(): String =
    trim()
        .uppercase()
        .let { country ->
            isoCountryNames[country] ?: country
        }

private val isoCountryNames: Map<String, String> =
    Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2)
        .associateBy(
            keySelector = { Locale.Builder().setRegion(it).build().displayCountry.uppercase() },
            valueTransform = { it },
        )
