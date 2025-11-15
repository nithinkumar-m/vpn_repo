package com.cursor.vpn.data

object ServerRepository {
    fun demoServers(): List<ServerProfile> = listOf(
        ServerProfile(
            name = "Demo Edge - Frankfurt",
            host = "198.51.100.10",
            port = 1194,
            dns = "1.1.1.1",
            virtualAddress = "10.0.0.2"
        ),
        ServerProfile(
            name = "Demo Edge - Singapore",
            host = "203.0.113.8",
            port = 443,
            dns = "8.8.8.8",
            virtualAddress = "10.8.0.2"
        ),
        ServerProfile(
            name = "Demo Edge - São Paulo",
            host = "192.0.2.24",
            port = 8443,
            dns = "9.9.9.9",
            virtualAddress = "10.16.0.2"
        )
    )
}
