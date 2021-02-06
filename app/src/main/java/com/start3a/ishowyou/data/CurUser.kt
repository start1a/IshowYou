package com.start3a.ishowyou.data

import com.google.firebase.auth.FirebaseAuth

class CurUser {
    companion object {
        val userName: String
            get() = FirebaseAuth.getInstance().currentUser!!.email!!.split("@")[0]
    }
}
