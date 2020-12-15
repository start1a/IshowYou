package com.start3a.ishowyou.main.chat

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.start3a.ishowyou.R
import com.start3a.ishowyou.main.MainViewModel
import kotlinx.android.synthetic.main.fragment_real_time_chat.*


class RealTimeChatFragment : Fragment() {

    private var viewModel: MainViewModel? = null
    private var listChatAdapter: ChatMessageAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_real_time_chat, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        viewModel = activity!!.application!!.let {
            ViewModelProvider(
                activity!!.viewModelStore,
                ViewModelProvider.AndroidViewModelFactory(it)
            )
                .get(MainViewModel::class.java)
        }

        viewModel!!.let { vm ->
            // 초기화
            listChatAdapter = ChatMessageAdapter(vm.listMessage)
            messageRecyclerView.adapter = listChatAdapter
            messageRecyclerView.layoutManager = LinearLayoutManager(activity)

            // 새 메세지
            vm.notifyChatMessage {
                vm.listMessage.add(it)
                listChatAdapter?.notifyDataSetChanged()
            }

            editSendMessage.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    btnSendMessage.isClickable = editSendMessage.text.isNotEmpty()
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            })

            // 채팅방 메뉴
            btnExtra.setOnClickListener { leaveRoom() }

            btnSendMessage.setOnClickListener {
                val message = editSendMessage.text.toString()
                vm.sendChatMessage(message)
                editSendMessage.text.clear()
            }
        }
    }

    private fun leaveRoom() {
        val builder = AlertDialog.Builder(activity!!)

        builder.setMessage("채팅방에서 나가시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                viewModel!!.leaveRoom()
            }
            .setNegativeButton("취소", null)
            .create().show()
    }
}