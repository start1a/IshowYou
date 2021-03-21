package com.start3a.ishowyou.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class RoomData_VideoSearch(
    var title: String,
    var desc: String,
    var channelTitle: String,
    var videoId: String,
    var thumbnail: String,
    var thumbnailSmall: String,
    var keyword: String,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var timeCreated = Date().time
}

@Entity
data class VideoSearchHistory(
    @PrimaryKey
    val keyword: String,
    var createdTime: Long
) {
    companion object {
        val maxItem = 20
    }
}