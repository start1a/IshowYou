package com.start3a.ishowyou.model

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.start3a.ishowyou.data.ChatRoom

class YoutubeDao(private val db: DatabaseReference) {

    private val TAG = "YoutubeDao"
    private var seekbarChangedListener: ValueEventListener? = null

    fun seekBarYoutubeClicked(time: Double) {
        db.child("seekbar").setValue(time)
    }

    fun setSeekbarChangedListener(changeListener: (Float) -> Unit) {
        seekbarChangedListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val time = snapshot.getValue<Double>()!!
                changeListener(time.toFloat())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Youtube Seek is Cancelled.")
            }
        }
        db.child("seekbar").addValueEventListener(seekbarChangedListener!!)
    }

    fun removeSeekbarChangedListener() {
        seekbarChangedListener?.let { db.removeEventListener(it) }
    }
}

class ChatDao(private val db: DatabaseReference) {

    private val TAG = "ChatDao"
    private var roomInfoChildChangedListener: ChildEventListener? = null

    fun createChatRoom(
        roomCode: String,
        title: String,
        successListener: () -> Unit,
        roomInfoChangedListener: () -> Unit
    ) {
        db.child("chat/$roomCode").setValue(ChatRoom(title))

        roomInfoChildChangedListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                successListener()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                roomInfoChangedListener()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Creating Chatroom is Cancelled.")
            }
        }
        db.child("chat/$roomCode").addChildEventListener(roomInfoChildChangedListener!!)
    }

    fun leaveRoom(roomCode: String) {
        db.child("chat/$roomCode").let {  dbr ->
            dbr.removeValue()
            roomInfoChildChangedListener?.let {  dbr.removeEventListener(it) }
        }
    }
}