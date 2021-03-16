package com.start3a.ishowyou.room.content.videoselection

import android.content.Context
import androidx.lifecycle.ViewModel
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.data.ListLiveData
import com.start3a.ishowyou.repository.RepoVideoSelection

class YoutubeVideoSelectionViewModel: ViewModel() {

    lateinit var context: Context
    lateinit var repo: RepoVideoSelection

    val listVideo: ListLiveData<YoutubeSearchData> by lazy {
        ListLiveData(mutableListOf())
    }

    val listVideoSelected: ListLiveData<YoutubeSearchData> by lazy {
        ListLiveData(mutableListOf())
    }
    // 선택 비디오들의 탐색 비디오 인덱스가 저장됨
    // 비디오 삭제 시 참조하여 탐색 비디오 체크 해제
    var searchedVideoIndexList = mutableListOf<Int>()

    // 현재 duration 추출 비디오 인덱스
    var indexDurationSave = -1
    // duration 추출 작업 종료 여부
    var isLoadVideosStarted = false
    var isEndVideoSelection = false

    fun init(context: Context) {
        repo = RepoVideoSelection(context)
    }

    fun getVideosByKeyword(keyword: String) {
        listVideo.value!!.clear()
        repo.getVideosByKeyword(keyword, listVideo)
    }
}