package com.start3a.ishowyou.repository

import android.content.Context
import android.text.Html
import android.util.Log
import androidx.room.Room
import com.start3a.ishowyou.contentapi.RetrofitYoutubeService
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.contentapi.YoutubeSearchJsonData
import com.start3a.ishowyou.data.ListLiveData
import com.start3a.ishowyou.model.RoomData_VideoSearch
import com.start3a.ishowyou.model.RoomDatabase
import com.start3a.ishowyou.model.VideoSearchHistory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class RepoVideoSelection(context: Context) {

    private val TAG = "RepoVideoSelection"

    private val roomDB =
        Room.databaseBuilder(context, RoomDatabase::class.java, "database-room")
            .allowMainThreadQueries()
            .build()
    private val youtubeVideoAPI = RetrofitYoutubeService()

    fun getVideosByKeyword(
        keyword: String,
        videoList: ListLiveData<YoutubeSearchData>,
        queryEnded: () -> Unit
    ) {
        val videosByRoom = roomDB.youtubeDao().getCacheVideosByKeyword(keyword)

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
            queryEnded()
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
                                val roomSaveList = mutableListOf<RoomData_VideoSearch>()
                                response.body()!!.items.forEach {

                                    val video = YoutubeSearchData().apply {
                                        title = converHtmlEntity_toString(it.snippet.title)
                                        desc = converHtmlEntity_toString(it.snippet.description)
                                        channelTitle =
                                            converHtmlEntity_toString(it.snippet.channelTitle)
                                        videoId = it.id.videoId
                                        thumbnail = it.snippet.thumbnails.high.url
                                        thumbnailSmall = it.snippet.thumbnails.default.url
                                    }

                                    val videoForRoomDB = RoomData_VideoSearch(
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
                            queryEnded()
                        }

                        override fun onFailure(call: Call<YoutubeSearchJsonData>, t: Throwable) {
                            Log.d(TAG, "youtube video search is failed.\n$t")
                            queryEnded()
                        }
                    })
            }.run()
        }
    }

    private fun insertVideos(videos: List<RoomData_VideoSearch>) {
        roomDB.youtubeDao().insertVideos(videos)
    }

    fun getAllSearchHistory(listSearchHistory: LinkedList<VideoSearchHistory>) {
        roomDB.youtubeDao().getAllSearchKewordHistory().let { keywordsByRoom ->
            keywordsByRoom.forEach {
                listSearchHistory.add(it)
            }
        }
    }

    fun insertSearchKeyword(record: VideoSearchHistory) {
        roomDB.youtubeDao().insertSearchKeyword(record)
    }

    // html 특수문자 처리 ex) &#38;, &#39; -> &, '
    private fun converHtmlEntity_toString(str: String) =
        Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY).toString()

    fun deleteHistory(item: VideoSearchHistory) {
        roomDB.youtubeDao().deleteHistory(item)
    }
}