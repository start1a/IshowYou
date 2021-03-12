package com.start3a.ishowyou.repository

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.start3a.ishowyou.contentapi.RetrofitYoutubeService
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.contentapi.YoutubeSearchJsonData
import com.start3a.ishowyou.contentapi.YoutubeVideoForRoomDB
import com.start3a.ishowyou.data.ListLiveData
import com.start3a.ishowyou.model.RoomDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoVideoSelection(context: Context) {

    private val TAG = "RepoVideoSelection"

    private val roomDB =
        Room.databaseBuilder(context, RoomDatabase::class.java, "database-room")
            .allowMainThreadQueries()
            .build()
    private val youtubeVideoAPI = RetrofitYoutubeService()

    fun getVideosByKeyword(keyword: String, videoList: ListLiveData<YoutubeSearchData>) {
        val videosByRoom = roomDB.youtubeDao().getVideosByKeyword(keyword)

        // Room에 캐시 체크
        if (videosByRoom.isNotEmpty()) {
            val list = mutableListOf<YoutubeSearchData>()
            videosByRoom.forEach {
                list.add(YoutubeSearchData().apply {
                    title = it.title
                    desc = it.desc
                    channelTitle = it.channelTitle
                    videoId = it.videoId
                    thumbnail = it.thumbnail
                    thumbnailSmall = it.thumbnailSmall
                })
            }
            videoList.addAll(list)
        }

        // 캐시 없음
        else {
            Runnable {
                youtubeVideoAPI.getService().getSearchedVideoList(keyword)
                    .enqueue(object :
                        Callback<YoutubeSearchJsonData> {
                        override fun onResponse(
                            call: Call<YoutubeSearchJsonData>,
                            response: Response<YoutubeSearchJsonData>
                        ) {
                            if (response.isSuccessful) {
                                val searchedVideoList = mutableListOf<YoutubeSearchData>()
                                val roomSaveList = mutableListOf<YoutubeVideoForRoomDB>()
                                response.body()!!.items.forEach {
                                    val video = YoutubeSearchData().apply {
                                        title = it.snippet.title
                                        desc = it.snippet.description
                                        channelTitle = it.snippet.channelTitle
                                        videoId = it.id.videoId
                                        thumbnail = it.snippet.thumbnails.high.url
                                        thumbnailSmall = it.snippet.thumbnails.default.url
                                    }
                                    val videoForRoomDB = YoutubeVideoForRoomDB(
                                        video.title,
                                        video.desc,
                                        video.channelTitle,
                                        video.videoId,
                                        video.thumbnail,
                                        video.thumbnailSmall,
                                        keyword
                                    )
                                    searchedVideoList.add(video)
                                    roomSaveList.add(videoForRoomDB)
                                }
                                videoList.addAll(searchedVideoList)
                                insertVideos(roomSaveList)
                            }
                        }

                        override fun onFailure(call: Call<YoutubeSearchJsonData>, t: Throwable) {
                            Log.d(TAG, "youtube video search is failed.\n$t")
                        }
                    })
            }.run()
        }
    }

    private fun insertVideos(videos: List<YoutubeVideoForRoomDB>) {
        roomDB.youtubeDao().insertVideos(videos)
    }
}