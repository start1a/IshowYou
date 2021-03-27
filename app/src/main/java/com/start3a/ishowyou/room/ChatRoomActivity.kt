package com.start3a.ishowyou.room

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
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
import kotlinx.android.synthetic.main.activity_chat_room.*

class ChatRoomActivity : AppCompatActivity() {

    private var viewModel: ChatRoomViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(ChatRoomViewModel::class.java)
        }

        viewModel!!.let { vm ->
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
            if (vm.isFullScreen)
                vm.mFullScreenController.contentExitFullScreenMode?.invoke()
            // 탭 넘기기
            else {
                when (bottom_navigation_chatroom.selectedItemId) {
                    R.id.action_contents -> bottom_navigation_chatroom.selectedItemId =
                        R.id.action_member
                    R.id.action_member -> bottom_navigation_chatroom.selectedItemId =
                        R.id.action_chat
                    R.id.action_chat -> bottom_navigation_chatroom.selectedItemId =
                        R.id.action_contents
                    else -> {
                    }
                }
            }
        }
    }

    private fun initView() {
        viewModel!!.let { vm ->
            vm.initRoomCurContent = { content ->
                val ft = supportFragmentManager.beginTransaction()
                when (content) {
                    Content.YOUTUBE -> {
                        ft.replace(R.id.chatroom_contentViewFrame, YoutubePlayerFragment())
                            .replace(R.id.chatroom_talkViewFrame, YoutubeContentEditFragment())
                            .commit()
                    }
                }
            }

            vm.mFullScreenController = FullScreenController(
                this,
                chatroom_layout,
                chatroom_contentViewFrame,
                bottom_menu_layout_chatroom,
                chatroom_talkViewFrame,
            )

            // 하단 메뉴
            bottom_navigation_chatroom.selectedItemId = R.id.action_contents
            bottom_navigation_chatroom.setOnNavigationItemSelectedListener { item ->
                val sfm = supportFragmentManager.beginTransaction()
                when (item.itemId) {
                    R.id.action_contents -> {
                        sfm.replace(R.id.chatroom_talkViewFrame, YoutubeContentEditFragment())
                            .commit()
                        true
                    }
                    R.id.action_member -> {
                        sfm.replace(R.id.chatroom_talkViewFrame, RoomMemberFragment()).commit()
                        true
                    }
                    R.id.action_chat -> {
                        sfm.replace(R.id.chatroom_talkViewFrame, RealTimeChatFragment()).commit()
                        true
                    }
                    else -> false
                }
            }

            // 키보드 활성화 / 비활성화
            KeyboardUtils.addKeyboardToggleListener(this) { isVisible ->
                if (vm.isFullScreen) {
                    vm.contentAvailability?.invoke(isVisible)
                    // 비율 조절
                    if (isVisible) {
                        vm.mFullScreenController.resizeScreenHeight(getVisibleViewHeight())
                        vm.mFullScreenController.changeWeight(true, 5.0f, 5.0f)
                    } else {
                        vm.mFullScreenController.resizeScreenHeight()
                        vm.mFullScreenController.changeWeight(true, 7.0f, 3.0f)
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
                hideSystemUI()
                vm.mFullScreenController.enterFullScreenView(7.0f, 3.0f)
                // 채팅 뷰로 전환
                swapToChat()
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

    private fun getVisibleViewHeight(): Int {
        val visibleFrameSize = Rect()
        chatroom_layout.getWindowVisibleDisplayFrame(visibleFrameSize)

        return visibleFrameSize.bottom - visibleFrameSize.top
    }

    private fun swapToChat() {
        // 채팅 뷰만 보기
        supportFragmentManager.beginTransaction()
            .replace(R.id.chatroom_talkViewFrame, RealTimeChatFragment()).commit()

        bottom_navigation_chatroom.selectedItemId = R.id.action_chat
    }
}