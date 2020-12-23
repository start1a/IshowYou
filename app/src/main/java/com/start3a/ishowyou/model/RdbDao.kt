package com.start3a.ishowyou.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.start3a.ishowyou.data.ChatMember
import com.start3a.ishowyou.data.ChatMessage
import com.start3a.ishowyou.data.ChatRoom
import java.util.*

class YoutubeDao(private val db: DatabaseReference) {

    private val TAG = "YoutubeDao"
    private var seekbarChangedListener: ValueEventListener? = null

    fun seekBarYoutubeClicked(time: Double) {
        db.child("seekbar").setValue(time)
    }

    fun setSeekbarChangedListener(changeListener: (Float) -> Unit) {
        seekbarChangedListener =
            db.child("seekbar").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val time = snapshot.getValue<Double>()!!
                    changeListener(time.toFloat())
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "Youtube Seek is Cancelled.\n$error")
                }
            })
    }

    fun removeSeekbarChangedListener() {
        seekbarChangedListener?.let { db.removeEventListener(it) }
    }
}

class ChatDao(private val db: DatabaseReference) {

    private val TAG = "ChatDao"
    private var roomCode: String? = null
    private val curUserId = FirebaseAuth.getInstance().currentUser!!.email!!.split("@")[0]

    private var newRoomNotifyListener: ValueEventListener? = null
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
                // 방으로 화면 이동
                successListener()
            }
            .addOnFailureListener {
                Log.d(TAG, "Creating ChatRoom is Failed\n$it")
            }

        db.child("member/$roomCode/$curUserId").setValue(ChatMember(curUserId, true))
    }

    fun leaveRoom(isHost: Boolean) {
        if (isHost) {
            db.child("chat/$roomCode").removeValue()
            db.child("message/$roomCode").removeValue()
            db.child("member/$roomCode").removeValue()
        }
        else {
            db.child("member/$roomCode/$curUserId").removeValue()
            removeListener(hostDeleteRoomNotifyListener, "member/$roomCode")
        }

        removeListener(roomInfoChildChangedListener, "chat/$roomCode")
        removeListener(messageNotifyListener, "message/$roomCode")
        removeListener(memberNotifyListener, "member/$roomCode")

        roomCode = null
    }

    fun notifyChatMessage(messageAdded: (ChatMessage) -> Unit) {
        messageNotifyListener =
            db.child("message/$roomCode").addChildEventListener(object : ChildEventListener {

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot.getValue<ChatMessage>()?.let {
                        messageAdded(it)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "Notifying Chat Message is Cancelled.\n$error")
                }
            })
    }

    fun notifyChatMember(memberAdded: (ChatMember) -> Unit, memberRemoved: (String) -> Unit) {
        memberNotifyListener =
            db.child("member/$roomCode").addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot.getValue<ChatMember>()?.let {
                        memberAdded(it)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
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
            ChatMessage(curUserId, message, time)
        )
    }

    fun requestUserChatRoomList(
        firstRequestRoomListSucceed: (MutableList<ChatRoom>) -> Unit,
        newRoomCreated: (ChatRoom) -> Unit
    ) {
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

                notifyNewRoomCreated(newRoomCreated)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Initializing Chat Room List is Cancelled.\n$error")
            }
        })
    }

    private fun notifyNewRoomCreated(newRoomCreated: (ChatRoom) -> Unit) {
        // 새 방이 생성될 때마다 실행
        newRoomNotifyListener =
            db.child("chat").addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(ChatRoom::class.java)?.let {
                        // 리스너가 처음 연결될 때 빈 방이 리턴됨
                        // 빈 방인지 체크
                        if (it.timeCreated != 0L)
                            newRoomCreated(it)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "Requesting Chat Room List is Cancelled.\n$error")
                }

            })
    }

    fun requestJoinRoom(
        requestedRoomCode: String,
        successJoined: (String) -> Unit,
        failJoined: () -> Unit
    ) {
        // 방 입장
        db.child("chat/$requestedRoomCode")
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        successJoined(requestedRoomCode)
                    }
                    else failJoined()
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "Request Join Room is Cancelled.\n$error")
                }
            })

        // 방 멤버 저장
        db.child("member/$requestedRoomCode/$curUserId").setValue(ChatMember(curUserId, false))
    }

    // 방 제거 여부
    // 방장이 방을 나가면 삭제됨
    fun notifyIsRoomDeleted(roomDeleted: () -> Unit) {
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

    fun joinRoom(code: String) {
        roomCode = code
    }

    fun closeJoinRoom() {
        removeListener(newRoomNotifyListener, "chat")
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