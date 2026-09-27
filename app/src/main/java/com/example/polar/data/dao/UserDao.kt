package com.example.polar.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.polar.data.entity.User

// https://developer.android.com/training/data-storage/room/accessing-data
@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?
}
