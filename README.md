# Polar — Heart Rate Training App (COMPX551 Assignment Four, Group 6)

An Android app (Kotlin + Jetpack Compose) for the **Polar H10** chest strap. Users sign in, pick a sport, and train with live heart-rate charts and an intensity gauge. They can also take a resting ECG check and fill in a fitness assessment that personalises their heart-rate zones. Past workouts are kept with weekly and monthly summaries.

---

## Features

| Area | What it does |
|---|---|
| **Sign in / Sign up** | Username + password. Sign up also asks for first and last name. Stored in Room. |
| **Dashboard** | "Hello, *name*": average heart rate across all workouts, total workouts, last workout, and shortcuts to ECG Check and Assessment. |
| **Workout** | Choose one of 8 sports. Live timer, current / min / avg / max heart rate. Swipe between a 60-second line chart and an intensity gauge. Stop saves the session. |
| **Personal zones** | The gauge uses zones based on the user's max heart rate (220 − age from the assessment). Without an assessment it falls back to fixed zones. |
| **ECG Check** | 30-second resting ECG at 130 Hz. Detects R-peaks and calculates resting heart rate. |
| **Assessment** | Gender, age, height, weight, workouts per week, preferred intensity. Shows BMI, max heart rate, 5 zones and a target heart-rate range. |
| **History** | Minutes-per-day bar chart (week / month) with an average line, heart-rate range of the latest workout, last workout with calories (Keytel formula), list of all workouts. |
| **Workout detail** | Duration, calories, min / avg / max, and a zoomable chart of the full session's heart rate. |

---

## Tech stack

- **Kotlin**, **Jetpack Compose** (Material 3), single-module app, `minSdk 24`
- **Room** (with KSP) for persistence; **Flow** for screens that update automatically
- **Apache ECharts 5.6** in a `WebView` (bundled in `assets/`, works offline)
- **Polar BLE SDK**: planned (not yet integrated)

---

## Project structure

```
app/src/main/
├── java/com/example/polar/
│   ├── MainActivity.kt              Welcome screen → SignPage
│   ├── data/                        Storage only (no UI, no calculations)
│   │   ├── db/AppDatabase.kt        Room database (v4) + migrations 2→3, 3→4
│   │   ├── entity/                  Tables: User, Assessment, Workout
│   │   ├── dao/                     Queries: UserDao, AssessmentDao (@Upsert, Flow), WorkoutDao (Flow)
│   │   └── model/WorkoutType.kt     List of sports + emoji (not a table)
│   ├── logic/                       Pure functions (no UI, no database) — easy to unit test
│   │   ├── Health.kt                BMI, max HR, zone limits, calories (Keytel)
│   │   ├── Ecg.kt                   R-peak detection → BPM, simulated ECG signal
│   │   ├── History.kt               Minutes per day, day labels, start of day
│   │   └── Format.kt                "1 hr 20 min", "01:15"
│   └── ui/
│       ├── page/
│       │   ├── SignPage.kt          Sign in / sign up
│       │   ├── MainPage.kt          Dashboard, tabs, bottom bar, workout picker
│       │   ├── HistoryTab.kt        History tab cards
│       │   ├── WorkoutPage.kt       Live workout (pager: line chart / gauge)
│       │   ├── WorkoutDetailPage.kt One past workout, zoomable chart
│       │   ├── EcgPage.kt           30 s resting ECG screen
│       │   ├── AssessmentPage.kt    Assessment form + results
│       │   └── EChartsView.kt       Reusable WebView wrapper for ECharts
│       └── theme/                   Colours, fonts, Material theme
└── assets/                          ECharts pages
    ├── echarts.min.js
    ├── line_chart.html              Live HR, last 60 s
    ├── gauge.html                   Intensity gauge with personal zones
    ├── ecg.html                     ECG waveform, last 3 s
    ├── week_bars.html               Minutes per day + average line
    ├── hr_range.html                Floating min–max bars
    └── history_chart.html           Full-session HR with dataZoom
```

### How data flows

```
(Polar H10 → SDK)  ──►  WorkoutPage: heartRates list (Compose state)
                             │  every second
                             ├─► min / avg / max, zone  ──► ECharts (evaluateJavascript)
                             └─► Stop → Workout row in Room
                                              │  Flow
                                              ▼
                         MainPage / HistoryTab update automatically
```

### Database

| Table | Key | Notes |
|---|---|---|
| `users` | `id`, unique `username` | first name, last name, password (plain text — see Known limitations) |
| `assessments` | `username` | gender, age, heightCm, weightKg, workoutsPerWeek, intensity |
| `workouts` | `id` | username, type, startTime, durationSec, min/avg/max HR, `heartRates` CSV |

Migrations: v1→v2 destructive (no real users yet); v2→v3 and v3→v4 are real migrations that keep existing data.

---

## Processing

- **Session statistics**: min, average and max heart rate over the whole workout.
- **Zone classification**: each heart rate is put into Rest / Light / Moderate / Hard / Maximum using 50/60/70/80 % of max heart rate (220 − age).
- **R-peak detection** (ECG): a sample crossing 500 µV upwards counts as a beat. BPM = (peaks − 1) / time between the first and last peak × 60.
- **Calories**: Keytel et al. (2005). Separate male and female equations using heart rate, weight and age.
- **Daily aggregation**: workout seconds are grouped into calendar days for the week and month charts.

## Visualisation choices

- **Live line chart (last 60 s)**: easy to read at a glance during exercise. No zoom, so it doesn't clash with the swipe gesture.
- **Gauge**: shows at once "how hard am I working", coloured by personal zone.
- **ECG waveform**: uses an ECG-paper grid, and animation is off so the 10 Hz updates stay sharp.
- **Floating range bars**: show how much heart rate varied in each part of the workout.
- **Zoomable area chart**: only used for past workouts, where the user has time to explore.

---

## Build & run

1. Open in Android Studio (AGP 9, Kotlin 2.2, JDK 11+).
2. Run the `app` configuration on a phone (API 24+).
3. Create an account on the Sign Up screen, then start a workout from the orange button.

```bash
./gradlew :app:assembleDebug
```

## Known limitations

- Sensor data is simulated (Polar SDK not integrated yet).
- Passwords are stored in plain text; they should be hashed (PBKDF2/bcrypt).
- The login session is not remembered, and user info is passed between screens with Intent extras.
- Pressing system Back during a workout discards it; only **Stop** saves.
- The ECG R-peak threshold is a fixed number (500 µV).

## Roadmap

1. Polar BLE SDK: connect, search and pair; stream HR, RR intervals, accelerometer and ECG; handle permissions and reconnection.
2. HRV (RMSSD / SDNN) from RR intervals, with artifact filtering.
3. Accelerometer: step cadence and motion-artifact flags.
4. Training load (TRIMP), time-in-zone, heart-rate recovery after a workout.
5. Adaptive ECG peak detection (Pan–Tompkins).

## Team

Group 6 — COMPX551-26B, University of Waikato.
