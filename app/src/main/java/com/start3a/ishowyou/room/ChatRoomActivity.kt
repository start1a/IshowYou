package com.start3a.ishowyou.room

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.hoanganhtuan95ptit.draggable.DraggablePanel
import com.hoanganhtuan95ptit.draggable.utils.reWidth
import com.rw.keyboardlistener.KeyboardUtils
import com.start3a.ishowyou.R
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.data.Content
import com.start3a.ishowyou.data.FullScreenController
import com.start3a.ishowyou.data.RoomRequest
import com.start3a.ishowyou.room.chat.RealTimeChatFragment
import com.start3a.ishowyou.room.content.YoutubeContentEditFragment
import com.start3a.ishowyou.room.content.YoutubePlayerFragment
import com.start3a.ishowyou.room.member.RoomMemberFragment
import com.start3a.ishowyou.signin.SigninActivity
import kotlinx.android.synthetic.main.activity_chat_room.*
import kotlinx.android.synthetic.main.layout_draggable_bottom.*
import kotlinx.android.synthetic.main.layout_draggable_top.*

class ChatRoomActivity : AppCompatActivity() {

    private var viewModel: ChatRoomViewModel? = null
    private var isKeyboardUp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(ChatRoomViewModel::class.java)
        }

        viewModel!!.let { vm ->
            vm.isJoinRoom = true
            initView()
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
            if (vm.isJoinRoom) {
                if (vm.isFullScreen) {
                    vm.mFullScreenController.contentExitFullScreenMode?.invoke()
                }
                else {
                    bottom_navigation_chatroom.selectedItemId =
                        when (bottom_navigation_chatroom.selectedItemId) {
                        R.id.action_contents -> R.id.action_member
                        R.id.action_member -> R.id.action_chat
                        else -> R.id.action_contents
                    }
                }
            }
            else signOut()
        }
    }

    private fun initView() {
        viewModel!!.let { vm ->
            chatroom_layout.viewTreeObserver.addOnGlobalLayoutListener {
                if (!vm.isActivitySizeMeasured) {
                    vm.isActivitySizeMeasured = true

                    vm.activity_width = chatroom_layout.width
                    vm.activity_height = chatroom_layout.height
                    draggablePanel.resizeFraneFirstWidth(vm.activity_width)
                    // 가로
                    if (vm.activity_width > vm.activity_height)
                        draggablePanel.setHeightMax(vm.activity_height)
                    else
                        draggablePanel.setHeightMax((vm.activity_width / 16) * 9)
                }
            }

            vm.initRoomCurContent = { content ->
                val ft = supportFragmentManager.beginTransaction()
                when (content) {
                    Content.YOUTUBE -> {
                        ft.replace(R.id.frameTop, YoutubePlayerFragment())
                            .replace(R.id.frameBottom, YoutubeContentEditFragment())
                            .replace(R.id.frameTopRightTab, RealTimeChatFragment())
                            .commit()
                    }
                }
            }

            vm.mFullScreenController = FullScreenController(
                this,
                chatroom_layout,
                frameTop,
                frameTopRightTab,
            )

            vm.openFullScreenChatView = { visible ->
                if (visible) {
                    changeWeightContentFrame((vm.activity_width * 0.7).toInt(), (vm.activity_width * 0.3).toInt())
                }
                else frameTop.reWidth(vm.activity_width)
            }

            // 하단 메뉴
            bottom_navigation_chatroom.selectedItemId = R.id.action_contents
            bottom_navigation_chatroom.setOnNavigationItemSelectedListener { item ->
                val sfm = supportFragmentManager.beginTransaction()
                when (item.itemId) {
                    R.id.action_contents -> {
                        sfm.replace(R.id.frameBottom, YoutubeContentEditFragment())
                            .commit()
                        true
                    }
                    R.id.action_member -> {
                        sfm.replace(R.id.frameBottom, RoomMemberFragment()).commit()
                        true
                    }
                    R.id.action_chat -> {
                        sfm.replace(R.id.frameBottom, RealTimeChatFragment()).commit()
                        true
                    }
                    else -> false
                }
            }

            draggablePanel.setDraggableListener(object : DraggablePanel.DraggableListener {
                override fun onChangeState(state: DraggablePanel.State) {

                    if (state == DraggablePanel.State.MAX)
                        frameTopRightTab.visibility = View.VISIBLE
                    else if (state == DraggablePanel.State.MIN) {
                        frameTopRightTab.visibility = View.GONE
                    }
                }

                override fun onChangePercent(percent: Float) {
                    alpha.alpha = 1 - percent
                }
            })

            // 키보드 활성화 / 비활성화
            KeyboardUtils.addKeyboardToggleListener(this) { isVisible ->
                if (vm.isFullScreen) {
                    vm.contentAvailability?.invoke(isVisible)
                    // 비율 조절
                    if (isVisible) {
                        isKeyboardUp = true
                        draggablePanel.run {
                            setHeightMax(getVisibleViewHeight())
                            changeWeightContentFrame(vm.activity_width / 2, vm.activity_width / 2)
                        }
                    }
                    else {
                        if (isKeyboardUp) {
                            isKeyboardUp = false
                            draggablePanel.run {
                                setHeightMax(vm.activity_height)
                                changeWeightContentFrame((vm.activity_width * 0.7).toInt(), (vm.activity_width * 0.3).toInt())
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initRoom() {
        val requestcode = intent.getIntExtra("requestcode", -1)

        viewModel!!.let { vm ->
            when (requestcode) {
                // 방 생성
                RoomRequest.CREATE_ROOM.num -> {
                    val title = intent.getStringExtra("title")!!
                    vm.createChatRoom(title,
                        // 방 생성 성공
                        { contentName ->
                            when (contentName) {
                                "Youtube" -> {
                                    val videos =
                                        intent.getParcelableArrayListExtra<YoutubeSearchData>("videos")!!
                                    vm.addVideoToPlaylist_Youtube(videos)
                                    viewModel!!.initRoomCurContent(Content.YOUTUBE)
                                    vm.curVideoPlayed.value = vm.listPlayYoutube.value!![0]
                                }
                            }
                        },
                        // 방 정보 변경
                        {

                        }
                    )
                }

                // 방 입장
                RoomRequest.JOIN_ROOM.num -> {
                    val roomCode = intent.getStringExtra("roomcode")!!
                    // 방장이 방에 재접속일 경우 체크
                    vm.isHost = intent.getBooleanExtra("ishost", false)

                    vm.requestJoinRoom(roomCode, vm.isHost,
                        // 방 입장 성공
                        { contentName ->

                            when (contentName) {
                                "Youtube" -> {
                                    vm.notifyPrevVideoPlayList()
                                    vm.initRoomCurContent(Content.YOUTUBE)
                                }
                            }
                            vm.notifyDeleteRoom {
                                val intent = Intent().apply {
                                    putExtra("message", "방장이 퇴장했습니다.")
                                }
                                setResult(Activity.RESULT_CANCELED, intent)
                                finish()
                            }

                        },
                        // 방 입장 실패
                        {
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
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                draggablePanel.getFrameSecond().visibility = View.GONE
                hideSystemUI()
            }
            else if (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
                vm.isFullScreen = false
                draggablePanel.getFrameSecond().visibility = View.VISIBLE
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
                showSystemUI()
            }
            vm.isActivitySizeMeasured = false
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

    private fun getVisibleViewHeight(): Int {
        val visibleFrameSize = Rect()
        chatroom_layout.getWindowVisibleDisplayFrame(visibleFrameSize)

        return visibleFrameSize.bottom - visibleFrameSize.top
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

    private fun changeWeightContentFrame(w1: Int, w2: Int) {
        frameTop.reWidth(w1)
        frameTopRightTab.reWidth(w2)
    }
}