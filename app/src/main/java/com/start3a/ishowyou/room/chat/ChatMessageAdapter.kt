package com.start3a.ishowyou.room.chat

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.start3a.ishowyou.ItemViewHolder
import com.start3a.ishowyou.R
import com.start3a.ishowyou.data.ChatMessage
import com.start3a.ishowyou.data.CurUser
import kotlinx.android.synthetic.main.custom_player_ui.view.*
import kotlinx.android.synthetic.main.item_chat_message_left.view.*
import kotlinx.android.synthetic.main.item_chat_message_right.view.*

class ChatMessageAdapter(val list: MutableList<ChatMessage>, private val context: Context):
    RecyclerView.Adapter<ItemViewHolder>() {

    var isFullScreen = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view =
            if (viewType == 0)
                LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message_right, parent, false)
        else LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message_left, parent, false)
        view.tag = viewType
        return ItemViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].userName == CurUser.userName) 0
        else 1
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.containerView.let {
            // 내 메세지
            if (it.tag == 0) {
                it.textMyMessage.text = list[position].content
            }

            // 상대 메세지
            else {
                it.textYourMessage.text = list[position].content

                if (!equalsToPrevMessageUser(position)) {
                    it.textYourName.text = list[position].userName
                    it.textYourName.visibility = View.VISIBLE
                }
                else it.textYourName.visibility = View.GONE

                if (isFullScreen) {
                    it.textYourName.setTextColor(Color.WHITE)
                    it.textYourMessage.setTextColor(Color.WHITE)
                    it.textYourMessage.background = ContextCompat.getDrawable(context, R.drawable.chat_bubble_left_landscape)
                }
                else {
                    it.textYourName.setTextColor(Color.BLACK)
                    it.textYourMessage.setTextColor(Color.BLACK)
                    it.textYourMessage.background = ContextCompat.getDrawable(context, R.drawable.chat_bubble_left_portrait)
                }
            }
        }
    }

    private fun equalsToPrevMessageUser(position: Int): Boolean {
        Log.d("TAGG", "position: $position")
        if (position != 0)
            Log.d("TAGG", "top: ${list[position - 1].userName}, bottom: ${list[position].userName}")
        return position != 0 && list[position].userName == list[position - 1].userName
    }
}