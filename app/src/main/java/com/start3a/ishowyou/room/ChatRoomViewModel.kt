package com.start3a.ishowyou.room

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.start3a.ishowyou.data.*
import com.start3a.ishowyou.model.RdbDao
import com.start3a.ishowyou.data.ContentSetting

class ChatRoomViewModel: ViewModel() {

    // 채팅방 정보
    var isHost = false
    lateinit var openChatRoomMenu: () -> Unit
    // 메세지
    val listMessage = MutableLiveData<MutableList<ChatMessage>>().apply { value = mutableListOf() }
    // 멤버
    val listMember = mutableListOf<ChatMember>()

    // 채팅방 유무 뷰 전환
    lateinit var initRoomCurContent: ((Content) -> Unit)

    // Dao
    private var dbYoutube: RdbDao.YoutubeDao
    private var dbChat: RdbDao.ChatDao
    private var curRoomContent: ContentSetting? = null

    // View
    var isFullScreen = false
    lateinit var mFullScreenController: FullScreenController

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
        initRoomCurContent(content)
    }

    // 유튜브
    fun seekbarYoutubeClicked(time: Float) {
        if (isHost)
            dbYoutube.seekbarYoutubeClicked(time.toDouble())
    }

    fun initContent_Youtube(changeSeekbar: (Float) -> Unit) {
        if (!isHost)
            dbYoutube.setSeekbarChangedListener(changeSeekbar)
    }


    // 채팅방 ----------------------------------------
    fun createChatRoom(
        title: String,
        successListener: () -> Unit,
        roomInfoChangedListener: (ChatRoom) -> Unit
    ) {
        isHost = true
        dbChat.createChatRoom(ChatRoom(title), successListener, roomInfoChangedListener)
        changeContent(Content.YOUTUBE)
    }

    fun requestJoinRoom(roomCode: String, successJoined: () -> Unit, failJoined: () -> Unit) {
        dbChat.requestJoinRoom(roomCode, successJoined, failJoined)
    }

    fun leaveRoom() {
        // 방 정보 삭제
        dbChat.closeRoom(isHost)
    }

    fun initChatRoom(roomDeleted: () -> Unit) {
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

        if (!isHost) {
            dbChat.notifyIsRoomDeleted {
                leaveRoom()
                roomDeleted()
            }
        }
    }

    fun sendChatMessage(message: String) {
        dbChat.sendChatMessage(message)
    }


    // 뷰 설정 -------------------------
    fun enterFullScreen(weightContent: Float, weightTalk: Float) {
        mFullScreenController.enterFullScreenView(weightContent, weightTalk)
    }

    fun exitFullScreen() {
        mFullScreenController.exitFullScreenView()
    }
}