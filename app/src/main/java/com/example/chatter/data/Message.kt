package com.example.chatter.data

data class Message(
    var side: String,
    var msg: String,
    var prevMsgId: Int,
    var newMsgId: Int,
    var isFirst: Boolean
)