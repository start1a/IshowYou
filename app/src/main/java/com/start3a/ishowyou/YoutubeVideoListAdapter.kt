package com.start3a.ishowyou

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import kotlinx.android.synthetic.main.item_video_searched.view.*
import retrofit2.http.Url
import java.net.URL

class YoutubeVideoListAdapter(val list: MutableList<YoutubeSearchData>):
    RecyclerView.Adapter<ItemViewHolder>() {

    lateinit var videoClicked: (Int) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_searched, parent, false)
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
            it.textTitle.text = list[position].title

            val desc = list[position].desc
            if (list[position].desc.length > 20)
                it.textDesc.text = desc.substring(0..20)
            else
                it.textDesc.text = desc

            it.textChannelTitle.text = list[position].channelTitle

            Glide.with(it)
                .load(list[position].thumbnail)
                .error(R.drawable.ic_baseline_search_24)
                .into(it.image_thumbnail)

            it.tag = position
        }
    }
}