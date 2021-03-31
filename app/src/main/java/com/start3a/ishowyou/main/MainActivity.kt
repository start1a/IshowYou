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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.start3a.ishowyou.R
import com.start3a.ishowyou.data.RoomRequest
import com.start3a.ishowyou.main.menu.NoRoomFragment
import com.start3a.ishowyou.room.ChatRoomActivity
import com.start3a.ishowyou.signin.SigninActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var viewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(MainViewModel::class.java)
        }

        viewModel!!.let { vm ->
            initMainView()

            supportFragmentManager.beginTransaction()
                .add(R.id.roomMenuViewFrame, NoRoomFragment())
                .commit()

            if (!vm.isRoomJoined) {
                loading_layout.visibility = View.VISIBLE

                vm.checkPrevRoomJoin({ roomCode, isHost ->
                    vm.isRoomJoined = true
                    val intent = Intent(this, ChatRoomActivity::class.java).apply {
                        putExtra("requestcode", RoomRequest.JOIN_ROOM.num)
                        putExtra("roomcode", roomCode)
                        putExtra("ishost", isHost)
                    }
                    requestActivityForJoinRoom.launch(intent)
                }, {
                    loading_layout.visibility = View.GONE
                })
            }
            // 로딩 아래의 뷰 클릭 방지
            loading_layout.setOnClickListener {}
        }
    }

    override fun onBackPressed() {
        if (!viewModel!!.isRoomJoined)
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
                startActivity(intent)
            }

            // 하단 메뉴
            bottom_navigation.selectedItemId = R.id.action_menu
            bottom_navigation.setOnNavigationItemSelectedListener { item ->
                val sfm = supportFragmentManager.beginTransaction()
                when(item.itemId) {
                    R.id.action_contents -> {
                        // 콘텐츠 설정 프래그먼트 활성화
                        true
                    }
                    R.id.action_menu -> {
                        sfm.replace(R.id.roomMenuViewFrame, NoRoomFragment()).commit()
                        true
                    }
                    else -> false
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

    private val requestActivityForJoinRoom: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK && activityResult.data != null) {
                val text = activityResult.data!!.getStringExtra("message")
                Toast.makeText(this, text, Toast.LENGTH_LONG).show()
                viewModel!!.isRoomJoined = false
            }
        }
}