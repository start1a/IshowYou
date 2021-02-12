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
}