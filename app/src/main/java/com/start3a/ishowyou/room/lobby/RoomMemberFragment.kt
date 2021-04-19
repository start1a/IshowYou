package com.start3a.ishowyou.room.lobby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.start3a.ishowyou.R
import com.start3a.ishowyou.room.ChatRoomViewModel
import com.start3a.ishowyou.room.chat.ChatMemberAdapter
import kotlinx.android.synthetic.main.fragment_room_member.*

class RoomMemberFragment : Fragment() {

    private var viewModel: ChatRoomViewModel? = null
    private var listMemberAdapter: ChatMemberAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_room_member, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = requireActivity().application!!.let {
            ViewModelProvider(
                requireActivity().viewModelStore,
                ViewModelProvider.AndroidViewModelFactory(it)
            )
                .get(ChatRoomViewModel::class.java)
        }

        viewModel!!.let { vm ->
            vm.isJoinRoom.observe(viewLifecycleOwner) {
                if (it) vm.initMemberList()
            }
            
            listMemberAdapter = ChatMemberAdapter(vm.listMember.value!!)
            memberRecyclerView.adapter = listMemberAdapter
            memberRecyclerView.layoutManager = LinearLayoutManager(requireContext())

            vm.listMember.observe(viewLifecycleOwner) {
                listMemberAdapter?.notifyDataSetChanged()
            }

            btnLeaveRoom.setOnClickListener {
                leaveRoom()
            }
        }
    }

    private fun leaveRoom() {
        val builder = AlertDialog.Builder(requireContext())

        builder.setMessage("채팅방에서 나가시겠습니까?")
            .setPositiveButton("확인") { _, _ ->

                viewModel!!.setRoomAttr(false, false)
            }
            .setNegativeButton("취소", null)
            .create().show()
    }
}