package com.start3a.ishowyou.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
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
                    Log.d(TAG, "Youtube Seek is Cancelled.")
                }
            })
    }

    fun removeSeekbarChangedListener() {
        seekbarChangedListener?.let { db.removeEventListener(it) }
    }
}

class ChatDao(private val db: DatabaseReference) {

    private val TAG = "ChatDao"
    private var roomInfoChildChangedListener: ValueEventListener? = null
    private var messageNotifyListener: ChildEventListener? = null

    fun createChatRoom(
        roomCode: String,
        chatRoom: ChatRoom,
        successListener: () -> Unit,
        roomInfoChangedListener: (ChatRoom) -> Unit
    ) {
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
                            Log.d(TAG, "Changing Room Info is Cancelled.")
                        }
                    })
                // 방으로 화면 이동
                successListener()
            }
            .addOnFailureListener {
                Log.d(TAG, "Creating ChatRoom is Failed.")
            }
    }

    fun leaveRoom(roomCode: String) {
        db.child("chat/$roomCode").let { dbr ->
            roomInfoChildChangedListener?.let { dbr.removeEventListener(it) }
            dbr.removeValue()
        }
        db.child("message/$roomCode").let { dbr ->
            messageNotifyListener?.let { dbr.removeEventListener(it) }
            dbr.removeValue()
        }
    }

    fun notifyChatMessage(roomCode: String, messageAdded: (ChatMessage) -> Unit) {
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
                    Log.d(TAG, "Notifying Chat Message is Cancelled.")
                }
            })
    }

    fun sendChatMessage(roomCode: String, message: String) {
        val curUserId = FirebaseAuth.getInstance().currentUser!!.email!!.split("@")[0]
        val time = Date().time
        db.child("message/$roomCode/$time").setValue(
            ChatMessage(curUserId, message, time)
        )
    }
}