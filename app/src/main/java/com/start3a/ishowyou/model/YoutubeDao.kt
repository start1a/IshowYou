package com.start3a.ishowyou.model

import androidx.room.*

@Dao
interface YoutubeDao {

    @Query("SELECT * FROM RoomData_VideoSearch WHERE keyword = :keyword")
    fun getCacheVideosByKeyword(keyword: String): List<RoomData_VideoSearch>

    @Insert
    fun insertVideos(videos: List<RoomData_VideoSearch>)

    @Query("DELETE FROM RoomData_VideoSearch WHERE keyword = :keyword")
    fun deleteCacheVideo(keyword: String)

    @Query("SELECT * FROM VideoSearchHistory ORDER BY createdTime DESC LIMIT 20")
    fun getAllSearchKewordHistory(): List<VideoSearchHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSearchKeyword(record: VideoSearchHistory)

    @Delete
    fun deleteHistory(item: VideoSearchHistory)

    @Query("SELECT * FROM RoomData_VideoSearch ORDER BY timeCreated")
    fun getAllCacheVideos(): List<RoomData_VideoSearch>
}