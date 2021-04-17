package com.start3a.ishowyou.room

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
import com.start3a.ishowyou.data.FullScreenController
import com.start3a.ishowyou.room.chat.RealTimeChatFragment
import com.start3a.ishowyou.room.content.YoutubeContentEditFragment
import com.start3a.ishowyou.room.content.YoutubePlayerFragment
import com.start3a.ishowyou.room.joinroom.NoRoomFragment
import com.start3a.ishowyou.room.roominfo.RoomMemberFragment
import com.start3a.ishowyou.signin.SigninActivity
import kotlinx.android.synthetic.main.activity_chat_room.*
import kotlinx.android.synthetic.main.layout_draggable_bottom.*
import kotlinx.android.synthetic.main.layout_draggable_top.*

class ChatRoomActivity : AppCompatActivity() {

    private var viewModel: ChatRoomViewModel? = null

    private var fragTop: YoutubePlayerFragment? = null
    private var fragBottom: YoutubeContentEditFragment? = null

    private var isKeyboardUp = false
    private var keyboardheight = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(ChatRoomViewModel::class.java)
        }

        viewModel!!.let { vm ->
            initView()

            supportFragmentManager.beginTransaction()
                .replace(R.id.frameTopRightTab, RealTimeChatFragment())
                .replace(R.id.main_view_frame, NoRoomFragment())
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
            if (vm.isJoinRoom.value!!) {
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
                    else if (state == DraggablePanel.State.MIN)
                        frameTopRightTab.visibility = View.GONE
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
                            enableDragFrame(false)
                        }
                    }
                    else {
                        if (isKeyboardUp) {
                            isKeyboardUp = false
                            draggablePanel.run {
                                setHeightMax(vm.activity_height)
                                changeWeightContentFrame((vm.activity_width * 0.7).toInt(), (vm.activity_width * 0.3).toInt())
                                enableDragFrame(true)
                            }
                        }
                    }
                }
            }

            vm.showDraggablePanel = { visible ->
                if (visible) draggablePanel.maximize()
                else draggablePanel.close()
            }

            vm.setRoomAttr = { isOpen, isHost ->
                val host = vm.isHost
                vm.isHost = isHost
                vm.isJoinRoom.value = isOpen
                vm.showDraggablePanel(isOpen)

                if (isOpen) replaceRoom()
                else vm.leaveRoom(host)
            }

            // 로딩 아래의 뷰 클릭 방지
            loading_layout.setOnClickListener {}
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
        if (keyboardheight != -1) return keyboardheight

        val visibleFrameSize = Rect()
        chatroom_layout.getWindowVisibleDisplayFrame(visibleFrameSize)
        keyboardheight = visibleFrameSize.bottom - visibleFrameSize.top

        return keyboardheight
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

    private fun replaceRoom() {
        val sft = supportFragmentManager.beginTransaction()

        if (fragTop != null && fragBottom != null)
            sft.remove(fragTop!!).remove(fragBottom!!)

        fragTop = YoutubePlayerFragment()
        fragBottom = YoutubeContentEditFragment()
        sft.replace(R.id.frameTop, fragTop!!)
            .replace(R.id.frameBottom, fragBottom!!)
            .commit()
        bottom_navigation_chatroom.selectedItemId = R.id.action_contents
    }
}