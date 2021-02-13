package com.start3a.ishowyou.model

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.data.*
import java.util.*

class RdbDao(private val db: DatabaseReference) {

    companion object {
        private var roomCode: String? = null
    }

    inner class YoutubeDao : ContentSetting {

        private val TAG = "YoutubeDao"
        private var seekbarChangedListener: ValueEventListener? = null
        private var newVideoSelectedListener: ValueEventListener? = null
        private var playlistChangedListener: ChildEventListener? = null

        fun seekbarYoutubeClicked(time: Double) {
            db.child("content/$roomCode/youtube/seekbar").setValue(time)
        }

        fun setSeekbarChangedListener(changeSeekbar: (Float) -> Unit) {
            if (seekbarChangedListener != null) return

            seekbarChangedListener =
                db.child("content/$roomCode/youtube/seekbar")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            snapshot.getValue<Double>()?.let { time ->
                                changeSeekbar(time.toFloat())
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d(TAG, "Youtube Seek is Cancelled.\n$error")
                        }
                    })
        }

        override fun close() {
            seekbarChangedListener?.let { db.removeEventListener(it) }
            playlistChangedListener?.let { db.removeEventListener(it) }
            newVideoSelectedListener?.let { db.removeEventListener(it) }
        }

        fun addVideoToPlaylist(list: List<YoutubeSearchData>) {
            list.forEach { video ->
                db.child("content/$roomCode/youtube/playlist/${video.videoId}").setValue(video)
            }
        }

        fun removeVideoToPlaylist(video: YoutubeSearchData) {
            db.child("content/$roomCode/youtube/playlist/${video.videoId}").removeValue()
        }

        fun notifyPlayListChanged(playlistAdded: (YoutubeSearchData) -> Unit, playlistRemoved: (YoutubeSearchData) -> Unit) {
            if (playlistChangedListener != null) return

            playlistChangedListener =
                db.child("content/$roomCode/youtube/playlist").addChildEventListener(object : ChildEventListener {
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

        fun setNewYoutubeVideoPlayed(video: YoutubeSearchData) {
            db.child("content/$roomCode/youtube/curvideo").setValue(video)
        }

        fun notifyNewVideoSelected(newVideoPlayed: (YoutubeSearchData) -> Unit) {
        if (newVideoSelectedListener != null) return

            newVideoSelectedListener =
            db.child("content/$roomCode/youtube/curvideo").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<YoutubeSearchData>()?.let { newVideo ->
                        newVideoPlayed(newVideo)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "Notifying new video selection is Cancelled.\n$error")
                }

            })
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
            successListener: () -> Unit,
            roomInfoChangedListener: (ChatRoom) -> Unit
        ) {
            Date().time.let {
                roomCode = it.toString()
                chatRoom.timeCreated = it
            }

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

                    successListener()
                }
                .addOnFailureListener {
                    Log.d(TAG, "Creating ChatRoom is Failed\n$it")
                }

            db.child("member/$roomCode/${CurUser.userName}")
                .setValue(ChatMember(CurUser.userName, true))
        }

        fun closeRoom(isHost: Boolean) {
            if (isHost) {
                db.child("chat/$roomCode").removeValue()
                db.child("message/$roomCode").removeValue()
                db.child("member/$roomCode").removeValue()
            } else {
                db.child("member/$roomCode/${CurUser.userName}").removeValue()
                removeListener(hostDeleteRoomNotifyListener, "member/$roomCode")
                hostDeleteRoomNotifyListener = null
            }

            removeListener(roomInfoChildChangedListener, "chat/$roomCode")
            removeListener(messageNotifyListener, "message/$roomCode")
            removeListener(memberNotifyListener, "member/$roomCode")
            roomInfoChildChangedListener = null
            messageNotifyListener = null
            memberNotifyListener = null

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
            successJoined: () -> Unit,
            failJoined: () -> Unit
        ) {
            // 방 입장
            db.child("chat/$requestedRoomCode")
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            roomCode = requestedRoomCode
                            successJoined()
                        } else failJoined()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "Request Join Room is Cancelled.\n$error")
                    }
                })

            // 방 멤버 저장
            db.child("member/$requestedRoomCode/${CurUser.userName}")
                .setValue(ChatMember(CurUser.userName, false))
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
    }
}