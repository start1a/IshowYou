package com.start3a.ishowyou

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import kotlinx.android.synthetic.main.item_video_selected.view.*

class YoutubeVideoSelectedListAdapter(val list: MutableList<YoutubeSearchData>):
    RecyclerView.Adapter<ItemViewHolder>() {

    lateinit var videoClicked: (Int) -> Unit
    lateinit var videoDeleted: (Int) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_selected, parent, false)
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
            it.text_title_video_selected.text = list[position].title

            Glide.with(it)
                .load(list[position].thumbnailSmall)
                .error(R.drawable.ic_baseline_search_24)
                .into(it.image_thumbnail_video_selected)

            it.button_remove_video_selected.setOnClickListener {
                videoDeleted(position)
            }

            it.tag = position
        }
    }
}