package com.example.polar.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// One row in the "users" table. Username is unique so two people can't use the same one.
@Entity(tableName = "users", indices = [Index(value = ["username"], unique = true)])
data class User(@PrimaryKey(autoGenerate = true) val id: Long = 0,
                val firstName: String,
                val lastName: String,
                val username: String,
                val password: String)
