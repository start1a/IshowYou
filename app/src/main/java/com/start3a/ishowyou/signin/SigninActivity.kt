package com.start3a.ishowyou.signin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.start3a.ishowyou.R
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.room.ChatRoomActivity

class SigninActivity : AppCompatActivity() {

    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
    }

    override fun onStart() {
        super.onStart()
        if (!shouldStartSignIn()) navigateToMain()
        else startSignInWithGoogle()
    }

    private fun shouldStartSignIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser == null
    }

    private fun startSignInWithGoogle() {
        val intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setTheme(R.style.AppTheme_NoActionBar)
            .setLogo(R.drawable.ic_intro_ishowyou)
            .build()

        requestActivityForSignIn.launch(intent)
    }

    private fun navigateToMain() {
        val intent = Intent(applicationContext, ChatRoomActivity::class.java)
        startActivity(intent)
        finish()
    }

    private val requestActivityForSignIn: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            // 비디오 리스트
            if (activityResult.resultCode == Activity.RESULT_OK)
                navigateToMain()
            else startSignInWithGoogle()
        }
}