package com.start3a.ishowyou.main

import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.start3a.ishowyou.model.RdbDao

class MainViewModel: ViewModel() {

    private var chatroomCode: String? = null
    private val db = RdbDao(FirebaseDatabase.getInstance().reference)

    fun seekBarYoutubeClicked(time: Float) {
        db.seekBarYoutubeClicked(time.toDouble())
    }

    fun setYoutubeSeekbarChangedListener(changedListener: (Float) -> Unit) {
        db.setSeekbarChangedListener(changedListener)
    }

    fun removeYoutubeSeekbarChangedListener() {
        db.removeSeekbarChangedListener()
    }
}