package com.start3a.ishowyou.room.content.videoselection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.start3a.ishowyou.ItemViewHolder
import com.start3a.ishowyou.R
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import kotlinx.android.synthetic.main.item_video_searched.view.*

class YoutubeVideoListAdapter(val list: MutableList<YoutubeSearchData>):
    RecyclerView.Adapter<ItemViewHolder>() {

    lateinit var videoClicked: (Int) -> Unit
    val selectionList = mutableListOf<Boolean>().apply {
        for (i in 0 until 50)
            add(false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_searched, parent, false)
        view.setOnClickListener {
            val pos = it.tag as Int
            videoClicked(pos)
            notifyItemChanged(pos)
        }
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.containerView.let {
            it.textTitle.text = list[position].title
            it.textDesc.text = list[position].desc

            it.textChannelTitle_playlist.text = list[position].channelTitle

            Glide.with(it)
                .load(list[position].thumbnail)
                .error(R.drawable.ic_baseline_search_24)
                .into(it.image_thumbnail)

            if (selectionList[position])
                it.image_checked.visibility = View.VISIBLE
            else it.image_checked.visibility = View.GONE


            it.tag = position
        }
    }
}