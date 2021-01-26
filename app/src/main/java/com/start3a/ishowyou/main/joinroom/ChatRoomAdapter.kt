package com.start3a.ishowyou.main.joinroom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.start3a.ishowyou.ItemViewHolder
import com.start3a.ishowyou.R
import com.start3a.ishowyou.data.ChatRoom
import kotlinx.android.synthetic.main.item_chatroom.view.*

class ChatRoomAdapter(val list: MutableList<ChatRoom>)
    : RecyclerView.Adapter<ItemViewHolder>() {

    lateinit var roomClicked: (Int) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chatroom, parent, false)
        view.setOnClickListener {
            val pos = it.tag as Int
            roomClicked(pos)
        }
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.containerView.let {
            if (list[position].title.length >= 20)
                it.textTitle.text = "${list[position].title.substring(0..18)}.."
            else it.textTitle.text = list[position].title

            it.textContentName.text = list[position].contentName
            it.textUserCount.text = "${list[position].countMember}명"
            it.tag = position
        }
    }
}