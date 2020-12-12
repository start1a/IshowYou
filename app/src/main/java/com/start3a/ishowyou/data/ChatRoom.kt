package com.start3a.ishowyou.data

data class ChatRoom(
    var title: String,
    var contentName: String = "NONE",
    var countMember: Int = 1
)