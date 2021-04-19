package com.start3a.ishowyou.model

import androidx.room.*

@Dao
interface YoutubeDao {

    // 검색 비디오 캐시
    @Query("SELECT * FROM RoomData_VideoSearch ORDER BY timeCreated")
    fun getAllCacheVideos(): List<RoomData_VideoSearch>

    @Query("SELECT * FROM RoomData_VideoSearch WHERE keyword = :keyword")
    fun getCacheVideosByKeyword(keyword: String): List<RoomData_VideoSearch>

    @Insert
    fun insertVideos(videos: List<RoomData_VideoSearch>)

    @Query("DELETE FROM RoomData_VideoSearch WHERE keyword = :keyword")
    fun deleteCacheVideo(keyword: String)


    // 검색 기록
    @Query("SELECT * FROM VideoSearchHistory ORDER BY createdTime DESC LIMIT 20")
    fun getAllSearchKewordHistory(): List<VideoSearchHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSearchKeyword(record: VideoSearchHistory)

    @Delete
    fun deleteHistory(item: VideoSearchHistory)

    @Query("DELETE FROM VideoSearchHistory")
    fun removeAllSearchKeywords()


    // 최근 시청 영상
    @Query("SELECT * FROM RoomData_CurWatchedVideo ORDER BY timeCreated DESC LIMIT 50")
    fun getCurWatchedVideos(): List<RoomData_CurWatchedVideo>

    @Query("DELETE FROM RoomData_CurWatchedVideo")
    fun removeAllCurWatchedVideo()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCurWatchedVideo(video: RoomData_CurWatchedVideo)
}