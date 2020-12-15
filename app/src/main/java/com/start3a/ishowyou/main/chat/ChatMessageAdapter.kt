package com.start3a.ishowyou.main.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.start3a.ishowyou.ItemViewHolder
import com.start3a.ishowyou.R
import com.start3a.ishowyou.data.ChatMessage
import kotlinx.android.synthetic.main.item_chat_message.view.*

class ChatMessageAdapter(val list: MutableList<ChatMessage>):
    RecyclerView.Adapter<ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.containerView.let {
            it.textUserName.text = list[position].userName
            it.textContent.text = list[position].content
        }
    }
}