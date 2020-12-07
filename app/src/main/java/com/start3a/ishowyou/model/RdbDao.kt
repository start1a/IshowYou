package com.start3a.ishowyou.model

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

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