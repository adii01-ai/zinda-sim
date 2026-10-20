package com.simkeeper.app.ui.format

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** All dates go through here, formatted with the device (or per-app) locale. */
object DateFormats {
    /** "21 Oct 2026" */
    fun date(date: LocalDate, locale: Locale): String = pattern("d MMM yyyy", locale).format(date)

    /** "16 Nov" */
    fun dayMonth(date: LocalDate, locale: Locale): String = pattern("d MMM", locale).format(date)

    /** "Wed, 21 Oct" */
    fun weekdayDayMonth(date: LocalDate, locale: Locale): String = pattern("EEE, d MMM", locale).format(date)

    /** "21 October 2026" for TalkBack. */
    fun spoken(date: LocalDate, locale: Locale): String = pattern("d MMMM yyyy", locale).format(date)

    /** "October 2026" */
    fun monthYear(month: YearMonth, locale: Locale): String = pattern("MMMM yyyy", locale).format(month)

    private fun pattern(pattern: String, locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern(pattern, locale)
}

val Context.currentLocale: Locale get() = resources.configuration.locales[0]

@Composable
@ReadOnlyComposable
fun currentLocale(): Locale = LocalConfiguration.current.locales[0]
