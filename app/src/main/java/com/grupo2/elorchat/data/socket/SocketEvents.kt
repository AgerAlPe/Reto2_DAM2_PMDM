package com.grupo2.elorchat.data.socket

enum class SocketEvents(val value: String) {
    ON_MESSAGE_RECEIVED("chat message"),
    ON_SEND_MESSAGE("chat message"),
    ON_CONNECT("connect"),
    ON_DISCONNECT("disconnect"),
}
