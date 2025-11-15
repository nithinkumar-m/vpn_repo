package com.example.vpn

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vpn.ui.VpnEvent
import com.example.vpn.ui.VpnScreen
import com.example.vpn.ui.VpnViewModel
import com.example.vpn.ui.theme.ShieldVpnTheme
import kotlinx.coroutines.flow.collect

class MainActivity : ComponentActivity() {
    private val viewModel: VpnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            viewModel.onPermissionResult(result.resultCode == Activity.RESULT_OK)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        is VpnEvent.PermissionRequired -> permissionLauncher.launch(event.intent)
                    }
                }
            }

            ShieldVpnTheme {
                VpnScreen(
                    uiState = uiState,
                    onServerChanged = viewModel::onServerAddressChanged,
                    onPortChanged = viewModel::onServerPortChanged,
                    onSecretChanged = viewModel::onSharedSecretChanged,
                    onCredentialsChanged = viewModel::onCredentialsChanged,
                    onConnect = viewModel::connect,
                    onDisconnect = viewModel::disconnect
                )
            }
        }
    }
}
