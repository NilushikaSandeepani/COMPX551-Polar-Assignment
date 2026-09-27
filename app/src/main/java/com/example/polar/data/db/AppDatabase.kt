package com.example.polar.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.polar.data.dao.AssessmentDao
import com.example.polar.data.dao.UserDao
import com.example.polar.data.dao.WorkoutDao
import com.example.polar.data.entity.Assessment
import com.example.polar.data.entity.User
import com.example.polar.data.entity.Workout

// https://developer.android.com/training/data-storage/room
@Database(entities = [User::class, Assessment::class, Workout::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun assessmentDao(): AssessmentDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        // Version 3 adds the assessments table. Users are kept.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `assessments` (" +
                            "`username` TEXT NOT NULL, " +
                            "`gender` TEXT NOT NULL, " +
                            "`age` INTEGER NOT NULL, " +
                            "`heightCm` INTEGER NOT NULL, " +
                            "`weightKg` REAL NOT NULL, " +
                            "`workoutsPerWeek` TEXT NOT NULL, " +
                            "`intensity` TEXT NOT NULL, " +
                            "PRIMARY KEY(`username`))"
                )
            }
        }

        // Version 4 adds the workouts table
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `workouts` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`username` TEXT NOT NULL, " +
                            "`type` TEXT NOT NULL, " +
                            "`startTime` INTEGER NOT NULL, " +
                            "`durationSec` INTEGER NOT NULL, " +
                            "`minHr` INTEGER NOT NULL, " +
                            "`avgHr` INTEGER NOT NULL, " +
                            "`maxHr` INTEGER NOT NULL, " +
                            "`heartRates` TEXT NOT NULL)"
                )
            }
        }

        // Only create the database once for the whole app
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "polar_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    // Version 1 used email, version 2 uses username.
                    // No real users yet, so just delete the old table instead of writing a migration.
                    .fallbackToDestructiveMigration(true)
                    .build()
                instance = db
                db
            }
        }
    }
}
