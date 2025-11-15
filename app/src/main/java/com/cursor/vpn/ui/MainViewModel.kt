package com.cursor.vpn.ui

import androidx.lifecycle.ViewModel
import com.cursor.vpn.core.ConnectionStatus
import com.cursor.vpn.data.ServerProfile
import com.cursor.vpn.data.ServerRepository
import com.cursor.vpn.data.VpnConfig
import com.cursor.vpn.data.VpnLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {

    private val servers: List<ServerProfile> = ServerRepository.demoServers()

    private val _uiState = MutableStateFlow(
        VpnUiState(
            status = ConnectionStatus.DISCONNECTED,
            selectedProfile = servers.firstOrNull()
        )
    )
    val uiState: StateFlow<VpnUiState> = _uiState.asStateFlow()

    fun listServers(): List<ServerProfile> = servers

    fun selectServer(position: Int) {
        val safeProfile = servers.getOrNull(position) ?: return
        _uiState.update { it.copy(selectedProfile = safeProfile) }
    }

    fun updateStatus(status: ConnectionStatus) {
        _uiState.update { it.copy(status = status, lastError = if (status == ConnectionStatus.ERROR) it.lastError else null) }
    }

    fun pushError(message: String) {
        addLog("Error: $message")
        _uiState.update { it.copy(status = ConnectionStatus.ERROR, lastError = message) }
    }

    fun addLog(message: String) {
        val logEntry = VpnLog(System.currentTimeMillis(), message)
        _uiState.update { current ->
            val nextLogs = (current.logs + logEntry).takeLast(50)
            current.copy(logs = nextLogs)
        }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
    }

    fun buildVpnConfig(): VpnConfig? {
        val profile = _uiState.value.selectedProfile ?: return null
        return VpnConfig(
            sessionName = profile.name,
            serverHost = profile.host,
            serverPort = profile.port,
            dnsServer = profile.dns,
            virtualAddress = profile.virtualAddress
        )
    }
}
