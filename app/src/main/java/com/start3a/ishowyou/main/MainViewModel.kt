package com.start3a.ishowyou.main

import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.start3a.ishowyou.data.Content
import com.start3a.ishowyou.data.FullScreenController
import com.start3a.ishowyou.model.RdbDao
import com.start3a.ishowyou.data.ContentSetting

class MainViewModel : ViewModel() {

    // 채팅방 유무 뷰 전환
    lateinit var createChatRoom: ((String) -> Unit)
    lateinit var initLobbyCurContent: ((Content) -> Unit)

    // Dao
    private var dbYoutube: RdbDao.YoutubeDao
    private var dbChat: RdbDao.ChatDao
    private var curRoomContent: ContentSetting? = null

    // View
    var isFullScreen = false
    lateinit var mFullScreenController: FullScreenController
    var timeCurVideo = -1f
    // 임시 방 생성 제목 저장
    lateinit var titleTemp: String

    init {
        val db = RdbDao(FirebaseDatabase.getInstance().reference)
        dbYoutube = db.YoutubeDao()
        dbChat = db.ChatDao()
    }

    // 컨텐츠 --------------------------------------
    fun changeContent(content: Content) {
        curRoomContent?.close()

        when (content) {
            Content.YOUTUBE -> curRoomContent = dbYoutube
        }
        initLobbyCurContent(content)
    }

    fun checkPrevRoomJoin(requestJoin: (String, Boolean) -> Unit, loadingOff: () -> Unit) {
        dbChat.checkPrevRoomJoin(requestJoin, loadingOff)
    }
}