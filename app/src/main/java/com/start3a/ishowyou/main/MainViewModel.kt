package com.start3a.ishowyou.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.start3a.ishowyou.data.ChatMember
import com.start3a.ishowyou.data.ChatMessage
import com.start3a.ishowyou.data.ChatRoom
import com.start3a.ishowyou.model.ChatDao
import com.start3a.ishowyou.model.YoutubeDao

class MainViewModel : ViewModel() {

    // 채팅방 정보
    var isHost = false
    var isJoinRoom = false
    lateinit var openChatRoomMenu: () -> Unit

    // 메세지
    val listMessage = MutableLiveData<MutableList<ChatMessage>>().apply { value = mutableListOf() }
    // 멤버
    val listMember = mutableListOf<ChatMember>()

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
        isHost = true
        dbChat.createChatRoom(ChatRoom(title), successListener, roomInfoChangedListener)
    }

    fun leaveRoom() {
        dbChat.leaveRoom(isHost)
        isJoinRoom = false
        isHost = false
        createChatRoomViewListener()
    }

    fun notifyChatInfo() {
        dbChat.notifyChatMessage {
            // 메시지 감지
            val list = listMessage.value!!
            list.add(it)
            listMessage.value = list
        }
        dbChat.notifyChatMember({
            // 멤버 추가
            listMember.add(it)
        },{
            // 멤버 삭제
            var removeIndex = -1
            for (i in 0 until listMember.size) {
                if (listMember[i].userName == it) {
                    removeIndex = i
                    break
                }
            }
            if (removeIndex != -1)
                listMember.removeAt(removeIndex)
        })
    }

    fun sendChatMessage(message: String) {
        dbChat.sendChatMessage(message)
    }

    fun joinRoom(roomCode: String) {
        isJoinRoom = true
        isHost = false
        dbChat.joinRoom(roomCode)
        createChatRoomViewListener()
    }
}