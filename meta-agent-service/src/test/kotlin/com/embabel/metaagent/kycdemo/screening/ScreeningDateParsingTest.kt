package com.embabel.metaagent.kycdemo.screening

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ScreeningDateParsingTest {

    @Test
    fun `parses exact screening dates and ignores partial dates`() {
        assertEquals(LocalDate.parse("1970-03-20"), "1970-03-20".toScreeningDateOrNull())
        assertEquals(LocalDate.parse("1970-03-20"), "20 Mar 1970".toScreeningDateOrNull())
        assertEquals(LocalDate.parse("1970-03-20"), "March 20, 1970".toScreeningDateOrNull())
        assertNull("1970".toScreeningDateOrNull())
        assertNull("unknown".toScreeningDateOrNull())
    }
}
