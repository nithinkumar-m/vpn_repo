package com.vpnapp

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.vpnapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var vpnManager: VpnManager
    private lateinit var viewModel: VpnViewModel
    
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            vpnManager.connect()
        } else {
            Toast.makeText(this, "VPN permission denied", Toast.LENGTH_SHORT).show()
            viewModel.updateState(VpnManager.VpnState.DISCONNECTED)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        vpnManager = VpnManager(this)
        viewModel = ViewModelProvider(this)[VpnViewModel::class.java]
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        binding.connectButton.setOnClickListener {
            when (viewModel.connectionState.value) {
                VpnManager.VpnState.DISCONNECTED -> connectVpn()
                VpnManager.VpnState.CONNECTED -> disconnectVpn()
                else -> { /* Do nothing */ }
            }
        }
    }
    
    private fun observeViewModel() {
        vpnManager.connectionState.observe(this) { state ->
            viewModel.updateState(state)
            updateUI(state)
        }
    }
    
    private fun connectVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            vpnManager.connect()
        }
    }
    
    private fun disconnectVpn() {
        vpnManager.disconnect()
    }
    
    private fun updateUI(state: VpnManager.VpnState) {
        when (state) {
            VpnManager.VpnState.DISCONNECTED -> {
                binding.connectButton.text = "Connect VPN"
                binding.statusText.text = "Disconnected"
                binding.statusIndicator.setBackgroundColor(
                    getColor(android.R.color.holo_red_dark)
                )
                binding.progressBar.visibility = View.GONE
            }
            VpnManager.VpnState.CONNECTING -> {
                binding.connectButton.text = "Connecting..."
                binding.statusText.text = "Connecting..."
                binding.statusIndicator.setBackgroundColor(
                    getColor(android.R.color.holo_orange_dark)
                )
                binding.progressBar.visibility = View.VISIBLE
            }
            VpnManager.VpnState.CONNECTED -> {
                binding.connectButton.text = "Disconnect VPN"
                binding.statusText.text = "Connected"
                binding.statusIndicator.setBackgroundColor(
                    getColor(android.R.color.holo_green_dark)
                )
                binding.progressBar.visibility = View.GONE
            }
            VpnManager.VpnState.DISCONNECTING -> {
                binding.connectButton.text = "Disconnecting..."
                binding.statusText.text = "Disconnecting..."
                binding.statusIndicator.setBackgroundColor(
                    getColor(android.R.color.holo_orange_dark)
                )
                binding.progressBar.visibility = View.VISIBLE
            }
            VpnManager.VpnState.ERROR -> {
                binding.connectButton.text = "Connect VPN"
                binding.statusText.text = "Error"
                binding.statusIndicator.setBackgroundColor(
                    getColor(android.R.color.holo_red_dark)
                )
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to connect VPN", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
