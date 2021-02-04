package com.start3a.ishowyou.room.chat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.start3a.ishowyou.R
import com.start3a.ishowyou.room.ChatRoomViewModel
import kotlinx.android.synthetic.main.fragment_real_time_chat.*

class RealTimeChatFragment : Fragment() {

    private var viewModel: ChatRoomViewModel? = null
    private var listChatAdapter: ChatMessageAdapter? = null

    private lateinit var messageListView: RecyclerView
    private var posLastItem = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_real_time_chat, container, false)
        messageListView = view.findViewById(R.id.messageRecyclerView)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = activity!!.application!!.let {
            ViewModelProvider(
                activity!!.viewModelStore,
                ViewModelProvider.AndroidViewModelFactory(it)
            )
                .get(ChatRoomViewModel::class.java)
        }

        viewModel!!.let { vm ->
            initAdapter()

            vm.initChatRoom {
                val intent = Intent().apply {
                    putExtra("message", "방장이 퇴장했습니다.")
                }
                activity!!.setResult(Activity.RESULT_CANCELED, intent)
                activity!!.finish()
            }

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

            messageListView.adapter = listChatAdapter
            messageListView.layoutManager = LinearLayoutManager(activity)

            messageListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val lastVisibleItemPosition =
                        (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                    val itemTotalCount = messageListView.adapter?.itemCount?.minus(1)

                    // 스크롤 이동
                    val isScrolled = lastVisibleItemPosition != itemTotalCount
                    vm.isMessageListUpScrolled = isScrolled
                    if (!isScrolled)
                        btnShowNewMessage.visibility = View.GONE

                    Log.d("TAGG", "scroll" + vm.isMessageListUpScrolled.toString())
                }
            })

            // 키보드가 생성될 경우
            messageListView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                // 채팅을 스크롤하지 않음
                Log.d("TAGG", "bottom : " + (bottom < oldBottom).toString())
                if (bottom < oldBottom && !vm.isMessageListUpScrolled) {
                    Handler().postDelayed(object : Runnable {
                        override fun run() {
                            scrollToLastItem()
                        }

                    }, 100)
                }
            }


            vm.listMessage.observe(viewLifecycleOwner) {
                listChatAdapter?.notifyDataSetChanged()
                if (it.size > 0)
                    posLastItem = it.size - 1

                // 메세지 스크롤 업 체크
                if (vm.isMessageListUpScrolled && btnShowNewMessage.visibility == View.GONE) {
                    btnShowNewMessage.visibility = View.VISIBLE
                    vm.isMessageListUpScrolled = false
                }
                else scrollToLastItem()
        }

            editSendMessage.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    btnSendMessage.isClickable = editSendMessage.text.isNotEmpty()
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            })

            btnShowNewMessage.setOnClickListener {
                if (posLastItem != -1)
                    scrollToLastItem()
                btnShowNewMessage.visibility = View.GONE
            }
        }
    }

    private fun scrollToLastItem() {
        if (posLastItem != -1)
            messageListView.scrollToPosition(posLastItem)
    }
}