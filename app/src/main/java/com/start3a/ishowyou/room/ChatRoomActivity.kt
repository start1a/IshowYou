package com.start3a.ishowyou.room

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.start3a.ishowyou.R
import com.start3a.ishowyou.data.Content
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

        initView()
        initDrawer()
        initRoom()
    }

    override fun onBackPressed() {
        leaveRoom()
    }

    private fun initView() {
        viewModel!!.initRoomCurContent = { content ->
            val ft = supportFragmentManager.beginTransaction()
            when (content) {
                Content.YOUTUBE ->
                    ft.replace(R.id.chatroom_contentViewFrame, YoutubePlayerFragment()).commit()
            }
        }
    }

    private fun initDrawer() {
        viewModel!!.let { vm ->
            listMemberAdapter = ChatMemberAdapter(vm.listMember)
            vm.openChatRoomMenu = {
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
                    .add(R.id.chatroom_talkViewFrame, RealTimeChatFragment()).commit()
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
}