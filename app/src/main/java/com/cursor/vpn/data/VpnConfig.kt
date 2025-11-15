package com.cursor.vpn.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VpnConfig(
    val sessionName: String,
    val serverHost: String,
    val serverPort: Int,
    val dnsServer: String,
    val virtualAddress: String,
    val mtu: Int = 1500,
    val keepaliveSeconds: Int = 15
) : Parcelable
