# Shield VPN (Android, Kotlin)

A fully-featured reference Android VPN client built with Kotlin, Jetpack Compose, and the Android `VpnService` API. It demonstrates how to collect VPN credentials, request the user permission handshake, run a foreground service that establishes a TUN interface, and keep the UI in sync via a dedicated view model.

> **Notice:** This sample does **not** connect to a real VPN server. The `LocalVpnService` sets up a virtual interface and foreground notification so you can extend it with your own tunnel implementation (OpenVPN, WireGuard, IPSec, etc.).

## Project structure

```
.
├── app/                     # Android application module
│   ├── src/main/kotlin/
│   │   ├── com/example/vpn/MainActivity.kt
│   │   ├── com/example/vpn/core/LocalVpnManager.kt
│   │   ├── com/example/vpn/model/VpnModels.kt
│   │   ├── com/example/vpn/service/LocalVpnService.kt
│   │   └── com/example/vpn/ui/… (Compose UI + ViewModel + theme)
│   └── src/main/res/        # Manifest, strings, vector icons, etc.
├── build.gradle.kts         # Root Gradle configuration
├── settings.gradle.kts
└── gradle/                  # Gradle wrapper + version catalog
```

Key components:

- **`MainActivity` + `VpnScreen`** – Compose UI for the connection form, state indicator, and event log.
- **`VpnViewModel`** – Holds form values, streams state updates, and emits permission requests to the Activity.
- **`LocalVpnManager`** – Thin controller that prepares permissions and starts/stops the foreground `VpnService`.
- **`LocalVpnService`** – Extends `VpnService`, provisions the TUN interface, and posts the persistent notification required for VPNs.
- **`VpnConfig` / `VpnRoute`** – Parcelable models sent to the service via intent extras.

## Getting started

1. **Android Studio / Gradle**
   - Open the project directory in Android Studio Iguana (or newer) with the Android Gradle Plugin 8.5+.
   - Allow the IDE to download the matching Gradle wrapper (8.7) and Android SDK platforms (API 35 recommended).

2. **Build & run**
   - Select an Android 7.0 (API 24) or newer device/emulator.
   - Click *Run* to install the app.
   - Tap **Connect** in the UI. Android will prompt for VPN permission; accept it to let the sample create a VPN session.

3. **Customize**
   - Change the defaults shown on the main screen or edit `VpnConfig` to hard-code your server, DNS routes, or MTU.
   - Extend `LocalVpnService.startTunnel` with real networking (sockets, TLS, WireGuard libraries, etc.).

## Building with Gradle (CLI)

```sh
./gradlew assembleDebug
```

Artifacts land in `app/build/outputs/apk/`.

## Extending the VPN tunnel

The service currently establishes a TUN interface and stops there. To connect to a real server:

- Implement your protocol inside `LocalVpnService.startTunnel` (open sockets, encrypt traffic, read/write the file descriptor).
- Protect control sockets with `protect(socket)` so they bypass the VPN tunnel.
- Replace the stub `LocalVpnManager` state updates with broadcasts or `WorkManager` to reflect real connection progress.

## Testing

- `app/src/test` contains a placeholder unit test; replace it with logic verifying your config builders or repositories.
- `app/src/androidTest` shows the standard instrumentation test shell.

## License

This project is provided as-is, no warranty. Use it as a starting point for your own VPN implementation and ensure you comply with all platform and regulatory requirements before distributing your app.
