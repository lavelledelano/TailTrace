package com.example.tailtrace

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class HelpersTest {
    @Test fun validEmailAccepted() = assertTrue(Validator.isValidEmail("owner@example.com"))
    @Test fun invalidEmailRejected() = assertFalse(Validator.isValidEmail("not-an-email"))
    @Test fun shortPasswordRejected() = assertFalse(Validator.isStrongPassword("abc123"))
    @Test fun passwordWithoutNumberRejected() = assertFalse(Validator.isStrongPassword("onlyletters"))
    @Test fun strongPasswordAccepted() = assertTrue(Validator.isStrongPassword("paws4ever"))
    @Test fun blankPhoneIsAllowed() = assertTrue(Validator.isValidPhone(""))
    @Test fun badPhoneRejected() = assertFalse(Validator.isValidPhone("12ab"))

    @Test fun timeAgoMinutes() = assertEquals("10 min ago", timeAgo(Instant.now().minusSeconds(605).toString()))
    @Test fun timeAgoHours() = assertEquals("2 h ago", timeAgo(Instant.now().minusSeconds(7300).toString()))
    @Test fun timeAgoDays() = assertEquals("3 d ago", timeAgo(Instant.now().minusSeconds(3 * 86400 + 60L).toString()))
}