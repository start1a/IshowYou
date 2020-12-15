package com.start3a.ishowyou.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.start3a.ishowyou.R
import com.start3a.ishowyou.main.chat.NoRoomFragment
import com.start3a.ishowyou.main.chat.RealTimeChatFragment
import com.start3a.ishowyou.main.content.YoutubePlayerFragment
import com.start3a.ishowyou.signin.SigninActivity

class MainActivity : AppCompatActivity() {

    private var viewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(MainViewModel::class.java)
        }

        viewModel!!.let { vm ->
            supportFragmentManager.beginTransaction()
                .replace(R.id.contentViewFrame, YoutubePlayerFragment()).commit()

            vm.createChatRoomViewListener = {
                supportFragmentManager.beginTransaction().let {
                    if (vm.isJoinRoom)
                        it.replace(R.id.talkViewFrame, RealTimeChatFragment()).commit()
                    else
                        it.replace(R.id.talkViewFrame, NoRoomFragment()).commit()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AuthUI.getInstance().signOut(applicationContext).addOnSuccessListener {
            val intent = Intent(applicationContext, SigninActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}