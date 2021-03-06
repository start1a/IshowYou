package com.start3a.ishowyou.room.chat

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.start3a.ishowyou.R
import com.start3a.ishowyou.data.CurUser
import com.start3a.ishowyou.room.ChatRoomViewModel
import kotlinx.android.synthetic.main.fragment_real_time_chat.*
import kotlinx.android.synthetic.main.layout_draggable_bottom.*

class RealTimeChatFragment : Fragment() {

    private var viewModel: ChatRoomViewModel? = null
    private var listChatAdapter: ChatMessageAdapter? = null

    private lateinit var messageListView: RecyclerView
    private var posLastItem = -1

    private lateinit var mContext: Context
    private lateinit var curTab: BottomNavigationView

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

        viewModel = requireActivity().application!!.let {
            ViewModelProvider(
                requireActivity().viewModelStore,
                ViewModelProvider.AndroidViewModelFactory(it)
            )
                .get(ChatRoomViewModel::class.java).apply {
                    isMessageListUpScrolled = false
                    mContext = requireContext()
                }
        }

        viewModel!!.let { vm ->
            initAdapter()
            initView()

            curTab = requireActivity().bottom_navigation_chatroom
            val badge = curTab.getOrCreateBadge(R.id.action_chat).run {
                number = 0
                isVisible = false
                this
            }

            vm.isJoinRoom.observe(viewLifecycleOwner) {
                if (it) vm.initChatRoom {
                    if (curTab.selectedItemId != R.id.action_chat) {
                        badge.number = badge.number + 1
                        badge.isVisible = true
                    }
                }
            }

            btnSendMessage.setOnClickListener {
                val message = editSendMessage.text.toString()
                if (message.isNotEmpty()) {
                    vm.sendChatMessage(message)
                    editSendMessage.text.clear()
                }
            }
        }
    }

    private fun initAdapter() {
        viewModel!!.let { vm ->
            // 메세지 리스트
            listChatAdapter = ChatMessageAdapter(vm.listMessage.value!!, requireContext())

            messageListView.adapter = listChatAdapter
            messageListView.layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun initView() {
        viewModel!!.let { vm ->
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
                }
            })

            // 키보드가 생성될 경우
            messageListView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                // 채팅을 스크롤하지 않음
                if (bottom < oldBottom && !vm.isMessageListUpScrolled) {
                    Handler().postDelayed({ scrollToLastItem() }, 100)
                }
            }

            // 메세지 수신
            vm.listMessage.observe(viewLifecycleOwner) {
                listChatAdapter?.notifyDataSetChanged()
                if (it.size > 0)
                    posLastItem = it.size - 1

                // 메세지 스크롤 업 체크
                if (vm.isMessageListUpScrolled) {
                    // 내가 보낸 매세지
                    if (posLastItem != -1 && it[posLastItem].userName == CurUser.userName) {
                        scrollToLastItem()
                        btnShowNewMessage.visibility = View.GONE
                    }
                    else if (btnShowNewMessage.visibility == View.GONE)
                        btnShowNewMessage.visibility = View.VISIBLE
                }
                else scrollToLastItem()
            }

            // 텍스트 전송 버튼 활성화
            editSendMessage.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s.toString().isNotEmpty())
                        btnSendMessage.visibility = View.VISIBLE
                    else
                        btnSendMessage.visibility = View.GONE
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            })

            // 키보드 전송 버튼 클릭
            editSendMessage.setOnEditorActionListener { v, actionId, event ->
                var handled = false

                if (v.id == R.id.editSendMessage && actionId == EditorInfo.IME_ACTION_SEND) {
                    handled = true
                    btnSendMessage.callOnClick()
                }
                handled
            }

            btnShowNewMessage.setOnClickListener {
                scrollToLastItem()
                btnShowNewMessage.visibility = View.GONE
            }

            if (vm.hideKeyboard == null) {
                vm.hideKeyboard = {
                    val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(editSendMessage.windowToken, 0)
                }
            }
        }
    }

    private fun scrollToLastItem() {
        if (posLastItem != -1)
            messageListView.scrollToPosition(posLastItem)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            chat_layout.setBackgroundColor(Color.BLACK)

            listChatAdapter?.let {
                it.isFullScreen = true
                it.notifyDataSetChanged()
            }

            editSendMessage.run {
                setTextColor(Color.WHITE)
                setHintTextColor(Color.LTGRAY)
            }
        }

        else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            chat_layout.setBackgroundColor(Color.WHITE)

            listChatAdapter?.let {
                it.isFullScreen = false
                it.notifyDataSetChanged()
            }
            editSendMessage.setTextColor(Color.BLACK)
        }
    }
}