package com.example.vpn.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VpnRoute(
    val address: String,
    val prefixLength: Int
) : Parcelable

@Parcelize
data class VpnConfig(
    val sessionName: String = "Shield VPN",
    val serverAddress: String = "vpn.example.com",
    val serverPort: Int = 443,
    val sharedSecret: String = "demo-shared-secret",
    val username: String = "demo",
    val password: String = "demo",
    val mtu: Int = 1500,
    val localAddress: String = "10.0.0.2",
    val localPrefix: Int = 32,
    val dnsServers: List<String> = listOf("1.1.1.1", "8.8.8.8"),
    val routes: List<VpnRoute> = listOf(VpnRoute("0.0.0.0", 0))
) : Parcelable
