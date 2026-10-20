package com.example.cogniboticsarucomapper.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeFormats {
    fun formatUtcTimestampForDevice(value: String): String = runCatching {
        formatInstant(Instant.parse(value))
    }.getOrDefault(value)

    fun formatEpochMillisForDevice(value: Long): String = formatInstant(Instant.ofEpochMilli(value))

    private fun formatInstant(value: Instant): String = DateTimeFormatter
        .ofPattern("EEE, d MMM yyyy, HH:mm z", Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(value)
}
