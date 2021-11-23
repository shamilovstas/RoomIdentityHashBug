package com.shamilov.roomreorerbug

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(version = 1, entities = [FeedEntity::class], exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
}