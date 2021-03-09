package com.start3a.ishowyou.room.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.start3a.ishowyou.ItemViewHolder
import com.start3a.ishowyou.R
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import kotlinx.android.synthetic.main.item_video_playlist.view.*
import kotlinx.android.synthetic.main.layout_fragment_youtube_content_edit_header.view.*

class YoutubePlayListAdapter(
    val list: MutableList<YoutubeSearchData>,
    var videoPlayed: YoutubeSearchData?,
    val isHost: Boolean
) :
    RecyclerView.Adapter<ItemViewHolder>() {

    lateinit var videoClicked: (Int) -> Unit
    lateinit var videoRemoved: (Int) -> Unit
    lateinit var videoAdd: () -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view =
            if (viewType == 1) {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_video_playlist, parent, false).apply {
                        setOnClickListener {
                            val pos = it.tag as Int
                            videoClicked(pos)
                        }
                    }
            } else
                // 첫번째 아이템 : 방 정보 표시
                // 스크롤 시 헤더 전체가 스크롤 되도록 recyclerview 아이템에 넣음
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_fragment_youtube_content_edit_header, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0
        else 1
    }

    override fun getItemCount(): Int {
        return list.count() + 1
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.containerView.let {
            // 플레이리스트
            if (position > 0) {
                it.textTitle_playlist.text = list[position - 1].title
                it.textChannelTitle_playlist.text = list[position - 1].channelTitle

                Glide.with(it)
                    .load(list[position - 1].thumbnailSmall)
                    .error(R.drawable.ic_baseline_search_24)
                    .into(it.imageThumbnail_playlist)

                if (!isHost)
                    it.btnRemoveVideo.visibility = View.GONE
                else
                    it.btnRemoveVideo.setOnClickListener { videoRemoved(position - 1) }

                it.tag = position - 1
            }

            // 영상 정보
            else {
                videoPlayed?.let { video ->
                    it.textVideoTitle.text = video.title
                    it.textVideoDesc.text = video.desc
                    it.textVideoChannelTitle.text = video.channelTitle

                    if (!isHost)
                        it.btnAddVideo.visibility = View.GONE
                    else
                        it.btnAddVideo.setOnClickListener { videoAdd() }
                }
            }
        }
    }
}