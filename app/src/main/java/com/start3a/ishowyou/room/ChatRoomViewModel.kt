package com.start3a.ishowyou.room

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.google.firebase.database.FirebaseDatabase
import com.start3a.ishowyou.contentapi.PlayStateRequested
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.data.*
import com.start3a.ishowyou.model.RdbDao
import com.start3a.ishowyou.model.RoomDatabase
import com.start3a.ishowyou.room.content.CustomPlayerUiController
import java.util.*

class ChatRoomViewModel: ViewModel() {

    // 채팅방 정보 ------------
    var isHost = false
    var isJoinRoom = MutableLiveData<Boolean>().apply { value = false }
    var listRoom = MutableLiveData<MutableList<ChatRoom>>().apply { value = mutableListOf() }

    // 메세지
    val listMessage: ListLiveData<ChatMessage> by lazy {
        ListLiveData(mutableListOf())
    }
    var isMessageListUpScrolled = false
    // 멤버
    val listMember: ListLiveData<ChatMember> by lazy {
        ListLiveData(mutableListOf())
    }

    // Dao
    private var dbYoutube: RdbDao.YoutubeDao
    private var dbChat: RdbDao.ChatDao

    // View attr ----------------
    var isFullScreen = false
    lateinit var mFullScreenController: FullScreenController
    var openFullScreenChatView: ((isVisible: Boolean) -> Unit)? = null

    var hideKeyboard: (() -> Unit)? = null
    var contentAvailability: ((Boolean) -> Unit)? = null

    lateinit var showDraggablePanel: (isVisible: Boolean) -> Unit
    lateinit var setRoomAttr:((isOpen: Boolean, isHost: Boolean) -> Unit)

    var isActivitySizeMeasured = false
    var activity_width = 0
    var activity_height = 0

    var curQueryKeyword = ""

    // 임시 방 생성 제목 저장
    lateinit var titleTemp: String

    init {
        val db = RdbDao(FirebaseDatabase.getInstance().reference)
        dbYoutube = db.YoutubeDao()
        dbChat = db.ChatDao()
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

    fun refreshVideoSearchCacaheList(context: Context) {
        val roomDB = Room.databaseBuilder(context, RoomDatabase::class.java, "database-room")
            .allowMainThreadQueries()
            .build()

        val videos = roomDB.youtubeDao().getAllCacheVideos()
        if (videos.isNotEmpty()) {
            val curTime = Date().time
            val millisecOfDay = 24 * 3600 * 1000
            val keyList = mutableListOf<String>()

            videos.forEach {
                if (keyList.size == 0 || keyList[keyList.lastIndex] != it.keyword) {
                    if (curTime - it.timeCreated >= millisecOfDay) {
                        keyList.add(it.keyword)
                    }
                }
            }

            keyList.forEach { roomDB.youtubeDao().deleteCacheVideo(it) }
        }
    }

    private fun isControlMember() = isHost && isRealtimeUsed.value!!

    // 채팅방 ----------------------------------------
    fun createChatRoom(
        title: String,
        successListener: (String) -> Unit,
        roomInfoChangedListener: (ChatRoom) -> Unit
    ) {
        isHost = true
        dbChat.createChatRoom(title, successListener, roomInfoChangedListener)
    }

    fun requestJoinRoom(roomCode: String, successJoined: (String) -> Unit, failJoined: () -> Unit) {
        dbChat.requestJoinRoom(roomCode, successJoined, failJoined)
    }

    fun leaveRoom(host: Boolean) {
        // 방 정보 삭제
        if (!isHost) dbYoutube.closeRoom()
        dbChat.closeRoom(host)

        listMember.value?.clear()
        listPlayYoutube.value?.clear()
        listMessage.value?.clear()

        curSeekbarPos.value = -1f
        curVideoPlayed.value = YoutubeSearchData()
        curVideoSelected.value = YoutubeSearchData()
        isRealtimeUsed.value = false
    }

    fun initChatRoom() {
        dbChat.notifyChatMessage {
            // 메시지 감지
            val list = listMessage.value!!
            list.add(it)
            listMessage.value = list
        }
    }

    fun notifyDeleteRoom(roomDeleted: () -> Unit) {
        if (!isHost) {
            dbChat.notifyIsRoomDeleted {
                leaveRoom(false)
                roomDeleted()
                setRoomAttr(false, false)
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

    fun loadRoomList(loadingOff: (() -> Unit)?) {
        dbChat.requestUserChatRoomList { rooms ->
            val list = listRoom.value!!
            list.clear()

            list.addAll(rooms)
            this.listRoom.value = list
            loadingOff?.invoke()
        }
    }

    fun checkPrevRoomJoin(requestJoin: (Boolean) -> Unit, loadingOff: () -> Unit) {
        dbChat.checkPrevRoomJoin(requestJoin, loadingOff)
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