package com.start3a.ishowyou.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.start3a.ishowyou.R
import com.start3a.ishowyou.main.joinroom.JoinChatRoomActivity
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

        viewModel = activity!!.application!!.let {
            ViewModelProvider(
                activity!!.viewModelStore,
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

            viewModel!!.createChatRoom(title)
            dialog.dismiss()
        }
    }

    private fun joinRoom() {
        val intent = Intent(activity, JoinChatRoomActivity::class.java)
        startActivity(intent)
    }
}