package com.start3a.ishowyou.main.chat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.start3a.ishowyou.R
import kotlinx.android.synthetic.main.activity_join_chat_room.*

class JoinChatRoomActivity : AppCompatActivity() {

    private var viewModel: JoinChatRoomViewModel? = null
    private lateinit var listRoomAdapter: ChatRoomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_chat_room)

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(JoinChatRoomViewModel::class.java)
        }

        viewModel!!.let { vm ->
            initAdapter()
            vm.loadRoomList()
            vm.listRoom.observe(this, {
                listRoomAdapter.notifyDataSetChanged()
            })
        }
    }

    private fun initAdapter() {
        viewModel!!.let { vm ->

            listRoomAdapter = ChatRoomAdapter(vm.listRoom.value!!)
            roomRecyclerView.adapter = listRoomAdapter
            roomRecyclerView.layoutManager = LinearLayoutManager(this)

            // 방이 선택됨
            listRoomAdapter.roomClicked = {
                val code = vm.listRoom.value!![it].timeCreated.toString()
                vm.requestJoinRoom(code,
                    // 입장 성공
                    {
                        val intent = Intent().apply {
                            putExtra("roomCode", code)
                        }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    },
                    // 실패
                    {
                        Toast.makeText(this, "방이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                    })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel!!.closeJoinRoom()
    }
}