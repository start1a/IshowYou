package com.start3a.ishowyou

import androidx.lifecycle.ViewModel
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.data.ListLiveData

class YoutubeVideoSelectionViewModel: ViewModel() {

    val listVideo: ListLiveData<YoutubeSearchData> by lazy {
        ListLiveData(mutableListOf())
    }

    val listVideoSelected: ListLiveData<YoutubeSearchData> by lazy {
        ListLiveData(mutableListOf())
    }

    // 현재 duration 추출 비디오 인덱스
    var indexDurationSave = 0
    // duration 추출 작업 종료 여부
    var isLoadVideosStarted = false
    var isEndVideoSelection = false
}