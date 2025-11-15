package com.cursor.vpn.ui

import com.cursor.vpn.core.ConnectionStatus
import com.cursor.vpn.data.ServerProfile
import com.cursor.vpn.data.VpnLog

data class VpnUiState(
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val selectedProfile: ServerProfile? = null,
    val logs: List<VpnLog> = emptyList(),
    val lastError: String? = null
)
