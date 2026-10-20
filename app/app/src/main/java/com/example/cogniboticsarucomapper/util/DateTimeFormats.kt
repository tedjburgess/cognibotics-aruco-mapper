package com.example.cogniboticsarucomapper.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeFormats {
    fun formatUtcTimestampForDevice(value: String): String = runCatching {
        DateTimeFormatter
            .ofPattern("EEE, d MMM yyyy, HH:mm z", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
            .format(Instant.parse(value))
    }.getOrDefault(value)
}
