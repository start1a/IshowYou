package com.start3a.ishowyou.room

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.start3a.ishowyou.R
import com.start3a.ishowyou.main.MainViewModel
import com.start3a.ishowyou.main.chat.ChatMemberAdapter

class ChatRoomActivity : AppCompatActivity() {

    private var viewModel: ChatRoomViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(ChatRoomViewModel::class.java)
        }
    }
}