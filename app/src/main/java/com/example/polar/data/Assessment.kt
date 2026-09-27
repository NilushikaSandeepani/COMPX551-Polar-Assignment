package com.example.polar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// One assessment per user, so username is the primary key.
// Saving again just replaces the old one.
@Entity(tableName = "assessments")
data class Assessment(@PrimaryKey val username: String,
                      val gender: String,          // "Male" or "Female"
                      val age: Int,
                      val heightCm: Int,
                      val weightKg: Double,
                      val workoutsPerWeek: String, // "0", "1-2", "3-4" or "5+"
                      val intensity: String)       // "Light", "Moderate" or "Hard"
