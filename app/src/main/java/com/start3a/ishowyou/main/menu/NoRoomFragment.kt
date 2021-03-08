package com.start3a.ishowyou.main.menu

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.start3a.ishowyou.R
import com.start3a.ishowyou.YoutubeVideoSelectionActivity
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.data.RoomRequest
import com.start3a.ishowyou.main.MainViewModel
import com.start3a.ishowyou.main.joinroom.JoinChatRoomActivity
import com.start3a.ishowyou.room.ChatRoomActivity
import kotlinx.android.synthetic.main.fragment_no_room.*

class NoRoomFragment : Fragment() {

    private var viewModel: MainViewModel? = null

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
                .get(MainViewModel::class.java)
        }

        viewModel!!.let { vm ->
            btnCreateRoom.setOnClickListener {
                createRoom()
            }

            btnJoinRoom.setOnClickListener {
                joinRoom()
            }
        }
    }

    private fun createRoom() {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_input_button, null)
        val editInput = view.findViewById<EditText>(R.id.editInput).apply {
            val curUserId = FirebaseAuth.getInstance().currentUser!!.email!!.split("@")[0]
            hint = "${curUserId}님의 방"
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

    private fun joinRoom() {
        val intent = Intent(activity, JoinChatRoomActivity::class.java)
        startActivity(intent)
    }

    private val requestActivityForVideoSelection: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            // 비디오 리스트
            if (activityResult.resultCode == Activity.RESULT_OK && activityResult.data != null) {
                activityResult.data!!.extras!!.getParcelableArrayList<YoutubeSearchData>("videos")?.let { videos ->
                    val intent = Intent(requireContext(), ChatRoomActivity::class.java).apply {
                        putExtra("requestcode", RoomRequest.CREATE_ROOM.num)
                        putExtra("title", viewModel!!.titleTemp)
                        putExtra("videos", videos)
                    }
                    startActivity(intent)

                }
            }
            else Toast.makeText(requireContext(), "적어도 1개 이상의 영상이 필요합니다.", Toast.LENGTH_LONG).show()
        }
}