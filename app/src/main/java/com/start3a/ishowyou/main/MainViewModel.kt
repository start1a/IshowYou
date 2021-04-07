package com.start3a.ishowyou.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.start3a.ishowyou.data.ChatRoom
import com.start3a.ishowyou.data.FullScreenController
import com.start3a.ishowyou.model.RdbDao

class MainViewModel : ViewModel() {

    var listRoom = MutableLiveData<MutableList<ChatRoom>>().apply { value = mutableListOf() }
    var isRoomJoined = false
    var curQueryKeyword = ""

    // 채팅방 유무 뷰 전환
    lateinit var createChatRoom: ((String) -> Unit)

    // Dao
    private var dbChat: RdbDao.ChatDao

    // 임시 방 생성 제목 저장
    lateinit var titleTemp: String

    init {
        val db = RdbDao(FirebaseDatabase.getInstance().reference)
        dbChat = db.ChatDao()
    }

    fun checkPrevRoomJoin(requestJoin: (String, Boolean) -> Unit, loadingOff: () -> Unit) {
        dbChat.checkPrevRoomJoin(requestJoin, loadingOff)
    }

    fun loadRoomList(loadingOff: (() -> Unit)?) {
        dbChat.requestUserChatRoomList { rooms ->
            val list = listRoom.value!!
            list.clear()

            list.addAll(rooms)
            this.listRoom.value = list
            loadingOff?.invoke()
        }
    }

    fun isQueryAvailable(keyword: String) =
        curQueryKeyword != keyword && keyword.isNotBlank() && keyword.isNotEmpty()

    fun searchRoomByKeyword(keyword: String, messageNoItem: () -> Unit, loadingOff: () -> Unit) {
        dbChat.searchRoomByKeyword(keyword) { rooms ->
            if (rooms.isNotEmpty()) {
                val list = listRoom.value!!

                list.clear()
                list.addAll(rooms)
                listRoom.value = list
            }
            else messageNoItem()

            loadingOff()
        }
    }
}