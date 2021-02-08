package com.start3a.ishowyou.contentapi

// 리스트 아이템용 Object
data class YoutubeSearchData(
    var title: String,
    var desc: String,
    var channelTitle: String,
    var thumbnail: String
)

// Json -> Object
data class YoutubeSearchJsonData(
    var regionCode: String = "",
    var kind: String = "",
    var nextPageToken: String = "",
    var pageInfo: PageInfo,
    var etag: String = "",
    var items: List<Items>
)

data class Items(
    var snippet: Snippet,
    var kind: String = "",
    var etag: String = "",
    var id: Id
)

data class Id(
    var kind: String = "",
    var videoId: String = ""
)

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

data class PageInfo(
    var totalResults: String = "",
    var resultsPerPage: Int = 0
)

data class Thumbnails(
    var default: Default,
    var medium: Medium,
    var high: High
)

data class Default(
    var width: Int = 0,
    var url: String = "",
    var height: Int = 0
)

data class Medium(
    var width: Int = 0,
    var url: String = "",
    var height: Int = 0
)

data class High(
    var width: Int = 0,
    var url: String = "",
    var height: Int = 0
)