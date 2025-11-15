package com.cursor.vpn.tunnel

import android.os.SystemClock
import android.os.ParcelFileDescriptor
import com.cursor.vpn.data.VpnConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.io.FileInputStream
import java.io.FileOutputStream

class VpnTunnelRunner(
    private val config: VpnConfig,
    private val logger: (String) -> Unit
) {

    companion object {
        private const val MAX_PACKET_SIZE = 32767
    }

    suspend fun run(descriptor: ParcelFileDescriptor) {
        withContext(Dispatchers.IO) {
            FileInputStream(descriptor.fileDescriptor).use { input ->
                FileOutputStream(descriptor.fileDescriptor).use { output ->
                    val buffer = ByteArray(MAX_PACKET_SIZE)
                    var lastKeepAlive = 0L
                    while (isActive) {
                        val available = input.available()
                        if (available > 0) {
                            val length = input.read(buffer, 0, minOf(buffer.size, available))
                            if (length > 0) {
                                output.write(buffer, 0, length)
                                output.flush()
                                logger("Loopback packet (${length}B) via ${config.virtualAddress}")
                            }
                        } else {
                            delay(50)
                        }
                        val now = SystemClock.elapsedRealtime()
                        if (now - lastKeepAlive >= config.keepaliveSeconds * 1000L) {
                            logger("Keepalive ping -> ${config.serverHost}:${config.serverPort}")
                            lastKeepAlive = now
                        }
                    }
                }
            }
        }
    }
}
