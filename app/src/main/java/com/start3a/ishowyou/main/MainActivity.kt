package com.start3a.ishowyou.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.start3a.ishowyou.R
import com.start3a.ishowyou.data.Content
import com.start3a.ishowyou.data.FullScreenController
import com.start3a.ishowyou.data.RoomRequest
import com.start3a.ishowyou.main.content.LobbyYoutubeFragment
import com.start3a.ishowyou.room.ChatRoomActivity
import com.start3a.ishowyou.signin.SigninActivity
import kotlinx.android.synthetic.main.activity_main.*

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

            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    if (vm.isFullScreen) {
                        hideSystemUI()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        viewModel!!.let { vm ->
            if (vm.isFullScreen) {
                vm.mFullScreenController.contentExitFullScreenMode?.invoke()
            }
            else signOut()
        }
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

            // 로비 컨텐츠 뷰 변경
            vm.initLobbyCurContent = { content ->
                val ft = supportFragmentManager.beginTransaction()
                when (content) {
                    Content.YOUTUBE ->
                        ft.replace(R.id.contentViewFrame, LobbyYoutubeFragment()).commit()
                }
            }

            // 화면 회전
            vm.mFullScreenController = FullScreenController(
                this,
                main_layout,
                contentViewFrame,
                roomMenuViewFrame
            )
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        viewModel!!.let { vm ->
            // 화면 회전 시 풀스크린 on / off
            if (resources.configuration.orientation == ORIENTATION_LANDSCAPE) {
                vm.isFullScreen = true
                hideSystemUI()
                vm.mFullScreenController.enterFullScreenView(1.0f, 0.0f)
            } else if (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
                vm.isFullScreen = false
                showSystemUI()
                vm.mFullScreenController.exitFullScreenView()
            }
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
   }
}