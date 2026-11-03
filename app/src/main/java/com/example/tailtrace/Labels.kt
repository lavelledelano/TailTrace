package com.example.tailtrace

import android.content.Context
import java.time.Duration
import java.time.Instant

fun timeAgoL(ctx: Context, iso: String): String {
    val mins = runCatching { Duration.between(Instant.parse(iso), Instant.now()).toMinutes() }.getOrDefault(0L)
    return when {
        mins < 1 -> ctx.getString(R.string.time_now)
        mins < 60 -> ctx.getString(R.string.time_min, mins.toInt())
        mins < 1440 -> ctx.getString(R.string.time_h, (mins / 60).toInt())
        else -> ctx.getString(R.string.time_d, (mins / 1440).toInt())
    }
}

fun statusLabel(ctx: Context, s: String): String = when (s) {
    "ACTIVE" -> ctx.getString(R.string.status_active)
    "PAUSED" -> ctx.getString(R.string.status_paused)
    "REUNITED" -> ctx.getString(R.string.status_reunited)
    "CLOSED" -> ctx.getString(R.string.status_closed)
    else -> s
}

fun speciesLabel(ctx: Context, s: String): String = when (s) {
    "All" -> ctx.getString(R.string.filter_all)
    "Dog" -> ctx.getString(R.string.species_dog)
    "Cat" -> ctx.getString(R.string.species_cat)
    "Other" -> ctx.getString(R.string.species_other)
    else -> s
}