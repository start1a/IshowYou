package com.start3a.ishowyou.room.content

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.start3a.ishowyou.ItemViewHolder
import com.start3a.ishowyou.R
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import kotlinx.android.synthetic.main.item_video_playlist.view.*

class YoutubePlayListAdapter(val list: MutableList<YoutubeSearchData>):
    RecyclerView.Adapter<ItemViewHolder>() {

    lateinit var videoClicked: (Int) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_playlist, parent, false)
        view.setOnClickListener {
            val pos = it.tag as Int
            videoClicked(pos)
        }
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.containerView.let {
            it.textTitle_playlist.text = list[position].title
            it.textChannelTitle_playlist.text = list[position].channelTitle

            Glide.with(it)
                .load(list[position].thumbnailSmall)
                .error(R.drawable.ic_baseline_search_24)
                .into(it.imageThumbnail_playlist)

            it.tag = position
        }
    }
}