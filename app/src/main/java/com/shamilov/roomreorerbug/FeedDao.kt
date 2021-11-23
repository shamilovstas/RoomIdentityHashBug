package com.shamilov.roomreorerbug

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
abstract class FeedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entity: FeedEntity)

    @Query("SELECT * FROM feed_entity")
    abstract suspend fun entities(): List<FeedEntity>
}