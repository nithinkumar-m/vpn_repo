package com.cursor.vpn.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cursor.vpn.R
import com.cursor.vpn.core.ConnectionStatus
import com.cursor.vpn.core.VpnContract
import com.cursor.vpn.data.ServerProfile
import com.cursor.vpn.databinding.ActivityMainBinding
import com.cursor.vpn.service.SecureVpnService
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                startVpnIfPossible()
            } else {
                viewModel.pushError(getString(R.string.vpn_request_permission))
            }
        }

    private val vpnStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val statusString = intent.getStringExtra(VpnContract.EXTRA_STATUS)
            val status = statusString?.let { runCatching { ConnectionStatus.valueOf(it) }.getOrNull() }
            if (status != null) {
                viewModel.updateStatus(status)
            }
            intent.getStringExtra(VpnContract.EXTRA_LOG)?.let { viewModel.addLog(it) }
            intent.getStringExtra(VpnContract.EXTRA_ERROR)?.let { viewModel.pushError(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupSpinner()
        setupListeners()
        observeState()
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(VpnContract.ACTION_VPN_STATUS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(vpnStatusReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(vpnStatusReceiver, filter)
        }
    }

    override fun onStop() {
        unregisterReceiver(vpnStatusReceiver)
        super.onStop()
    }

    private fun setupSpinner() {
        val servers = viewModel.listServers()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            servers.map { formatServerLabel(it) }
        )
        binding.serverSpinner.adapter = adapter
        binding.serverSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.selectServer(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun setupListeners() {
        binding.connectButton.setOnClickListener { handleConnectAction() }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { renderState(it) }
            }
        }
    }

    private fun renderState(state: VpnUiState) {
        binding.statusLabel.text = when (state.status) {
            ConnectionStatus.DISCONNECTED -> getString(R.string.vpn_status_disconnected)
            ConnectionStatus.CONNECTING -> getString(R.string.vpn_status_connecting)
            ConnectionStatus.CONNECTED -> getString(R.string.vpn_status_connected)
            ConnectionStatus.ERROR -> state.lastError ?: getString(R.string.vpn_status_error)
        }
        binding.connectButton.text = if (state.status == ConnectionStatus.CONNECTED) {
            getString(R.string.vpn_disconnect)
        } else {
            getString(R.string.vpn_connect)
        }
        binding.connectButton.isEnabled = state.status != ConnectionStatus.CONNECTING
        binding.logText.text = state.logs.joinToString("\n") { it.render() }
        binding.logScroll.post { binding.logScroll.fullScroll(View.FOCUS_DOWN) }
    }

    private fun handleConnectAction() {
        when (viewModel.uiState.value.status) {
            ConnectionStatus.CONNECTED -> SecureVpnService.stop(this)
            ConnectionStatus.CONNECTING -> Unit
            ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> ensurePermission()
        }
    }

    private fun ensurePermission() {
        val prepareIntent = VpnService.prepare(this)
        if (prepareIntent != null) {
            permissionLauncher.launch(prepareIntent)
        } else {
            startVpnIfPossible()
        }
    }

    private fun startVpnIfPossible() {
        val config = viewModel.buildVpnConfig()
        if (config == null) {
            viewModel.pushError("No server selected")
            return
        }
        viewModel.updateStatus(ConnectionStatus.CONNECTING)
        viewModel.addLog("Requesting connection to ${config.serverHost}:${config.serverPort}")
        SecureVpnService.start(this, config)
    }

    private fun formatServerLabel(profile: ServerProfile): String =
        getString(R.string.vpn_server_description, profile.name, profile.host, profile.port)
}
