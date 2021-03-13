package com.start3a.ishowyou.model

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.start3a.ishowyou.contentapi.PlayStateRequested
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.data.*
import java.util.*

class RdbDao(private val db: DatabaseReference) {

    companion object {
        private var roomCode: String? = null
    }

    inner class YoutubeDao : ContentSetting {

        private val TAG = "YoutubeDao"
        private var seekbarChangedListener: ChildEventListener? = null
        private var newVideoSelectedListener: ChildEventListener? = null
        private var playlistChangedListener: ChildEventListener? = null

        private val PATH_REALTIME_SEEKBAR
            get() = "content/$roomCode/youtube/RealtimeListenPlayState/seekbar"
        private val PATH_REALTIME_NEW_VIDEO
            get() = "content/$roomCode/youtube/RealtimeListenPlayState/video"
        private val PATH_CURRENT_PLAY_STATE
            get() = "content/$roomCode/youtube/CurrentPlayState"
        private val PATH_PLAY_LIST
            get() = "content/$roomCode/youtube/playlist"

        fun seekbarYoutubeClicked(time: Float) {
            db.child("$PATH_REALTIME_SEEKBAR/seekbar").setValue(time)
        }

        fun setSeekbarChangedListener(changeSeekbar: (Float) -> Unit) {
            if (seekbarChangedListener != null) return

            seekbarChangedListener =
                db.child(PATH_REALTIME_SEEKBAR).addChildEventListener(object : ChildEventListener {
                        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}

                        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                            snapshot.getValue<Float>()?.let { time ->
                                changeSeekbar(time)
                            }
                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {}
                        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                        override fun onCancelled(error: DatabaseError) {
                            Log.d(TAG, "notifying seekbar change is Cancelled.\n$error")
                        }
                    })
        }

        override fun close() {
            seekbarChangedListener?.let { db.removeEventListener(it) }
            playlistChangedListener?.let { db.removeEventListener(it) }
            newVideoSelectedListener?.let { db.removeEventListener(it) }
        }

        fun addVideoToPlaylist(list: List<YoutubeSearchData>) {
            var alpha = 0
            list.forEach { video ->
                val uploadTime = video.createdTime + alpha++
                video.createdTime = uploadTime
                db.child("$PATH_PLAY_LIST/$uploadTime").setValue(video)
            }
        }

        fun removeVideoToPlaylist(createdTime: Long) {
            db.child("$PATH_PLAY_LIST/$createdTime").removeValue()
        }

        fun notifyPlayListChanged(playlistAdded: (YoutubeSearchData) -> Unit, playlistRemoved: (YoutubeSearchData) -> Unit) {
            if (playlistChangedListener != null) return

            playlistChangedListener =
                db.child(PATH_PLAY_LIST).addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        snapshot.getValue<YoutubeSearchData>()?.let {
                            playlistAdded(it)
                        }
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        snapshot.getValue<YoutubeSearchData>()?.let {
                            playlistRemoved(it)
                        }
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "Changing Play-list is Cancelled.\n$error")
                    }

                })
        }

        // 방에 재접속한 방장이 기존에 플레이 리스트를 불러옴
        fun notifyPrevVideoPlayList(playlistAdded: (YoutubeSearchData) -> Unit) {
            db.child(PATH_PLAY_LIST).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        playlistAdded(it.getValue<YoutubeSearchData>()!!)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "notifying previous play list is Cancelled.\n$error")
                }

            })
        }

        fun setNewYoutubeVideoSelected(videoId: String) {
            db.child("$PATH_REALTIME_NEW_VIDEO/video").setValue(videoId)
        }

        fun notifyNewVideoSelected(newVideoPlayed: (String) -> Unit) {
            if (newVideoSelectedListener != null) return

            newVideoSelectedListener =
            db.child(PATH_REALTIME_NEW_VIDEO).addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot.getValue<String>()?.let { videoId ->
                        newVideoPlayed(videoId)
                    }
                }
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "Notifying new video selection is Cancelled.\n$error")
                }

            })

        }

        fun setNewYoutubeVideoPlayed(video: YoutubeSearchData, seekBar: Float) {
            val curPlayState = PlayStateRequested(video, seekBar)
            val playState = hashMapOf<String, Any>().apply {
                put("$PATH_CURRENT_PLAY_STATE/curVideo", curPlayState)
            }
            db.updateChildren(playState)
        }

        fun setYoutubeVideoSeekbarChanged(seekbar: Float) {
            val changeSeekbar = hashMapOf<String, Any>().apply {
                put("$PATH_CURRENT_PLAY_STATE/curVideo/seekbar", seekbar)
                put("$PATH_CURRENT_PLAY_STATE/timeRecorded", ServerValue.TIMESTAMP)
            }
            db.updateChildren(changeSeekbar)
        }

        fun requestVideoPlayState(requestPlayState: (PlayStateRequested, Long, Long) -> Unit) {
            db.child(PATH_CURRENT_PLAY_STATE)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val video = snapshot.child("curVideo/curVideo").getValue<YoutubeSearchData>()!!
                        val seekBar = snapshot.child("curVideo/seekbar").getValue<Float>()!!
                        val videoInfo = PlayStateRequested(video, seekBar)
                        var videoInfoSaveTime = snapshot.child("timeRecorded").getValue<Long>()?:0L

                        // 현재 서버 시간 가져오기
                        val serverTimeRef = db.child("ServerTime")
                        serverTimeRef.setValue(ServerValue.TIMESTAMP).addOnSuccessListener {
                            serverTimeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    snapshot.getValue<Long>()?.let { serverTime ->
                                        if (videoInfoSaveTime == 0L)
                                            videoInfoSaveTime = serverTime
                                        requestPlayState(videoInfo, serverTime, videoInfoSaveTime)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.d(TAG, "getting serverTime is Cancelled.\n$error")
                                }

                            })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "Request video play state is Cancelled.\n$error")
                }

            })
        }

        fun inActiveYoutubeRealtimeListener() {
            seekbarChangedListener?.let {
                db.child(PATH_REALTIME_SEEKBAR).removeEventListener(it)
                seekbarChangedListener = null
            }
            newVideoSelectedListener?.let {
                db.child(PATH_REALTIME_NEW_VIDEO).removeEventListener(it)
                newVideoSelectedListener = null
            }
        }
    }


    inner class ChatDao {

        private val TAG = "ChatDao"

        private var roomInfoChildChangedListener: ValueEventListener? = null
        private var messageNotifyListener: ChildEventListener? = null
        private var memberNotifyListener: ChildEventListener? = null
        private var hostDeleteRoomNotifyListener: ValueEventListener? = null

        fun createChatRoom(
            chatRoom: ChatRoom,
            successListener: (String) -> Unit,
            roomInfoChangedListener: (ChatRoom) -> Unit
        ) {
            roomCode = Date().time.toString()

            val roomRef = db.child("chat/$roomCode")

            roomRef.setValue(chatRoom)
                .addOnSuccessListener {
                    // 방 정보 리스너 저장
                    roomInfoChildChangedListener =
                        roomRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val room = snapshot.getValue<ChatRoom>()!!
                                roomInfoChangedListener(room)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d(TAG, "Changing Room Info is Cancelled.\n$error")
                            }
                        })

                    successListener(chatRoom.contentName)

                    // 방 멤버로 추가
                    db.child("member/$roomCode/${CurUser.userName}")
                        .setValue(ChatMember(CurUser.userName, true))
                    // 사용자 방 접속 기록 갱신
                    setUserRoomRecord(true)
                }
                .addOnFailureListener {
                    Log.d(TAG, "Creating ChatRoom is Failed\n$it")
                }
        }

        fun closeRoom(isHost: Boolean) {
            // 방 정보 제거
            if (isHost) {
                db.child("chat/$roomCode").removeValue()
                db.child("message/$roomCode").removeValue()
                db.child("member/$roomCode").removeValue()
                db.child("content/$roomCode").removeValue()
            } else {
                db.child("member/$roomCode/${CurUser.userName}").removeValue()
                removeListener(hostDeleteRoomNotifyListener, "member/$roomCode")
                hostDeleteRoomNotifyListener = null
            }

            // 방 리스너 제거
            removeListener(roomInfoChildChangedListener, "chat/$roomCode")
            removeListener(messageNotifyListener, "message/$roomCode")
            removeListener(memberNotifyListener, "member/$roomCode")
            roomInfoChildChangedListener = null
            messageNotifyListener = null
            memberNotifyListener = null

            // 현재 사용자의 방 접속 기록 제거
            db.child("user/${CurUser.userName}").removeValue()

            roomCode = null
        }

        fun notifyChatMessage(messageAdded: (ChatMessage) -> Unit) {
            if (messageNotifyListener != null) return
            val joinRoomTime = Date().time.toString()

            messageNotifyListener =
                // 새 멤버는 입장 이후 시간의 메세지만 추가됨
                db.child("message/$roomCode").orderByKey().startAt(joinRoomTime)
                    .addChildEventListener(object : ChildEventListener {

                        override fun onChildAdded(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            snapshot.getValue<ChatMessage>()?.let {
                                messageAdded(it)
                            }
                        }

                        override fun onChildChanged(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {}
                        override fun onChildMoved(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d(TAG, "Notifying Chat Message is Cancelled.\n$error")
                        }
                    })
        }

        fun notifyChatMember(memberAdded: (ChatMember) -> Unit, memberRemoved: (String) -> Unit) {
            if (memberNotifyListener != null) return

            memberNotifyListener =
                db.child("member/$roomCode").addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        snapshot.getValue<ChatMember>()?.let {
                            memberAdded(it)
                        }
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        snapshot.getValue(ChatMember::class.java)?.let {
                            memberRemoved(it.userName)
                        }
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "Notifying Chat Member is Cancelled.\n$error")
                    }

                })
        }

        fun sendChatMessage(message: String) {
            val time = Date().time
            db.child("message/$roomCode/$time").setValue(
                ChatMessage(CurUser.userName, message, time)
            )
        }

        fun requestUserChatRoomList(firstRequestRoomListSucceed: (MutableList<ChatRoom>) -> Unit) {
            // 최초 리스트 출력
            db.child("chat").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val rooms = mutableListOf<ChatRoom>()
                    snapshot.children.forEach { ref ->
                        ref.getValue<ChatRoom>()?.let {
                            rooms.add(it)
                        }
                    }
                    firstRequestRoomListSucceed(rooms)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "Initializing Chat Room List is Cancelled.\n$error")
                }
            })
        }

        fun requestJoinRoom(
            requestedRoomCode: String,
            isHostJoinRoom: Boolean,
            successJoined: (String) -> Unit,
            failJoined: () -> Unit
        ) {
            // 방 입장
            db.child("chat/$requestedRoomCode")
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            snapshot.getValue<ChatRoom>()?.let {
                                roomCode = requestedRoomCode
                                successJoined(it.contentName)
                                setUserRoomRecord(isHostJoinRoom)
                            }
                        } else failJoined()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "Request Join Room is Cancelled.\n$error")
                    }
                })

            // 방 멤버 저장
            db.child("member/$requestedRoomCode/${CurUser.userName}")
                .setValue(ChatMember(CurUser.userName, isHostJoinRoom))
        }

        // 방 제거 여부
        // 방장이 방을 나가면 삭제됨
        fun notifyIsRoomDeleted(roomDeleted: () -> Unit) {
            if (hostDeleteRoomNotifyListener != null) return

            hostDeleteRoomNotifyListener =
                db.child("chat/$roomCode").addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists())
                            roomDeleted()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "Notifying Chat Room List is Cancelled.\n$error")
                    }
                })
        }

        private fun removeListener(listener: ChildEventListener?, path: String) {
            db.child(path).let { dbr ->
                listener?.let { dbr.removeEventListener(it) }
            }
        }

        private fun removeListener(listener: ValueEventListener?, path: String) {
            db.child(path).let { dbr ->
                listener?.let { dbr.removeEventListener(it) }
            }
        }

        fun checkPrevRoomJoin(requestJoin: (String, Boolean) -> Unit, loadingOff: () -> Unit) {
            db.child("user/${CurUser.userName}")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        loadingOff()
                        if (snapshot.exists()) {
                            val userRoomCode = snapshot.child("room").getValue<String>()!!
                            val isHost = snapshot.child("isHost").getValue<Boolean>()!!
                            requestJoin(userRoomCode, isHost)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "Checking previous room join is Cancelled.\n$error")
                        loadingOff()
                    }
                })
        }

        // 사용자 방 접속 기록 업데이트
        private fun setUserRoomRecord(isHost: Boolean) {
            db.child("user/${CurUser.userName}/room").setValue(roomCode)
            db.child("user/${CurUser.userName}/isHost").setValue(isHost)
        }
    }
}