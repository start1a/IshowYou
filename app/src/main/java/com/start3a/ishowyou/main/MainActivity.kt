package com.start3a.ishowyou.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.start3a.ishowyou.R
import com.start3a.ishowyou.data.Content
import com.start3a.ishowyou.main.chat.ChatMemberAdapter
import com.start3a.ishowyou.main.chat.NoRoomFragment
import com.start3a.ishowyou.main.chat.RealTimeChatFragment
import com.start3a.ishowyou.main.content.YoutubePlayerFragment
import com.start3a.ishowyou.room.ChatRoomActivity
import com.start3a.ishowyou.signin.SigninActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.info_chatroom.*

class MainActivity : AppCompatActivity() {

    private var viewModel: MainViewModel? = null
    private var listMemberAdapter: ChatMemberAdapter? = null

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
//            initDrawer()

            supportFragmentManager.beginTransaction()
                    // 별도 프래그먼트를 추가 구현할 것
 //               .add(R.id.contentViewFrame, YoutubePlayerFragment())
                .add(R.id.RoomMenuViewFrame, NoRoomFragment())
                .commit()
        }
    }

    override fun onBackPressed() {
//        if (main_drawer_layout.isDrawerOpen(GravityCompat.START)) {
//            main_drawer_layout.closeDrawer(GravityCompat.START)
//        } else {
            super.onBackPressed()
            AuthUI.getInstance().signOut(applicationContext).addOnSuccessListener {
                val intent = Intent(applicationContext, SigninActivity::class.java)
                startActivity(intent)
                finish()
//            }
        }
    }

    private fun initMainView() {
        viewModel!!.let { vm ->

            vm.messageView = { text ->
                Toast.makeText(this, text, Toast.LENGTH_LONG).show()
            }

            // 방 출입 뷰
            vm.createChatRoomView = {
                val intent = Intent(this, ChatRoomActivity::class.java)
                startActivity(intent)
            }

            vm.initRoomCurContent = { content ->
                val ft = supportFragmentManager.beginTransaction()
                when (content) {
                    Content.YOUTUBE ->
                            ft.replace(R.id.contentViewFrame, YoutubePlayerFragment()).commit()
                }
            }
        }
    }

    private fun initDrawer() {
        viewModel!!.let { vm ->
            listMemberAdapter = ChatMemberAdapter(vm.listMember)
            vm.openChatRoomMenu = {
//                main_drawer_layout.openDrawer(GravityCompat.START)
                memberRecyclerView.adapter = listMemberAdapter
                memberRecyclerView.layoutManager = LinearLayoutManager(this)
            }

            btnLeaveRoom.setOnClickListener {
                if (vm.isJoinRoom)
                    leaveRoom()
            }
        }
    }

    private fun leaveRoom() {
        val builder = AlertDialog.Builder(this)

        builder.setMessage("채팅방에서 나가시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                viewModel!!.run {
                    leaveRoom()
                }
//                main_drawer_layout.closeDrawer(GravityCompat.START)
            }
            .setNegativeButton("취소", null)
            .create().show()
    }
}