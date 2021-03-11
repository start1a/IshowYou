package com.start3a.ishowyou.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.start3a.ishowyou.contentapi.YoutubeVideoForRoomDB

@Dao
interface YoutubeDao {

    @Query("SELECT * FROM YoutubeVideoForRoomDB WHERE keyword = :keyword")
    fun getVideosByKeyword(keyword: String): List<YoutubeVideoForRoomDB>

    @Insert
    fun insertVideos(videos: List<YoutubeVideoForRoomDB>)

    @Delete
    fun deleteVideosByKeyword(vararg videos: YoutubeVideoForRoomDB)

}