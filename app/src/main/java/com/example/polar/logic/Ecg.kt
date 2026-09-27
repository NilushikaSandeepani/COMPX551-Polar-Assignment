package com.example.polar.logic

import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.random.Random

// Polar H10 sends 130 ECG samples per second
const val ECG_SAMPLE_RATE = 130

// Finds the R peaks (the big spikes) and works out beats per minute.
// A peak is when the signal goes from below 500 µV to above 500 µV.
fun heartRateFromEcg(samples: List<Int>): Int {
    val peaks = mutableListOf<Int>() // index of each peak
    for (i in 1 until samples.size) {
        if (samples[i - 1] < 500 && samples[i] >= 500) {
            peaks.add(i)
        }
    }
    if (peaks.size < 2) {
        return 0
    }
    // Time from first peak to last peak, in seconds
    val seconds = (peaks.last() - peaks.first()).toDouble() / ECG_SAMPLE_RATE
    val beats = peaks.size - 1
    return (beats / seconds * 60).roundToInt()
}

// Normal resting heart rate for adults is about 60 - 100 bpm
fun restingHrComment(bpm: Int): String {
    return when {
        bpm == 0 -> "Could not find a heart beat, please try again."
        bpm < 60 -> "Below 60 bpm, common for fit people."
        bpm <= 100 -> "Normal range (60 - 100 bpm)."
        else -> "Above 100 bpm, try to relax and measure again."
    }
}

// Makes a fake ECG value (in microvolts) for a point inside one heart beat.
// A real beat has P, Q, R, S and T waves, each one is a small bump made with exp().
fun fakeEcgValue(phase: Double): Int {
    fun bump(center: Double, width: Double, height: Double): Double {
        val x = (phase - center) / width
        return height * exp(-x * x)
    }

    val value = bump(0.20, 0.025, 100.0) +   // P wave
            bump(0.37, 0.010, -100.0) +      // Q
            bump(0.40, 0.012, 1000.0) +      // R (the big spike)
            bump(0.43, 0.010, -250.0) +      // S
            bump(0.65, 0.040, 300.0) +       // T wave
            Random.nextInt(-20, 20)          // a little noise
    return value.toInt()
}
