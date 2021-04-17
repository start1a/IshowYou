package com.start3a.ishowyou.room.chat

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.start3a.ishowyou.ItemViewHolder
import com.start3a.ishowyou.R
import com.start3a.ishowyou.data.ChatMember
import kotlinx.android.synthetic.main.item_chat_member.view.*

class ChatMemberAdapter(val list: MutableList<ChatMember>)
    : RecyclerView.Adapter<ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_chat_member, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.containerView.let {
            it.textMemberName.text = list[position].userName
            if (list[position].isHost) {
                it.textMemberName.append("  (방장)")
                it.textMemberName.setTextColor(Color.parseColor("#6200EE"))
                it.image_chatroom_member.setImageResource(R.drawable.ic_baseline_person_host_24)
            }
            else {
                it.textMemberName.setTextColor(Color.parseColor("#99000000"))
                it.image_chatroom_member.setImageResource(R.drawable.ic_baseline_person_24)
            }
        }
    }
}