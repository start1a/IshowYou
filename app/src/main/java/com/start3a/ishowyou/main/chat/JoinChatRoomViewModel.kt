package com.start3a.ishowyou.main.chat

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.start3a.ishowyou.data.ChatRoom
import com.start3a.ishowyou.model.ChatDao

class JoinChatRoomViewModel : ViewModel() {

    var listRoom = MutableLiveData<MutableList<ChatRoom>>().apply { value = mutableListOf() }

    private val dbChat = ChatDao(FirebaseDatabase.getInstance().reference)

    fun loadRoomList() {
        dbChat.requestUserChatRoomList(
            // 처음 리스트를 불러옴
            { rooms ->
                val list = listRoom.value!!
                rooms.forEach {
                    list.add(it)
                }
                listRoom.value = list
            },
            // 새 방이 생성될 때마다
            {
                val list = listRoom.value!!
                listRoom.value!!.add(it)
                listRoom.value = list
            })
    }

    fun requestJoinRoom(roomCode: String, successJoined: (String) -> Unit, failJoined: () -> Unit) {
        dbChat.requestJoinRoom(roomCode, successJoined, failJoined)
    }

    fun closeJoinRoom() {
        dbChat.closeJoinRoom()
    }
}
