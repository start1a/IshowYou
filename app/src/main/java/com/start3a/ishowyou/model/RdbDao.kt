package com.start3a.ishowyou.model

import android.util.Log
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.start3a.ishowyou.data.User

class RdbDao(private val db: DatabaseReference) {

    private val TAG = "mRdbDao"

    fun checkUserExist(userName: String, existListener: () -> Unit, notExistListener: () -> Unit) {
        db.child("users/$userName").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                    existListener()
                else
                    notExistListener()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "check User Exist is Cancelled.")
            }
        })
    }

//    fun signUpUser(
//        userName: String,
//        password: String,
//        phoneNumber: String,
//        successListener: () -> Unit,
//        failListener: () -> Unit
//    ) {
//        checkUserExist(userName,
//            // 아이디 중복
//            { failListener() },
//            // 가입 성공
//            {
//                db.child("users/$userName").setValue(User(userName, password))
//                successListener()
//            }
//        )
//    }

}