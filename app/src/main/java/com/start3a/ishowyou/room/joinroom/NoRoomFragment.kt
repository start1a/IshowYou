package com.start3a.ishowyou.room.joinroom

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.start3a.ishowyou.R
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.room.ChatRoomViewModel
import com.start3a.ishowyou.room.content.videoselection.YoutubeVideoSelectionActivity
import kotlinx.android.synthetic.main.activity_chat_room.*
import kotlinx.android.synthetic.main.fragment_no_room.*

class NoRoomFragment : Fragment() {

    private var viewModel: ChatRoomViewModel? = null
    private lateinit var listRoomAdapter: ChatRoomAdapter
    private var mSearchView: SearchView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_no_room, container, false)
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
            initRoomListAdapter()

            room_refresh_layout.setOnRefreshListener {
                vm.loadRoomList {
                    room_refresh_layout.isRefreshing = false
                }
            }

            btnCreateRoom.setOnClickListener {
                createRoom()
            }

            mSearchView = topAppBar.menu.findItem(R.id.action_search).actionView as SearchView
            mSearchView!!.let {
                it.maxWidth = Int.MAX_VALUE
                it.queryHint = "방 제목 검색"

                it.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        // 검색 성공
                        if (query != null && vm.isQueryAvailable(query)) {
                            it.clearFocus()
                            room_refresh_layout.isRefreshing = true
                            vm.searchRoomByKeyword(query,
                                { Toast.makeText(requireContext(), "일치하는 제목의 방이 존재하지 않습니다.", Toast.LENGTH_LONG).show() },
                                { room_refresh_layout.isRefreshing = false })
                        }

                        return true
                    }

                    override fun onQueryTextChange(newText: String?) = true
                })
            }

            btnCreateRoom.setColorFilter(Color.WHITE)
        }
    }

    private fun initRoomListAdapter() {
        viewModel!!.let { vm ->

            listRoomAdapter = ChatRoomAdapter(vm.listRoom.value!!)
            roomRecyclerView.adapter = listRoomAdapter
            roomRecyclerView.layoutManager = LinearLayoutManager(requireContext())

            // 방이 선택됨
            listRoomAdapter.roomClicked = { pos ->

                if (!vm.isJoinRoom.value!!) {
                    val code = vm.listRoom.value!![pos].id
                    requireActivity().loading_layout.visibility = View.VISIBLE

                    vm.requestJoinRoom(code, {
                        // 입장
                        vm.setRoomAttr(true, false)
                        requireActivity().loading_layout.visibility = View.GONE

                        vm.notifyDeleteRoom()
                    },
                        // 방 없음
                    { Toast.makeText(requireContext(), "방이 존재하지 않습니다.", Toast.LENGTH_LONG).show() })
                }
            }
            vm.listRoom.observe(viewLifecycleOwner) {
                if (it.size == 0)
                    no_room_layout.visibility = View.VISIBLE
                else no_room_layout.visibility = View.GONE

                listRoomAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun createRoom() {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_input_button, null)
        val editInput = view.findViewById<EditText>(R.id.editInput).apply {
            val curUserId = FirebaseAuth.getInstance().currentUser!!.email!!.split("@")[0]
            hint = "${curUserId}님의 방"
            showKeyboard()
        }
        val alertDialog = AlertDialog.Builder(activity)

        val dialog = alertDialog
            .setView(view)
            .setTitle("방 제목 입력")
            .show()

        view.findViewById<Button>(R.id.btnDone).setOnClickListener {
            var title = editInput.text.toString()
            if (title.isBlank())
                title = editInput.hint.toString()

            viewModel!!.titleTemp = title
            dialog.dismiss()
            val intent = Intent(requireContext(), YoutubeVideoSelectionActivity::class.java)
            requestActivityForVideoSelection.launch(intent)
        }
    }

    private val requestActivityForVideoSelection: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            // 비디오 리스트
            if (activityResult.resultCode == Activity.RESULT_OK && activityResult.data != null) {
                activityResult.data!!.extras!!.getParcelableArrayList<YoutubeSearchData>("videos")?.let { videos ->
                    viewModel!!.let { vm ->

                        vm.createChatRoom(vm.titleTemp, {
                            // 방 생성
                            vm.addVideoToPlaylist_Youtube(videos)
                            vm.setRoomAttr(true, true)
                            vm.curVideoPlayed.value = vm.listPlayYoutube.value!![0]
                        },
                        // 방 정보 변경
                        {

                        })
                    }
                }
            }
            else Toast.makeText(requireContext(), "적어도 1개 이상의 영상이 필요합니다.", Toast.LENGTH_LONG).show()
        }

    private fun EditText.showKeyboard() {
        requestFocus()
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }
}