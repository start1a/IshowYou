package com.start3a.ishowyou.main.chat

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
            initAdapter()
            vm.initChatRoom()
            vm.listMessage.observe(viewLifecycleOwner) {
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
            btnChatRoomMenu.setOnClickListener { vm.openChatRoomMenu() }

            btnSendMessage.setOnClickListener {
                val message = editSendMessage.text.toString()
                vm.sendChatMessage(message)
                editSendMessage.text.clear()
            }
        }
    }

    private fun initAdapter() {
        viewModel!!.let { vm ->
            // 메세지 리스트
            listChatAdapter = ChatMessageAdapter(vm.listMessage.value!!)
            messageRecyclerView.adapter = listChatAdapter
            messageRecyclerView.layoutManager = LinearLayoutManager(activity)
        }
    }
}