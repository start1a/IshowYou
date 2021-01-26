package com.start3a.ishowyou.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.start3a.ishowyou.R
import com.start3a.ishowyou.data.Content
import com.start3a.ishowyou.data.RoomRequest
import com.start3a.ishowyou.main.content.LobbyYoutubeFragment
import com.start3a.ishowyou.room.ChatRoomActivity
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
            initMainView()

            supportFragmentManager.beginTransaction()
                .add(R.id.contentViewFrame, LobbyYoutubeFragment())
                .add(R.id.roomMenuViewFrame, NoRoomFragment())
                .commit()
        }
    }

    override fun onBackPressed() {
        signOut()
    }

    private fun initMainView() {
        viewModel!!.let { vm ->

            // 방 출입 뷰
            vm.createChatRoom = { title ->
                val intent = Intent(this, ChatRoomActivity::class.java).apply {
                    putExtra("requestcode", RoomRequest.CREATE_ROOM.num)
                    putExtra("title", title)
                }
                startActivityForResult(intent, RoomRequest.CREATE_ROOM.num)
            }

            vm.initLobbyCurContent = { content ->
                val ft = supportFragmentManager.beginTransaction()
                when (content) {
                    Content.YOUTUBE ->
                        ft.replace(R.id.contentViewFrame, LobbyYoutubeFragment()).commit()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RoomRequest.CREATE_ROOM.num -> {
                // 정상적인 퇴장이 아닐 경우
                // ex. 방장이 퇴장하여 자동 퇴장
                if (resultCode != Activity.RESULT_OK && data != null) {
                    val text = data.getStringExtra("message")
                    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun signOut() {
        val builder = AlertDialog.Builder(this)

        builder.setMessage("로그아웃 하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                AuthUI.getInstance().signOut(applicationContext).addOnSuccessListener {
                    val intent = Intent(applicationContext, SigninActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            .setNegativeButton("취소", null)
            .create().show()
    }
}