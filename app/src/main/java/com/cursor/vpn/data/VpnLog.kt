package com.cursor.vpn.data

data class VpnLog(
    val timestampMillis: Long,
    val message: String
) {
    fun render(): String = String.format("[%tT] %s", timestampMillis, message)
}
