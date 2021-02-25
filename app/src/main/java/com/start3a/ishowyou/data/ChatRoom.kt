package com.start3a.ishowyou.data

import java.util.*

data class ChatRoom(
    var title: String = "",
    var contentName: String = "NONE",
    var countMember: Int = 1,
    var timeCreated: Long = Date().time
)

data class ChatMessage(
    var userName: String = "",
    var content: String = "",
    var timeStamp: Long = Date().time
)

data class ChatMember(
    var userName: String = "",
    var isHost: Boolean = false
)

enum class RoomRequest(val num: Int) {
    CREATE_ROOM(0),
    JOIN_ROOM(1)
}