package com.start3a.ishowyou.signin

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.start3a.ishowyou.model.RdbDao

class SignInViewModel: ViewModel() {

    // 실시간 데이터베이스
    private val mRdbDao = RdbDao(Firebase.database.reference)

    fun checkUserExist(userName: String, existListener: () -> Unit, notExistListener: () -> Unit) {
        mRdbDao.checkUserExist(userName, existListener, notExistListener)
    }

}