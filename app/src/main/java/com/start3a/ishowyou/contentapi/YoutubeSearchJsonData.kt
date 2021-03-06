package com.start3a.ishowyou.contentapi

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import java.util.*

// 리스트 아이템용 Object
// parcelable : intent Arraylist로 넘김
@Keep
class YoutubeSearchData() : Parcelable {
    lateinit var title: String
    lateinit var desc: String
    lateinit var channelTitle: String
    lateinit var videoId: String
    lateinit var thumbnail: String
    lateinit var thumbnailSmall: String
    var duration = -1f
    var createdTime = Date().time

    constructor(parcel: Parcel) : this() {
        title = parcel.readString()!!
        desc = parcel.readString()!!
        channelTitle = parcel.readString()!!
        videoId = parcel.readString()!!
        thumbnail = parcel.readString()!!
        thumbnailSmall = parcel.readString()!!
        duration = parcel.readFloat()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(desc)
        parcel.writeString(channelTitle)
        parcel.writeString(videoId)
        parcel.writeString(thumbnail)
        parcel.writeString(thumbnailSmall)
        parcel.writeFloat(duration)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<YoutubeSearchData> {
        override fun createFromParcel(parcel: Parcel): YoutubeSearchData {
            return YoutubeSearchData(parcel)
        }

        override fun newArray(size: Int): Array<YoutubeSearchData?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return videoId
    }

    override fun equals(other: Any?): Boolean = this.toString() == other.toString()
}

// 현재 재생 영상 및 위치 요청
@Keep
data class PlayStateRequested(
    var curVideo: YoutubeSearchData,
    var seekbar: Float
)

// 비디오 선택 리스트 정보
@Keep
data class VideoSelectInfo(
    var keyword: String,
    // 검색한 비디오에서의 위치
    var indexToSearchList: Int
)

// Json -> Object
@Keep
data class YoutubeSearchJsonData(
    var regionCode: String = "",
    var kind: String = "",
    var nextPageToken: String = "",
    var pageInfo: PageInfo,
    var etag: String = "",
    var items: List<Items>
)

@Keep
data class Items(
    var snippet: Snippet,
    var kind: String = "",
    var etag: String = "",
    var id: Id
)

@Keep
data class Id(
    var kind: String = "",
    var videoId: String = ""
)

@Keep
data class Snippet(
    var publishTime: String = "",
    var publishedAt: String = "",
    var description: String = "",
    var title: String = "",
    var thumbnails: Thumbnails,
    var channelId: String = "",
    var channelTitle: String = "",
    var liveBroadcastContent: String = "",
)

@Keep
data class PageInfo(
    var totalResults: String = "",
    var resultsPerPage: Int = 0
)

@Keep
data class Thumbnails(
    var default: Default,
    var medium: Medium,
    var high: High
)

@Keep
data class Default(
    var width: Int = 0,
    var url: String = "",
    var height: Int = 0
)

@Keep
data class Medium(
    var width: Int = 0,
    var url: String = "",
    var height: Int = 0
)

@Keep
data class High(
    var width: Int = 0,
    var url: String = "",
    var height: Int = 0
)