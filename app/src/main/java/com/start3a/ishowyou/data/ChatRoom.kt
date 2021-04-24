package com.start3a.ishowyou.data

import androidx.annotation.Keep
import com.google.firebase.database.ServerValue
import java.util.*

@Keep
data class ChatRoom(
    var id: String = "",
    var title: String = "",
    var contentName: String = "NONE",
    var countMember: Int = 1,
    var timeCreated: Long = Date().time
)

@Keep
data class ChatMessage(
    var userName: String = "",
    var content: String = "",
    var timeStamp: Long = Date().time
)

// Server Time 적용
@Keep
data class ChatMessageForSend(
    var userName: String = "",
    var content: String = "",
    var timeStamp: Map<String, String> = ServerValue.TIMESTAMP
)

@Keep
data class ChatMember(
    var userName: String = "",
    var isHost: Boolean = false
)