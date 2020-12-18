package com.start3a.ishowyou.data

data class ChatRoom(
    var title: String = "",
    var contentName: String = "NONE",
    var countMember: Int = 1
)

data class ChatMessage(
    var userName: String = "",
    var content: String = "",
    var timeStamp: Long = 0
)

data class ChatMember(
    var userName: String = "",
    var isHost: Boolean = false
)