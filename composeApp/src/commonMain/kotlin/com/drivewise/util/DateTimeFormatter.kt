package com.drivewise.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
object DateTimeFormatter {

    /**
     * Example outputs:
     * EN → "23 Dec • 10:33"
     * DE → "23 Dez • 10:33"
     * TR → "23 Ara • 10:33"
     */
    fun formatLessonTitle(
        startedAtMs: Long,
        languageCode: String,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        val dt = Instant
            .fromEpochMilliseconds(startedAtMs)
            .toLocalDateTime(timeZone)

        val day = dt.day
        val month = monthShort(dt.month.number, languageCode)
        val hh = dt.hour.pad2()
        val mm = dt.minute.pad2()

        return "$day $month • $hh:$mm"
    }

    private fun Int.pad2(): String =
        if (this < 10) "0$this" else toString()

    private fun monthShort(month: Int, lang: String): String {
        return when (lang.lowercase()) {
            "tr" -> monthTr(month)
            "de" -> monthDe(month)
            else -> monthEn(month)
        }
    }

    // ---- EN ----
    private fun monthEn(m: Int) = when (m) {
        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
        5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
        9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
        else -> "?"
    }

    // ---- DE ----
    private fun monthDe(m: Int) = when (m) {
        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mär"; 4 -> "Apr"
        5 -> "Mai"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
        9 -> "Sep"; 10 -> "Okt"; 11 -> "Nov"; 12 -> "Dez"
        else -> "?"
    }

    // ---- TR ----
    private fun monthTr(m: Int) = when (m) {
        1 -> "Oca"; 2 -> "Şub"; 3 -> "Mar"; 4 -> "Nis"
        5 -> "May"; 6 -> "Haz"; 7 -> "Tem"; 8 -> "Ağu"
        9 -> "Eyl"; 10 -> "Eki"; 11 -> "Kas"; 12 -> "Ara"
        else -> "?"
    }
}
