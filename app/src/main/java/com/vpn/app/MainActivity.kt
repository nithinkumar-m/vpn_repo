package com.vpn.app

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.vpn.app.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var vpnService: VpnService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkVpnStatus()
    }

    private fun setupUI() {
        binding.connectButton.setOnClickListener {
            if (VpnService.isConnected()) {
                disconnectVpn()
            } else {
                connectVpn()
            }
        }

        binding.settingsButton.setOnClickListener {
            Toast.makeText(this, "Settings feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun connectVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST_CODE)
        } else {
            startVpnService()
        }
    }

    private fun disconnectVpn() {
        lifecycleScope.launch {
            VpnService.disconnect()
            updateUI(false)
        }
    }

    private fun startVpnService() {
        val intent = Intent(this, VpnService::class.java)
        intent.action = VpnService.ACTION_CONNECT
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun checkVpnStatus() {
        lifecycleScope.launch {
            val isConnected = VpnService.isConnected()
            updateUI(isConnected)
        }
    }

    private fun updateUI(isConnected: Boolean) {
        binding.apply {
            if (isConnected) {
                connectButton.text = getString(R.string.disconnect)
                statusText.text = getString(R.string.status_connected)
                statusIndicator.setBackgroundColor(
                    androidx.core.content.ContextCompat.getColor(this@MainActivity, R.color.connected)
                )
            } else {
                connectButton.text = getString(R.string.connect)
                statusText.text = getString(R.string.status_disconnected)
                statusIndicator.setBackgroundColor(
                    androidx.core.content.ContextCompat.getColor(this@MainActivity, R.color.disconnected)
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            startVpnService()
        } else if (requestCode == VPN_REQUEST_CODE) {
            Toast.makeText(this, "VPN permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        checkVpnStatus()
    }

    companion object {
        private const val VPN_REQUEST_CODE = 0x01
    }
}
