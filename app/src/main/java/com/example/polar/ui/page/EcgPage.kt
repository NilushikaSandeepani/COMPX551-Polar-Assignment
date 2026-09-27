package com.example.polar.ui.page

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polar.R
import com.example.polar.ui.theme.Orange
import com.example.polar.ui.theme.PolarTheme
import com.example.polar.ui.theme.WorkSans
import kotlinx.coroutines.delay
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.random.Random

// Polar H10 sends 130 ECG samples per second
const val ECG_SAMPLE_RATE = 130
const val ECG_SECONDS = 30

class EcgPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolarTheme {
                EcgScreen()
            }
        }
    }
}

@Composable
fun EcgScreen() {
    val context = LocalContext.current

    // "ready" -> "measuring" -> "done"
    var status by remember { mutableStateOf("ready") }
    var secondsLeft by remember { mutableIntStateOf(ECG_SECONDS) }
    var restingHr by remember { mutableIntStateOf(0) }
    // Every sample of this reading (30s x 130 = 3900 samples)
    val allSamples = remember { mutableStateListOf<Int>() }

    // Runs every time status changes. Only does work when measuring.
    LaunchedEffect(status) {
        if (status == "measuring") {
            allSamples.clear()
            // TODO: replace this fake ECG with the real ECG stream from the Polar SDK
            val fakeHr = Random.nextInt(58, 72)
            var phase = 0.0 // where we are inside one heart beat, 0.0 to 1.0

            // 10 times a second, add 13 samples (= 130 per second)
            for (tick in 1..ECG_SECONDS * 10) {
                delay(100)
                for (i in 0 until 13) {
                    phase += (fakeHr / 60.0) / ECG_SAMPLE_RATE
                    if (phase >= 1.0) phase -= 1.0
                    allSamples.add(fakeEcgValue(phase))
                }
                secondsLeft = ECG_SECONDS - tick / 10
            }

            restingHr = heartRateFromEcg(allSamples)
            status = "done"
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.illustration_fitness_equipments_design_background),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            Text(
                text = "ECG Check",
                color = Color.White,
                fontSize = 36.sp,
                fontFamily = WorkSans,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when (status) {
                    "ready" -> "Sit down, relax and stay still. The reading takes $ECG_SECONDS seconds."
                    "measuring" -> "Recording... stay still. $secondsLeft s left"
                    else -> "Done! Here is your result."
                },
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ECG chart, shows the last 3 seconds
            GlassCard(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)) {
                EChartsView(
                    fileName = "ecg.html",
                    script = "setData(${allSamples.takeLast(ECG_SAMPLE_RATE * 3)})"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Result is only shown after the reading is finished
            if (status == "done") {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(text = "Resting Heart Rate", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(text = "$restingHr", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "bpm",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Text(
                            text = restingHrComment(restingHr),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    if (status == "done") {
                        (context as Activity).finish()
                    } else {
                        secondsLeft = ECG_SECONDS
                        status = "measuring"
                    }
                },
                enabled = status != "measuring",
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF111111),
                    disabledContentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = when (status) {
                        "ready" -> "Start ECG"
                        "measuring" -> "Recording... $secondsLeft s"
                        else -> "Done"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

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
