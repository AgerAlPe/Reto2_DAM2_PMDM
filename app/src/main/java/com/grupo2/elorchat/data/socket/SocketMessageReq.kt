package com.grupo2.elorchat.data.socket

data class SocketMessageReq(
    val room: String,
    val message: String
)