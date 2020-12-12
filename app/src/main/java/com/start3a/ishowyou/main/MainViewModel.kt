package com.start3a.ishowyou.main

import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.start3a.ishowyou.data.ChatRoom
import com.start3a.ishowyou.model.ChatDao
import com.start3a.ishowyou.model.YoutubeDao
import java.util.*

class MainViewModel : ViewModel() {

    // 채팅방 정보
    private var chatroomCode: String? = null
    var isHost = false
    var isJoinRoom = false

    // 채팅방 유무 뷰 전환
    lateinit var createChatRoomViewListener: (() -> Unit)

    private val dbYoutube = YoutubeDao(FirebaseDatabase.getInstance().reference)
    private val dbChat = ChatDao(FirebaseDatabase.getInstance().reference)

    fun seekBarYoutubeClicked(time: Float) {
        dbYoutube.seekBarYoutubeClicked(time.toDouble())
    }

    fun setYoutubeSeekbarChangedListener(changedListener: (Float) -> Unit) {
        dbYoutube.setSeekbarChangedListener(changedListener)
    }

    fun removeYoutubeSeekbarChangedListener() {
        dbYoutube.removeSeekbarChangedListener()
    }

    fun createChatRoom(
        title: String,
        successListener: () -> Unit,
        roomInfoChangedListener: () -> Unit
    ) {
        chatroomCode = UUID.randomUUID().toString()
        isHost = true
        dbChat.createChatRoom(chatroomCode!!, title, successListener, roomInfoChangedListener)
    }

    fun leaveRoom() {
        dbChat.leaveRoom(chatroomCode!!)
        chatroomCode = null
        isJoinRoom = false
        isHost = false
        createChatRoomViewListener()
    }


}