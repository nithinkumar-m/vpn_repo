package com.example.vpn.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.vpn.core.VpnStatus

@Composable
fun VpnScreen(
    modifier: Modifier = Modifier,
    uiState: VpnUiState,
    onServerChanged: (String) -> Unit,
    onPortChanged: (String) -> Unit,
    onSecretChanged: (String) -> Unit,
    onCredentialsChanged: (String, String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header(uiState)
            ConnectionForm(
                uiState = uiState,
                onServerChanged = onServerChanged,
                onPortChanged = onPortChanged,
                onSecretChanged = onSecretChanged,
                onCredentialsChanged = onCredentialsChanged
            )
            ActionButtons(
                status = uiState.status,
                onConnect = onConnect,
                onDisconnect = onDisconnect
            )
            LogCard(uiState)
        }
    }
}

@Composable
private fun Header(uiState: VpnUiState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.VpnLock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = "Shield VPN", style = MaterialTheme.typography.titleLarge)
            Text(
                text = when (uiState.status) {
                    VpnStatus.CONNECTED -> "Tunnel active"
                    VpnStatus.CONNECTING -> "Connecting…"
                    VpnStatus.DISCONNECTING -> "Disconnecting…"
                    VpnStatus.ERROR -> uiState.lastError ?: "Error"
                    VpnStatus.PERMISSION_REQUIRED -> "Waiting for permission"
                    else -> "Disconnected"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConnectionForm(
    uiState: VpnUiState,
    onServerChanged: (String) -> Unit,
    onPortChanged: (String) -> Unit,
    onSecretChanged: (String) -> Unit,
    onCredentialsChanged: (String, String) -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.form.serverAddress,
                onValueChange = onServerChanged,
                label = { Text("Server address") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.form.serverPort,
                onValueChange = onPortChanged,
                label = { Text("Server port") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.form.sharedSecret,
                onValueChange = onSecretChanged,
                label = { Text("Shared secret") },
                trailingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                singleLine = true
            )
            CredentialRow(uiState, onCredentialsChanged)
        }
    }
}

@Composable
private fun CredentialRow(uiState: VpnUiState, onCredentialsChanged: (String, String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.form.username,
            onValueChange = { onCredentialsChanged(it, uiState.form.password) },
            label = { Text("Username") },
            singleLine = true
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.form.password,
            onValueChange = { onCredentialsChanged(uiState.form.username, it) },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
    }
}

@Composable
private fun ActionButtons(
    status: VpnStatus,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Connection", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val connectEnabled = status == VpnStatus.DISCONNECTED || status == VpnStatus.ERROR
                Button(onClick = onConnect, enabled = connectEnabled) {
                    Text("Connect")
                }
                val disconnectEnabled = status == VpnStatus.CONNECTED || status == VpnStatus.CONNECTING || status == VpnStatus.PERMISSION_REQUIRED
                TextButton(onClick = onDisconnect, enabled = disconnectEnabled) {
                    Text("Disconnect")
                }
            }
        }
    }
}

@Composable
private fun LogCard(uiState: VpnUiState) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Event log", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.logs.isEmpty()) {
                Text(text = "No events yet", style = MaterialTheme.typography.bodyMedium)
            } else {
                uiState.logs.forEach { log ->
                    Text(text = "• $log", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
