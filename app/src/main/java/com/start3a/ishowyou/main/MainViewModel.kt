package com.start3a.ishowyou.main

import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.start3a.ishowyou.model.YoutubeDao

class MainViewModel: ViewModel() {

    private var chatroomCode: String? = null
    private val dbYoutube = YoutubeDao(FirebaseDatabase.getInstance().reference)

    fun seekBarYoutubeClicked(time: Float) {
        dbYoutube.seekBarYoutubeClicked(time.toDouble())
    }

    fun setYoutubeSeekbarChangedListener(changedListener: (Float) -> Unit) {
        dbYoutube.setSeekbarChangedListener(changedListener)
    }

    fun removeYoutubeSeekbarChangedListener() {
        dbYoutube.removeSeekbarChangedListener()
    }
}