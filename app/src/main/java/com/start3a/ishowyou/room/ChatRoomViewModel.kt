package com.start3a.ishowyou.room

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.start3a.ishowyou.contentapi.PlayStateRequested
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.data.*
import com.start3a.ishowyou.model.RdbDao
import com.start3a.ishowyou.room.content.CustomPlayerUiController

class ChatRoomViewModel: ViewModel() {

    // 채팅방 정보
    var isHost = false
    // 메세지
    val listMessage: ListLiveData<ChatMessage> by lazy {
        ListLiveData(mutableListOf())
    }
    var isMessageListUpScrolled = false
    // 멤버
    val listMember: ListLiveData<ChatMember> by lazy {
        ListLiveData(mutableListOf())
    }

    // 채팅방 유무 뷰 전환
    lateinit var initRoomCurContent: ((Content) -> Unit)

    // Dao
    private var dbYoutube: RdbDao.YoutubeDao
    private var dbChat: RdbDao.ChatDao
    private var curRoomContent: ContentSetting? = null

    // View
    var isFullScreen = false
    lateinit var mFullScreenController: FullScreenController
    var hideKeyboard: (() -> Unit)? = null
    var contentAvailability: ((Boolean) -> Unit)? = null

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


    // 유튜브 --------------------------------------
    lateinit var customPlayerUiController: CustomPlayerUiController

    val listPlayYoutube: ListLiveData<YoutubeSearchData> by lazy {
        ListLiveData(mutableListOf())
    }
    var curVideoSelected = MutableLiveData<YoutubeSearchData>()
    var isRealtimeUsed = MutableLiveData<Boolean>().apply { value = true }

    // 재생 시간 복원 데이터
    val curVideoPlayed = MutableLiveData<YoutubeSearchData>()
    val curSeekbarPos = MutableLiveData<Float>()

    fun PlayNextVideo(curVideo: YoutubeSearchData, time: Float) {
        var restTime = time
        val list = listPlayYoutube.value!!
        // 현재 영상 위치
        var indexSearched = -1
        for (i in 0 until list.size) {
            if (curVideo.createdTime == list[i].createdTime) {
                indexSearched = (i + 1) % list.size
                // 흐른 시간만큼 다음 영상 이동
                while (restTime >= list[indexSearched].duration) {
                    restTime -= list[indexSearched].duration
                    indexSearched = (indexSearched + 1) % list.size
                }
                curSeekbarPos.value = restTime
                curVideoPlayed.value = list[indexSearched]
                return
            }
        }

        // 해당 영상이 없음
        if (indexSearched == -1 && list.size > 0) {
            curSeekbarPos.value = 0f
            curVideoPlayed.value = list[0]
        }
    }

    fun initContent_Youtube(changeSeekbar: (Float) -> Unit) {
        if (!isHost)
            dbYoutube.setSeekbarChangedListener(changeSeekbar)
    }

    fun initContentEdit_Youtube(playlistAdded: (YoutubeSearchData) -> Unit, playlistRemoved: (YoutubeSearchData) -> Unit) {
        if (!isHost)
            dbYoutube.notifyPlayListChanged(playlistAdded, playlistRemoved)
    }

    fun notifyPrevVideoPlayList() {
        if (isHost)
            dbYoutube.notifyPrevVideoPlayList { listPlayYoutube.add(it) }
    }

    fun addVideoToPlaylist_Youtube(list: List<YoutubeSearchData>) {
        listPlayYoutube.addAll(list)
        dbYoutube.addVideoToPlaylist(list)
    }

    fun removeVideoPlaylist_Youtube(pos: Int) {
        val createdTime = listPlayYoutube.value!![pos].createdTime
        dbYoutube.removeVideoToPlaylist(createdTime)
        listPlayYoutube.removeAt(pos)
    }

    fun setNewYoutubeVideoSelected(video: String) {
        if (isControlMember()) dbYoutube.setNewYoutubeVideoSelected(video)
    }

    fun setNewYoutubeVideoPlayed(video: YoutubeSearchData, seekBar: Float) {
        if (isControlMember()) dbYoutube.setNewYoutubeVideoPlayed(video, seekBar)
    }

    fun notifyNewVideoSelected(newVideoPlayed: (String) -> Unit) {
        if (!isHost) dbYoutube.notifyNewVideoSelected(newVideoPlayed)
    }

    fun seekbarYoutubeClicked(time: Float) {
        if (isControlMember()) dbYoutube.seekbarYoutubeClicked(time)
    }

    fun setYoutubeVideoSeekbarChanged(seekbar: Float) {
        if (isControlMember()) dbYoutube.setYoutubeVideoSeekbarChanged(seekbar)
    }

    fun requestVideoPlayState(requestPlayState: (PlayStateRequested, Long, Long) -> Unit) {
        dbYoutube.requestVideoPlayState(requestPlayState)
    }

    fun inActiveYoutubeRealtimeListener() {
        if (!isHost) dbYoutube.inActiveYoutubeRealtimeListener()
    }

    fun retriveVideoById(videoId: String): YoutubeSearchData? {
        val list = listPlayYoutube.value!!

        for (i in 0 until list.size) {
            if (list[i].videoId == videoId) {
                return list[i]
            }
        }

        return null
    }

    private fun isControlMember() = isHost && isRealtimeUsed.value!!

    // 채팅방 ----------------------------------------
    fun createChatRoom(
        title: String,
        successListener: (String) -> Unit,
        roomInfoChangedListener: (ChatRoom) -> Unit
    ) {
        isHost = true
        dbChat.createChatRoom(ChatRoom(title, "Youtube"), successListener, roomInfoChangedListener)
    }

    fun requestJoinRoom(roomCode: String, isHostJoinRoom: Boolean, successJoined: (String) -> Unit, failJoined: () -> Unit) {
        dbChat.requestJoinRoom(roomCode, isHostJoinRoom, successJoined, failJoined)
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

        if (!isHost) {
            dbChat.notifyIsRoomDeleted {
                leaveRoom()
                roomDeleted()
            }
        }
    }

    fun initMemberList() {
        dbChat.notifyChatMember({
            // 멤버 추가
            listMember.add(it)
        },{
            // 멤버 삭제
            var removeIndex = -1
            for (i in 0 until listMember.value!!.size) {
                if (listMember.value!![i].userName == it) {
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
}