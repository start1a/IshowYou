package com.start3a.ishowyou.room

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.start3a.ishowyou.R
import com.start3a.ishowyou.data.Content
import com.start3a.ishowyou.data.FullScreenController
import com.start3a.ishowyou.data.RoomRequest
import com.start3a.ishowyou.room.chat.ChatMemberAdapter
import com.start3a.ishowyou.room.chat.RealTimeChatFragment
import com.start3a.ishowyou.room.content.YoutubePlayerFragment
import kotlinx.android.synthetic.main.activity_chat_room.*
import kotlinx.android.synthetic.main.info_chatroom.*

class ChatRoomActivity : AppCompatActivity() {

    private var viewModel: ChatRoomViewModel? = null
    private var listMemberAdapter: ChatMemberAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(ChatRoomViewModel::class.java)
        }

        viewModel!!.let { vm ->
            initView()
            initDrawer()
            initRoom()

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
            if (vm.isFullScreen)
                vm.mFullScreenController.rotate(false)
            else leaveRoom()
        }
    }

    private fun initView() {
        viewModel!!.let { vm ->
            vm.initRoomCurContent = { content ->
                val ft = supportFragmentManager.beginTransaction()
                when (content) {
                    Content.YOUTUBE ->
                        ft.replace(R.id.chatroom_contentViewFrame, YoutubePlayerFragment()).commit()
                }
            }

            vm.mFullScreenController = FullScreenController(
                this,
                chatroom_layout,
                chatroom_contentViewFrame,
                chatroom_talkViewFrame,
            )

            chatroom_drawer_layout.addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                }

                override fun onDrawerOpened(drawerView: View) {
                }

                override fun onDrawerClosed(drawerView: View) {
                    chatroom_drawer_layout.visibility = View.GONE
                }

                override fun onDrawerStateChanged(newState: Int) {
                }

            })
        }
    }

    private fun initDrawer() {
        viewModel!!.let { vm ->
            listMemberAdapter = ChatMemberAdapter(vm.listMember)
            vm.openChatRoomMenu = {
                chatroom_drawer_layout.visibility = View.VISIBLE
                chatroom_drawer_layout.openDrawer(GravityCompat.START)
                memberRecyclerView.adapter = listMemberAdapter
                memberRecyclerView.layoutManager = LinearLayoutManager(this)
            }

            btnLeaveRoom.setOnClickListener {
                leaveRoom()
            }
        }
    }

    private fun leaveRoom() {
        val builder = AlertDialog.Builder(this)

        builder.setMessage("채팅방에서 나가시겠습니까?")
            .setPositiveButton("확인") { _, _ ->

                viewModel!!.leaveRoom()
                chatroom_drawer_layout.closeDrawer(GravityCompat.START)
                finish()
            }
            .setNegativeButton("취소", null)
            .create().show()
    }

    private fun initRoom() {
        val requestcode = intent.getIntExtra("requestcode", -1)
        val roomSucceedJoined = object : () -> Unit {
            override fun invoke() {
                supportFragmentManager.beginTransaction()
                    .add(R.id.chatroom_contentViewFrame, YoutubePlayerFragment())
                    .add(R.id.chatroom_talkViewFrame, RealTimeChatFragment())
                    .commit()
            }
        }

        viewModel!!.let { vm ->
            when (requestcode) {
                // 방 생성
                RoomRequest.CREATE_ROOM.num -> {
                    val title = intent.getStringExtra("title")!!
                    vm.createChatRoom(title,
                        // 방 생성 성공
                        roomSucceedJoined,
                        // 방 정보 변경
                        {

                        })
                }

                // 방 입장
                RoomRequest.JOIN_ROOM.num -> {
                    val roomCode = intent.getStringExtra("roomcode")!!
                    vm.requestJoinRoom(roomCode, roomSucceedJoined, {
                        // 방 입장 실패
                        val intent = Intent().apply {
                            putExtra("message", "방이 존재하지 않습니다.")
                        }
                        setResult(Activity.RESULT_CANCELED, intent)
                        finish()
                    })
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        viewModel!!.let { vm ->
            // 화면 회전 시 풀스크린 on / off
            if (resources.configuration.orientation == ORIENTATION_LANDSCAPE) {
                vm.isFullScreen = true
                hideSystemUI()
                vm.mFullScreenController.enterFullScreenView(7.0f, 3.0f)
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