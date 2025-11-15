# Secure VPN (Android / Kotlin)

A reference-quality Kotlin Android app that demonstrates how to build a VPN client powered by Android's `VpnService` APIs. The sample showcases a foreground VPN service, lifecycle-aware UI, and a lightweight tunnel loopback that keeps the interface alive for demo purposes.

## Features

- Foreground `VpnService` with notification channel and proper permission handling
- Reactive UI built with ViewBinding + `StateFlow`
- Configurable demo endpoints (`ServerRepository`) and log streaming from the service
- Parcelable VPN configuration passed to the service, with keepalive support
- Simple tunnel runner that echoes packets on the TUN interface while emitting keepalive logs

## Project Structure

- `app/src/main/java/com/cursor/vpn/ui` – Activity + ViewModel + UI state
- `app/src/main/java/com/cursor/vpn/service` – `SecureVpnService` entry point
- `app/src/main/java/com/cursor/vpn/tunnel` – `VpnTunnelRunner`
- `app/src/main/java/com/cursor/vpn/data` – immutable models & repositories
- `app/src/main/java/com/cursor/vpn/util` – notification helper

## Getting Started

```bash
./gradlew assembleDebug
```

Then install/run the generated APK (`app/build/outputs/apk/debug/app-debug.apk`). When the app launches:

1. Pick one of the demo endpoints in the spinner.
2. Tap **Connect** and grant the VPN permission prompt.
3. Observe connection state + logs. Tap **Disconnect** to stop the tunnel.

> ⚠️ The tunnel uses a loopback implementation intended for educational purposes. It does **not** forward traffic to a remote VPN server. Replace `VpnTunnelRunner` with your protocol implementation (OpenVPN, WireGuard, etc.) to hook into a real backend.

## Testing & Notes

- Unit tests can be added with `./gradlew test`.
- The project targets Android SDK 35 and requires API 26+ devices/emulators that support the system VPN feature.
- Real deployments must supply production-ready certificates, authentication, and packet routing logic.
