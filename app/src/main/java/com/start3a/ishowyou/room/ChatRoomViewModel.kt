package com.start3a.ishowyou.room

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.start3a.ishowyou.contentapi.PlayStateRequested
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.data.*
import com.start3a.ishowyou.repository.RepoChatRoom
import com.start3a.ishowyou.room.content.CustomPlayerUiController

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

    // 로비
    var isStopSavingCurWatchedVideo: Boolean =
        MyApplication.prefs.getString("isStopSavingCurWatchedVideo", "false").toBoolean()
    var isStopSearchKeywords: Boolean =
        MyApplication.prefs.getString("isStopSearchKeywords", "false").toBoolean()

    // Dao
    private lateinit var repo: RepoChatRoom


    // View attr ----------------
    var isFullScreen = false
    var openFullScreenChatView: ((isVisible: Boolean) -> Unit)? = null

    var hideKeyboard: (() -> Unit)? = null
    var contentAvailability: ((Boolean) -> Unit)? = null

    lateinit var showDraggablePanel: (isVisible: Boolean) -> Unit
    lateinit var setRoomAttr:((isOpen: Boolean, isHost: Boolean) -> Unit)
    lateinit var notifyRoomDeletedListener: () -> Unit

    var isActivitySizeMeasured = false
    var activity_width = 0
    var activity_height = 0

    var curQueryKeyword = ""

    // 임시 방 생성 제목 저장
    lateinit var titleTemp: String

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

    fun init(context: Context) {
        repo = RepoChatRoom(context)
    }

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

    fun setSeekbarChangedListener(changeSeekbar: (Float) -> Unit) {
        if (!isHost)
            repo.setSeekbarChangedListener(changeSeekbar)
    }

    fun notifyPlayListChanged(playlistAdded: (YoutubeSearchData) -> Unit, playlistRemoved: (YoutubeSearchData) -> Unit) {
        if (!isHost)
            repo.notifyPlayListChanged(playlistAdded, playlistRemoved)
    }

    fun notifyPrevVideoPlayList() {
        if (isHost)
            repo.notifyPrevVideoPlayList { listPlayYoutube.add(it) }
    }

    fun addVideoToPlaylist_Youtube(list: List<YoutubeSearchData>) {
        listPlayYoutube.addAll(list)
        repo.addVideoToPlaylist(list)
    }

    fun removeVideoPlaylist_Youtube(pos: Int) {
        val createdTime = listPlayYoutube.value!![pos].createdTime
        repo.removeVideoPlaylist_Youtube(createdTime)
        listPlayYoutube.removeAt(pos)
    }

    fun setNewYoutubeVideoSelected(video: String) {
        if (isControlMember()) repo.setNewYoutubeVideoSelected(video)
    }

    fun setNewYoutubeVideoPlayed(video: YoutubeSearchData, seekBar: Float) {
        if (isControlMember()) repo.setNewYoutubeVideoPlayed(video, seekBar)
    }

    fun notifyNewVideoSelected(newVideoPlayed: (String) -> Unit) {
        if (!isHost) repo.notifyNewVideoSelected(newVideoPlayed)
    }

    fun seekbarYoutubeClicked(time: Float) {
        if (isControlMember()) repo.seekbarYoutubeClicked(time)
    }

    fun setYoutubeVideoSeekbarChanged(seekbar: Float) {
        if (isControlMember()) repo.setYoutubeVideoSeekbarChanged(seekbar)
    }

    fun requestVideoPlayState(requestPlayState: (PlayStateRequested, Long, Long) -> Unit) {
        repo.requestVideoPlayState(requestPlayState)
    }

    fun inActiveYoutubeRealtimeListener() {
        if (!isHost) repo.inActiveYoutubeRealtimeListener()
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

    fun refreshVideoSearchCacaheList() {
        repo.refreshVideoSearchCacaheList()
    }

    fun removeAllCurWatchedVideo() {
        repo.removeAllCurWatchedVideo()
    }

    fun removeAllSearchKeywords() {
        repo.removeAllSearchKeywords()
    }

    fun insertCurWatchedVideo(video: YoutubeSearchData) {
        if (!isStopSavingCurWatchedVideo) repo.insertCurWatchedVideo(video)
    }

    private fun isControlMember() = isHost && isRealtimeUsed.value!!



    // 채팅방 ----------------------------------------
    fun createChatRoom(
        title: String,
        successListener: (String) -> Unit,
        roomInfoChangedListener: (ChatRoom) -> Unit
    ) {
        isHost = true
        repo.createChatRoom(title, successListener, roomInfoChangedListener)
    }

    fun requestJoinRoom(roomCode: String, successJoined: (String) -> Unit, failJoined: () -> Unit) {
        repo.requestJoinRoom(roomCode, successJoined, failJoined)
    }

    fun leaveRoom(host: Boolean) {
        // 방 정보 삭제
        repo.leaveRoom(host)

        listMember.value?.clear()
        listPlayYoutube.value?.clear()
        listMessage.value?.clear()

        curSeekbarPos.value = -1f
        curVideoPlayed.value = YoutubeSearchData()
        curVideoSelected.value = YoutubeSearchData()
    }

    fun initChatRoom(newMessageNotifiedtoTab: () -> Unit) {
        repo.notifyChatMessage {
            // 메시지 감지
            val list = listMessage.value!!
            list.add(it)
            listMessage.value = list

            if (CurUser.userName != it.userName && !isFullScreen)
                newMessageNotifiedtoTab()
        }
    }

    fun notifyDeleteRoom() {
        if (!isHost) {
            repo.notifyIsRoomDeleted {
                leaveRoom(false)
                notifyRoomDeletedListener()
                setRoomAttr(false, false)
            }
        }
    }

    fun initMemberList() {
        repo.notifyChatMember({
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
        repo.sendChatMessage(message)
    }

    fun loadRoomList(loadingOff: (() -> Unit)?) {
        repo.requestUserChatRoomList { rooms ->
            val list = listRoom.value!!
            list.clear()

            list.addAll(rooms)
            this.listRoom.value = list
            loadingOff?.invoke()
        }
    }

    fun checkPrevRoomJoin(requestJoin: (Boolean) -> Unit, loadingOff: () -> Unit) {
        repo.checkPrevRoomJoin(requestJoin, loadingOff)
    }

    fun isQueryAvailable(keyword: String) =
        curQueryKeyword != keyword && keyword.isNotBlank() && keyword.isNotEmpty()

    fun searchRoomByKeyword(keyword: String, messageNoItem: () -> Unit, loadingOff: () -> Unit) {
        repo.searchRoomByKeyword(keyword) { rooms ->
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