package com.shamilov.roomreorerbug

import android.app.Application
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val database = Room.databaseBuilder(this, AppDatabase::class.java, "mydatabase.db")
            .allowMainThreadQueries()
            .build()
        val dao = database.feedDao()
        GlobalScope.launch {
            repeat(5) {
                dao.insert(FeedEntity(name = "entity#$it"))
            }

            val list = dao.entities()
            for (entity in list) {
                Log.d("RoomReorderBug", entity.toString())
            }
        }
    }
}