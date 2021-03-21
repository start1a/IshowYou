package com.start3a.ishowyou.room.content.videoselection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.start3a.ishowyou.ItemViewHolder
import com.start3a.ishowyou.R
import com.start3a.ishowyou.model.VideoSearchHistory
import kotlinx.android.synthetic.main.item_current_search_keyword.view.*

class SearchHistoryAdapter(val list: MutableList<VideoSearchHistory>) :
    RecyclerView.Adapter<ItemViewHolder>() {

    lateinit var viewClicked: (Int) -> Unit
    lateinit var pasteButtonClicked: (Int) -> Unit

    private val limitItem = 20

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_current_search_keyword, parent, false)
        view.setOnClickListener {
            val pos = it.tag as Int
            viewClicked(pos)
        }
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (list.count() > limitItem) limitItem
        else list.count()
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.containerView.let {
            it.textKeyword.text = list[position].keyword

            it.image_search_paste.setOnClickListener {
                pasteButtonClicked(position)
            }

            it.tag = position
        }
    }
}