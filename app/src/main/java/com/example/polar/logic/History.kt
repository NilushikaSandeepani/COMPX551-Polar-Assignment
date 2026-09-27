package com.example.polar.logic

import com.example.polar.data.entity.Workout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// Total workout minutes for each of the last [days] days, oldest first
fun minutesPerDay(workouts: List<Workout>, days: Int): List<Double> {
    val result = mutableListOf<Double>()
    for (i in days - 1 downTo 0) {
        val dayStart = startOfDay(daysAgo = i)
        val dayEnd = startOfDay(daysAgo = i - 1)
        var seconds = 0
        for (workout in workouts) {
            if (workout.startTime >= dayStart && workout.startTime < dayEnd) {
                seconds += workout.durationSec
            }
        }
        // Minutes with 1 decimal place
        result.add((seconds / 60.0 * 10).roundToInt() / 10.0)
    }
    return result
}

// Labels for the last [days] days, e.g. "Mon" (pattern "EEE") or "27" (pattern "d")
fun dayLabels(days: Int, pattern: String): List<String> {
    val format = SimpleDateFormat(pattern, Locale.ENGLISH)
    val labels = mutableListOf<String>()
    for (i in days - 1 downTo 0) {
        labels.add(format.format(Date(startOfDay(daysAgo = i))))
    }
    return labels
}

// Midnight of today minus [daysAgo] days, in milliseconds
fun startOfDay(daysAgo: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    calendar.add(Calendar.DAY_OF_MONTH, -daysAgo)
    return calendar.timeInMillis
}
