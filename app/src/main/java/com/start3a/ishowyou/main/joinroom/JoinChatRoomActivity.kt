package com.start3a.ishowyou.main.joinroom

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.start3a.ishowyou.R
import com.start3a.ishowyou.data.RoomRequest
import com.start3a.ishowyou.room.ChatRoomActivity
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
            vm.listShow.observe(this, {
                listRoomAdapter.notifyDataSetChanged()
            })

            btnRefresh.setOnClickListener {
                vm.loadRoomList()
            }
        }
    }

    private fun initAdapter() {
        viewModel!!.let { vm ->

            listRoomAdapter = ChatRoomAdapter(vm.listShow.value!!)
            roomRecyclerView.adapter = listRoomAdapter
            roomRecyclerView.layoutManager = LinearLayoutManager(this)

            // 방이 선택됨
            listRoomAdapter.roomClicked = {
                val code = vm.listShow.value!![it].timeCreated.toString()
                val intent = Intent(this, ChatRoomActivity::class.java).apply {
                    putExtra("requestcode", RoomRequest.JOIN_ROOM.num)
                    putExtra("roomcode", code)
                }
                startActivityForResult(intent, RoomRequest.JOIN_ROOM.num)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RoomRequest.JOIN_ROOM.num -> {
                if (resultCode != Activity.RESULT_OK && data != null) {
                    val text = data.getStringExtra("message")
                    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}