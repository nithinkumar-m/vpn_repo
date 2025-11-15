package com.vpnapp.secure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService as AndroidVpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var connectionButton: MaterialButton
    private lateinit var statusText: MaterialTextView
    private lateinit var statusCard: MaterialCardView
    private lateinit var serverText: MaterialTextView
    private lateinit var ipText: MaterialTextView
    private lateinit var durationText: MaterialTextView
    
    private var isConnected = false
    private var connectionStartTime = 0L
    
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startVpnService()
        } else {
            Toast.makeText(this, "VPN permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    
    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                VpnService.ACTION_VPN_STATUS -> {
                    val connected = intent.getBooleanExtra("connected", false)
                    updateConnectionStatus(connected)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeViews()
        setupClickListeners()
        updateUI()
        
        // Register broadcast receiver for VPN status updates
        val filter = IntentFilter(VpnService.ACTION_VPN_STATUS)
        registerReceiver(statusReceiver, filter, RECEIVER_EXPORTED)
        
        // Start timer for connection duration
        startConnectionTimer()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(statusReceiver)
    }
    
    private fun initializeViews() {
        connectionButton = findViewById(R.id.connectionButton)
        statusText = findViewById(R.id.statusText)
        statusCard = findViewById(R.id.statusCard)
        serverText = findViewById(R.id.serverText)
        ipText = findViewById(R.id.ipText)
        durationText = findViewById(R.id.durationText)
    }
    
    private fun setupClickListeners() {
        connectionButton.setOnClickListener {
            if (isConnected) {
                disconnectVpn()
            } else {
                connectVpn()
            }
        }
    }
    
    private fun connectVpn() {
        val intent = AndroidVpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startVpnService()
        }
    }
    
    private fun startVpnService() {
        val intent = Intent(this, VpnService::class.java).apply {
            action = VpnService.ACTION_CONNECT
        }
        ContextCompat.startForegroundService(this, intent)
        
        // Simulate connection delay
        lifecycleScope.launch {
            delay(1500)
            connectionStartTime = System.currentTimeMillis()
            updateConnectionStatus(true)
        }
    }
    
    private fun disconnectVpn() {
        val intent = Intent(this, VpnService::class.java).apply {
            action = VpnService.ACTION_DISCONNECT
        }
        startService(intent)
        
        updateConnectionStatus(false)
    }
    
    private fun updateConnectionStatus(connected: Boolean) {
        isConnected = connected
        updateUI()
    }
    
    private fun updateUI() {
        if (isConnected) {
            connectionButton.text = "Disconnect"
            connectionButton.setBackgroundColor(getColor(R.color.disconnect_red))
            statusText.text = "Connected"
            statusCard.setCardBackgroundColor(getColor(R.color.success_green))
            serverText.text = "Server: United States"
            ipText.text = "IP: 192.168.1.100"
        } else {
            connectionButton.text = "Connect"
            connectionButton.setBackgroundColor(getColor(R.color.connect_blue))
            statusText.text = "Disconnected"
            statusCard.setCardBackgroundColor(getColor(R.color.gray))
            serverText.text = "Server: Not Connected"
            ipText.text = "IP: --"
            durationText.text = "Duration: --"
            connectionStartTime = 0L
        }
    }
    
    private fun startConnectionTimer() {
        lifecycleScope.launch {
            while (isActive) {
                if (isConnected && connectionStartTime > 0) {
                    val duration = (System.currentTimeMillis() - connectionStartTime) / 1000
                    val hours = duration / 3600
                    val minutes = (duration % 3600) / 60
                    val seconds = duration % 60
                    durationText.text = String.format("Duration: %02d:%02d:%02d", hours, minutes, seconds)
                }
                delay(1000)
            }
        }
    }
}
