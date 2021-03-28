package com.start3a.ishowyou.room.content.videoselection

import android.content.Context
import androidx.lifecycle.ViewModel
import com.start3a.ishowyou.contentapi.VideoSelectInfo
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.data.ListLiveData
import com.start3a.ishowyou.model.VideoSearchHistory
import com.start3a.ishowyou.repository.RepoVideoSelection
import java.util.*

class YoutubeVideoSelectionViewModel : ViewModel() {

    lateinit var context: Context
    lateinit var repo: RepoVideoSelection

    val listVideo: ListLiveData<YoutubeSearchData> by lazy {
        ListLiveData(mutableListOf())
    }

    val listVideoSelected: ListLiveData<YoutubeSearchData> by lazy {
        ListLiveData(mutableListOf())
    }

    val listSearchHistory = LinkedList<VideoSearchHistory>()

    // 비디오 선택 정보
    var selectedVideoInfoList = mutableListOf<VideoSelectInfo>()
    var curQueryKeyword = ""

    // 현재 duration 추출 비디오 인덱스
    var indexDurationSave = -1

    // duration 추출 작업 종료 여부
    var isLoadVideosStarted = false
    var isEndVideoSelection = false

    fun init(context: Context) {
        repo = RepoVideoSelection(context)
    }

    fun getVideosByKeyword(keyword: String, queryEnded: () -> Unit) {
        listVideo.value!!.clear()
        repo.getVideosByKeyword(keyword, listVideo, queryEnded)
    }

    fun getAllSearchHistory() {
        repo.getAllSearchHistory(listSearchHistory)
    }

    fun insertSearchKeyword(keyword: String) {
        for (i in 0 until listSearchHistory.size) {
            val item = listSearchHistory[i]

            if (keyword == item.keyword) {
                updateSearchKeyword(i)
                return
            }
        }

        // 중복 없음
        val record = VideoSearchHistory(keyword, Date().time)

        repo.insertSearchKeyword(record)
        listSearchHistory.addFirst(record)
        if (listSearchHistory.size > VideoSearchHistory.maxItem) {
            repo.deleteHistory(listSearchHistory.last)
            listSearchHistory.removeLast()
        }
    }

    fun updateSearchKeyword(index: Int) {
        val record = listSearchHistory[index].run {
            createdTime = Date().time
            this
        }

        repo.insertSearchKeyword(record)
        listSearchHistory.addFirst(record)
        listSearchHistory.removeAt(index + 1)
    }

    fun isQueryAvailable(keyword: String) =
        curQueryKeyword != keyword && keyword.isNotBlank() && keyword.isNotEmpty()
}