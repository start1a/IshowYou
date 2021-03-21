package com.start3a.ishowyou.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RoomData_VideoSearch::class, VideoSearchHistory::class], version = 1)
abstract class RoomDatabase: RoomDatabase() {
    abstract fun youtubeDao(): YoutubeDao
}