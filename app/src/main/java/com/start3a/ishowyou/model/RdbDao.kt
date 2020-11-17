package com.start3a.ishowyou.model

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.start3a.ishowyou.data.User

class RdbDao(private val db: DatabaseReference) {

    private val TAG = "mRdbDao"

    fun checkUserExist(userEmail: String, startMainListener: () -> Unit) {
        db.child("users/$userEmail").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 신규 유저일 경우
                if (!snapshot.exists()) {
                    val userId = userEmail.split("@")[0]
                    signUpUser(userEmail, userId)
                }
                startMainListener()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "check User Exist is Cancelled.")
            }
        })
    }

    fun signUpUser(userEmail: String, userId: String) {
        db.child("users/$userEmail")
            .setValue(
                User(userId, userEmail)
            )
    }

}