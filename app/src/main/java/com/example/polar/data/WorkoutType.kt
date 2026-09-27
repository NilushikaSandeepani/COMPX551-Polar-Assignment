package com.example.polar.data

data class WorkoutType(val name: String, val emoji: String)

// All the workouts the user can pick from before starting
val workoutTypes = listOf(
    WorkoutType("Running", "🏃"),
    WorkoutType("Walking", "🚶"),
    WorkoutType("Swimming", "🏊"),
    WorkoutType("Hiking", "🥾"),
    WorkoutType("Badminton", "🏸"),
    WorkoutType("Rugby", "🏉"),
    WorkoutType("Tennis", "🎾"),
    WorkoutType("Strength Training", "🏋️")
)
