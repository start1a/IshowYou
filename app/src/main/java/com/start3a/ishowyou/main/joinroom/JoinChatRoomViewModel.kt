package com.start3a.ishowyou.main.joinroom

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.start3a.ishowyou.data.ChatRoom
import com.start3a.ishowyou.model.RdbDao

class JoinChatRoomViewModel : ViewModel() {

    var listShow = MutableLiveData<MutableList<ChatRoom>>().apply { value = mutableListOf() }

    private val dbChat = RdbDao(FirebaseDatabase.getInstance().reference).ChatDao()

    fun loadRoomList() {
        dbChat.requestUserChatRoomList { rooms ->
            val list = listShow.value!!
            list.clear()

            rooms.forEach {
                list.add(it)
            }
            this.listShow.value = list
        }
    }
}