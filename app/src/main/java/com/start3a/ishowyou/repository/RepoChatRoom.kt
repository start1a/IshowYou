package com.start3a.ishowyou.repository

import android.content.Context
import androidx.room.Room
import com.google.firebase.database.FirebaseDatabase
import com.start3a.ishowyou.contentapi.PlayStateRequested
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.data.ChatMember
import com.start3a.ishowyou.data.ChatMessage
import com.start3a.ishowyou.data.ChatRoom
import com.start3a.ishowyou.model.RdbDao
import com.start3a.ishowyou.model.RoomData_CurWatchedVideo
import com.start3a.ishowyou.model.RoomDatabase
import java.util.*

class RepoChatRoom(context: Context) {

    private val roomDB =
        Room.databaseBuilder(context, RoomDatabase::class.java, "database-room")
            .allowMainThreadQueries()
            .build()

    private var dbYoutube: RdbDao.YoutubeDao
    private var dbChat: RdbDao.ChatDao

    init {
        val db = RdbDao(FirebaseDatabase.getInstance().reference)
        dbYoutube = db.YoutubeDao()
        dbChat = db.ChatDao()
    }


    // 유튜브 --------------------------------------
    fun setSeekbarChangedListener(changeSeekbar: (Float) -> Unit) {
        dbYoutube.setSeekbarChangedListener(changeSeekbar)
    }

    fun notifyPlayListChanged(
        playlistAdded: (YoutubeSearchData) -> Unit,
        playlistRemoved: (YoutubeSearchData) -> Unit
    ) {
        dbYoutube.notifyPlayListChanged(playlistAdded, playlistRemoved)
    }

    fun notifyPrevVideoPlayList(videoAdded: (YoutubeSearchData) -> Unit) {
        dbYoutube.notifyPrevVideoPlayList(videoAdded)
    }

    fun addVideoToPlaylist(list: List<YoutubeSearchData>) {
        dbYoutube.addVideoToPlaylist(list)
    }

    fun removeVideoPlaylist_Youtube(createdTime: Long) {
        dbYoutube.removeVideoToPlaylist(createdTime)
    }

    fun setNewYoutubeVideoSelected(video: String) {
        dbYoutube.setNewYoutubeVideoSelected(video)
    }

    fun setNewYoutubeVideoPlayed(video: YoutubeSearchData, seekBar: Float) {
        dbYoutube.setNewYoutubeVideoPlayed(video, seekBar)
    }

    fun notifyNewVideoSelected(newVideoPlayed: (String) -> Unit) {
        dbYoutube.notifyNewVideoSelected(newVideoPlayed)
    }

    fun seekbarYoutubeClicked(time: Float) {
        dbYoutube.seekbarYoutubeClicked(time)
    }

    fun setYoutubeVideoSeekbarChanged(seekbar: Float) {
        dbYoutube.setYoutubeVideoSeekbarChanged(seekbar)
    }

    fun requestVideoPlayState(requestPlayState: (PlayStateRequested, Long, Long) -> Unit) {
        dbYoutube.requestVideoPlayState(requestPlayState)
    }

    fun inActiveYoutubeRealtimeListener() {
        dbYoutube.inActiveYoutubeRealtimeListener()
    }

    fun refreshVideoSearchCacaheList() {
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

    fun insertCurWatchedVideo(video: YoutubeSearchData) {
        val watchedVideo = RoomData_CurWatchedVideo(
            video.title,
            video.desc,
            video.channelTitle,
            video.videoId,
            video.thumbnail,
            video.thumbnailSmall,
            video.duration
        )
        roomDB.youtubeDao().insertCurWatchedVideo(watchedVideo)
    }

    fun removeAllCurWatchedVideo() {
        roomDB.youtubeDao().removeAllCurWatchedVideo()
    }

    fun removeAllSearchKeywords() {
        roomDB.youtubeDao().removeAllSearchKeywords()
    }


    // 채팅방 ----------------------------------------
    fun createChatRoom(
        title: String,
        successListener: (String) -> Unit,
        roomInfoChangedListener: (ChatRoom) -> Unit
    ) {
        dbChat.createChatRoom(title, successListener, roomInfoChangedListener)
    }

    fun requestJoinRoom(roomCode: String, successJoined: (String) -> Unit, failJoined: () -> Unit) {
        dbChat.requestJoinRoom(roomCode, successJoined, failJoined)
    }

    fun leaveRoom(host: Boolean) {
        // 방 정보 삭제
        if (!host) dbYoutube.closeRoom()
        dbChat.closeRoom(host)
    }

    fun notifyChatMessage(messageAdded: (ChatMessage) -> Unit) {
        dbChat.notifyChatMessage(messageAdded)
    }

    fun notifyIsRoomDeleted(roomDeleted: () -> Unit) {
        dbChat.notifyIsRoomDeleted(roomDeleted)
    }

    fun notifyChatMember(memberAdded: (ChatMember) -> Unit, memberRemoved: (String) -> Unit) {
        dbChat.notifyChatMember(memberAdded, memberRemoved)

    }

    fun sendChatMessage(message: String) {
        dbChat.sendChatMessage(message)
    }

    fun requestUserChatRoomList(roomListAdded: (MutableList<ChatRoom>) -> Unit) {
        dbChat.requestUserChatRoomList(roomListAdded)
    }

    fun checkPrevRoomJoin(requestJoin: (Boolean) -> Unit, loadingOff: () -> Unit) {
        dbChat.checkPrevRoomJoin(requestJoin, loadingOff)
    }

    fun searchRoomByKeyword(keyword: String, getRooms: (List<ChatRoom>) -> Unit) {
        dbChat.searchRoomByKeyword(keyword, getRooms)
    }
}