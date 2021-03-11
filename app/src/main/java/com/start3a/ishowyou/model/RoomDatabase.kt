package com.start3a.ishowyou.model

import androidx.room.Database
import androidx.room.RoomDatabase
import com.start3a.ishowyou.contentapi.YoutubeVideoForRoomDB

@Database(entities = [YoutubeVideoForRoomDB::class], version = 1)
abstract class RoomDatabase: RoomDatabase() {
    abstract fun youtubeDao(): YoutubeDao
}