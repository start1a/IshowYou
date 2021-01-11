package com.start3a.ishowyou.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.start3a.ishowyou.data.ChatMember
import com.start3a.ishowyou.data.ChatMessage
import com.start3a.ishowyou.data.ChatRoom
import com.start3a.ishowyou.data.Content
import com.start3a.ishowyou.main.content.ContentSetting
import com.start3a.ishowyou.model.RdbDao

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
    lateinit var createChatRoomView: (() -> Unit)
    lateinit var initRoomCurContent: ((Content) -> Unit)
    // 액티비티에서 토스트 메시지 관리
    // 프래그먼트에서 뷰를 실행하면서 종료할 시에 발생하는 에러 방지
    lateinit var messageView: (String) -> Unit

    // Dao
    private var dbYoutube: RdbDao.YoutubeDao
    private var dbChat: RdbDao.ChatDao
    private var curRoomContent: ContentSetting? = null

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
        if (!isHost && isJoinRoom)
            dbYoutube.setSeekbarChangedListener(changeSeekbar)
    }

    // 채팅방 ----------------------------------------
    fun createChatRoom(
        title: String,
        successListener: () -> Unit,
        roomInfoChangedListener: (ChatRoom) -> Unit
    ) {
        isHost = true
        isJoinRoom = true
        dbChat.createChatRoom(ChatRoom(title), successListener, roomInfoChangedListener)
        changeContent(Content.YOUTUBE)
    }

    fun leaveRoom() {
        // 방 정보 삭제
        dbChat.closeRoom(isHost)

        // 방 정보 리셋
        isJoinRoom = false
        isHost = false
        listMessage.value?.clear()
        listMember.clear()

        // 방 컨텐츠 비활성화
        curRoomContent?.close()
        curRoomContent = null

        // 방 대기 화면
        createChatRoomView()
    }

    fun initChatRoom() {
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
                messageView("방장이 퇴장했습니다.")
                leaveRoom()
            }
        }
    }

    fun sendChatMessage(message: String) {
        dbChat.sendChatMessage(message)
    }

    fun joinRoom(roomCode: String) {
        isJoinRoom = true
        isHost = false
        dbChat.joinRoom(roomCode)
        changeContent(Content.YOUTUBE)
        createChatRoomView()
    }
}