# Secure VPN - Android App

A complete, functional VPN application for Android built with Kotlin.

## Features

- ✅ **Secure VPN Connection**: Establishes a VPN tunnel to protect your internet traffic
- ✅ **Modern Material Design UI**: Beautiful and intuitive user interface
- ✅ **Real-time Connection Status**: Live updates on connection status and duration
- ✅ **Foreground Service**: Keeps VPN running in the background
- ✅ **Connection Statistics**: Track server, IP address, and connection duration
- ✅ **Easy Connect/Disconnect**: One-tap connection control

## Project Structure

```
VPN App/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/vpnapp/secure/
│   │       │   ├── MainActivity.kt          # Main UI Activity
│   │       │   └── VpnService.kt           # VPN Service Implementation
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml   # Main UI Layout
│   │       │   ├── values/
│   │       │   │   ├── strings.xml
│   │       │   │   ├── colors.xml
│   │       │   │   └── themes.xml
│   │       │   ├── drawable/
│   │       │   │   ├── ic_vpn.xml
│   │       │   │   └── ic_check.xml
│   │       │   └── xml/
│   │       │       ├── backup_rules.xml
│   │       │       └── data_extraction_rules.xml
│   │       └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## Requirements

- **Android Studio**: Arctic Fox or newer
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Kotlin**: 1.9.0+
- **Gradle**: 8.1.0+

## Setup Instructions

### 1. Open in Android Studio

1. Open Android Studio
2. Click "Open an Existing Project"
3. Navigate to this project directory
4. Wait for Gradle sync to complete

### 2. Build the Project

```bash
./gradlew clean build
```

### 3. Run on Device/Emulator

1. Connect an Android device or start an emulator
2. Click the "Run" button in Android Studio
3. Select your target device

## Key Components

### MainActivity.kt

The main activity that provides the user interface:
- Connection button (Connect/Disconnect)
- Status card showing connection state
- Information card displaying server, IP, and duration
- Real-time connection timer
- Broadcast receiver for VPN status updates

### VpnService.kt

The VPN service that handles:
- VPN tunnel establishment
- Packet routing and forwarding
- Foreground notification
- Connection lifecycle management
- DNS configuration (8.8.8.8, 8.8.4.4)

## Permissions

The app requires the following permissions:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
```

## How It Works

1. **VPN Permission**: User grants VPN permission on first connect
2. **Tunnel Creation**: VPN interface is established with virtual IP (10.0.0.2)
3. **Route Configuration**: All traffic (0.0.0.0/0) is routed through the VPN
4. **DNS Setup**: Google DNS servers are configured
5. **Packet Processing**: IP packets are intercepted and processed
6. **Foreground Service**: Connection runs as a foreground service with notification

## Configuration

### VPN Settings (in VpnService.kt)

```kotlin
builder.setSession("SecureVPN")
    .addAddress("10.0.0.2", 24)        // VPN interface IP
    .addRoute("0.0.0.0", 0)           // Route all traffic
    .addDnsServer("8.8.8.8")          // Primary DNS
    .addDnsServer("8.8.4.4")          // Secondary DNS
    .setMtu(1500)                      // Maximum Transmission Unit
```

## Customization

### Change App Name
Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="app_name">Your VPN Name</string>
```

### Change Colors
Edit `app/src/main/res/values/colors.xml`:
```xml
<color name="primary">#YOUR_COLOR</color>
<color name="connect_blue">#YOUR_COLOR</color>
```

### Change Package Name
1. Update `namespace` in `app/build.gradle`
2. Update `applicationId` in `app/build.gradle`
3. Refactor package in Android Studio

## Important Notes

### ⚠️ Production Considerations

This is a **basic VPN implementation** for educational purposes. For production use, you should:

1. **Implement Proper Server Connection**: Connect to actual VPN servers
2. **Add Encryption**: Implement strong encryption protocols (OpenVPN, WireGuard, IPSec)
3. **Add Authentication**: Implement user authentication and authorization
4. **Server Selection**: Allow users to choose from multiple server locations
5. **Error Handling**: Add comprehensive error handling and recovery
6. **Logging**: Implement proper logging and analytics
7. **Kill Switch**: Add network kill switch to prevent data leaks
8. **Split Tunneling**: Allow selective app VPN routing
9. **Auto-Connect**: Add auto-connect on boot/network change
10. **Performance**: Optimize packet processing for better speed

### VPN Server Setup

This app requires a VPN server to connect to. You can:
- Use commercial VPN services (with their SDK)
- Set up your own VPN server (OpenVPN, WireGuard)
- Use cloud providers (AWS, GCP, Azure) to deploy VPN servers

### Testing

To test the app:
1. Build and install on a physical device (emulators may have limited VPN support)
2. Grant VPN permission when prompted
3. Click "Connect" button
4. Verify VPN connection in Android settings
5. Test internet connectivity
6. Check connection duration timer

## Dependencies

```gradle
androidx.core:core-ktx:1.12.0
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.10.0
androidx.constraintlayout:constraintlayout:2.1.4
androidx.lifecycle:lifecycle-runtime-ktx:2.6.2
kotlinx-coroutines-android:1.7.3
```

## Troubleshooting

### App doesn't connect
- Check if VPN permission is granted
- Verify internet connection
- Check Android VPN settings

### Build errors
- Sync Gradle files
- Clean and rebuild project
- Update Android Studio

### Permission denied
- Request VPN permission properly
- Check AndroidManifest.xml permissions

## License

This project is provided as-is for educational purposes. Feel free to modify and use it according to your needs.

## Resources

- [Android VPN Service Documentation](https://developer.android.com/reference/android/net/VpnService)
- [Material Design Guidelines](https://material.io/design)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

## Support

For issues, questions, or contributions, please refer to the source code and Android documentation.

---

**Note**: This is a basic VPN implementation. For production use, implement proper security protocols, server infrastructure, and encryption methods.
