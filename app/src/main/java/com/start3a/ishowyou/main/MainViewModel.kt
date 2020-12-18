package com.start3a.ishowyou.main

import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.start3a.ishowyou.data.ChatMember
import com.start3a.ishowyou.data.ChatMessage
import com.start3a.ishowyou.data.ChatRoom
import com.start3a.ishowyou.model.ChatDao
import com.start3a.ishowyou.model.YoutubeDao
import java.util.*

class MainViewModel : ViewModel() {

    // 채팅방 정보
    private var chatroomCode: String? = null
    var isHost = false
    var isJoinRoom = false
    lateinit var openChatRoomMenu: () -> Unit

    // 메세지
    val listMessage: MutableList<ChatMessage> by lazy {
        mutableListOf()
    }

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
        roomInfoChangedListener: (ChatRoom) -> Unit
    ) {
        chatroomCode = UUID.randomUUID().toString()
        isHost = true
        dbChat.createChatRoom(chatroomCode!!, ChatRoom(title), successListener, roomInfoChangedListener)
    }

    fun leaveRoom() {
        dbChat.leaveRoom(chatroomCode!!)
        chatroomCode = null
        isJoinRoom = false
        isHost = false
        createChatRoomViewListener()
    }

    fun notifyChatMessage(messageAddedListener: (ChatMessage) -> Unit) {
        dbChat.notifyChatMessage(chatroomCode!!, messageAddedListener)
    }

    fun sendChatMessage(message: String) {
        dbChat.sendChatMessage(chatroomCode!!, message)
    }
}