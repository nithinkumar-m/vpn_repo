package com.cursor.vpn.data

data class ServerProfile(
    val name: String,
    val host: String,
    val port: Int,
    val dns: String,
    val virtualAddress: String
)
