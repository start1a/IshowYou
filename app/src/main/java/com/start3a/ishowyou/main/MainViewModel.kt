package com.start3a.ishowyou.main

import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.start3a.ishowyou.data.Content
import com.start3a.ishowyou.model.RdbDao
import com.start3a.ishowyou.room.content.ContentSetting

class MainViewModel : ViewModel() {

    // 채팅방 유무 뷰 전환
    lateinit var createChatRoom: ((String) -> Unit)
    lateinit var initLobbyCurContent: ((Content) -> Unit)

    // Dao
    private var dbYoutube: RdbDao.YoutubeDao
    private var curRoomContent: ContentSetting? = null

    init {
        val db = RdbDao(FirebaseDatabase.getInstance().reference)
        dbYoutube = db.YoutubeDao()
    }


    // 컨텐츠 --------------------------------------
    fun changeContent(content: Content) {
        curRoomContent?.close()

        when (content) {
            Content.YOUTUBE -> curRoomContent = dbYoutube
        }
        initLobbyCurContent(content)
    }
}