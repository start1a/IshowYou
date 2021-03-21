package com.start3a.ishowyou.model

import androidx.room.*

@Dao
interface YoutubeDao {

    @Query("SELECT * FROM RoomData_VideoSearch WHERE keyword = :keyword")
    fun getVideosByKeyword(keyword: String): List<RoomData_VideoSearch>

    @Insert
    fun insertVideos(videos: List<RoomData_VideoSearch>)

    @Delete
    fun deleteVideosByKeyword(vararg videos: RoomData_VideoSearch)

    @Query("SELECT * FROM VideoSearchHistory ORDER BY createdTime DESC LIMIT 20")
    fun getAllSearchKewordHistory(): List<VideoSearchHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSearchKeyword(record: VideoSearchHistory)

    @Delete
    fun deleteHistory(item: VideoSearchHistory)
}