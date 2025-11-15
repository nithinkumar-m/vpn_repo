# Android VPN App - Kotlin

A complete Android VPN application built with Kotlin that provides basic VPN functionality.

## Features

- ✅ VPN connection/disconnection
- ✅ Modern Material Design UI
- ✅ Real-time connection status
- ✅ Foreground service for VPN
- ✅ Permission handling
- ✅ MVVM architecture pattern

## Project Structure

```
app/
├── src/main/
│   ├── java/com/vpnapp/
│   │   ├── MainActivity.kt          # Main UI activity
│   │   ├── VpnService.kt            # VPN service implementation
│   │   ├── VpnManager.kt            # VPN connection manager
│   │   └── VpnViewModel.kt          # ViewModel for state management
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml    # Main activity layout
│   │   ├── values/
│   │   │   ├── strings.xml          # String resources
│   │   │   ├── colors.xml           # Color resources
│   │   │   └── themes.xml           # App theme
│   └── AndroidManifest.xml          # App manifest
├── build.gradle.kts                 # App-level build config
└── proguard-rules.pro               # ProGuard rules

build.gradle.kts                     # Project-level build config
settings.gradle.kts                  # Gradle settings
gradle.properties                    # Gradle properties
```

## Requirements

- Android Studio Hedgehog (2023.1.1) or later
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Kotlin 1.9.0+
- Gradle 8.0+

## Setup Instructions

1. **Clone or download the project**

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the project directory

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle
   - If not, click "Sync Now" when prompted

4. **Build the Project**
   - Click "Build" → "Make Project" or press `Ctrl+F9` (Windows/Linux) or `Cmd+F9` (Mac)

5. **Run the App**
   - Connect an Android device or start an emulator
   - Click "Run" → "Run 'app'" or press `Shift+F10` (Windows/Linux) or `Ctrl+R` (Mac)

## How to Use

1. **Launch the App**
   - Open the VPN App on your device

2. **Connect VPN**
   - Tap the "Connect VPN" button
   - Grant VPN permission when prompted (first time only)
   - Wait for connection to establish
   - Status indicator will turn green when connected

3. **Disconnect VPN**
   - Tap the "Disconnect VPN" button
   - VPN will disconnect immediately

## Permissions

The app requires the following permissions:

- `BIND_VPN_SERVICE` - Required for VPN functionality
- `INTERNET` - Required for network access
- `FOREGROUND_SERVICE` - Required for Android 14+ foreground service
- `FOREGROUND_SERVICE_CONNECTED_DEVICE` - Required for VPN foreground service

## Architecture

The app follows MVVM (Model-View-ViewModel) architecture:

- **View**: `MainActivity` - Handles UI and user interactions
- **ViewModel**: `VpnViewModel` - Manages UI-related data and state
- **Model**: `VpnManager` - Handles VPN business logic
- **Service**: `VpnService` - Android VPN service implementation

## VPN Implementation Details

The VPN service uses Android's `VpnService` API to:

- Create a virtual network interface
- Route traffic through the VPN
- Process network packets
- Maintain a foreground service for continuous operation

**Note**: This is a basic VPN implementation. For production use, you would need to:
- Implement proper encryption/decryption
- Add server connection logic
- Handle different VPN protocols (OpenVPN, WireGuard, etc.)
- Add authentication mechanisms
- Implement proper packet routing

## Building for Release

1. Generate a signed APK:
   - Build → Generate Signed Bundle / APK
   - Follow the wizard to create a keystore and sign the app

2. Or use Gradle:
   ```bash
   ./gradlew assembleRelease
   ```

## Troubleshooting

**VPN permission denied:**
- Make sure you grant VPN permission when prompted
- Check device settings → Apps → VPN App → Permissions

**App won't build:**
- Ensure Android Studio is up to date
- Sync Gradle files: File → Sync Project with Gradle Files
- Clean and rebuild: Build → Clean Project, then Build → Rebuild Project

**VPN won't connect:**
- Check device logs: View → Tool Windows → Logcat
- Ensure device is running Android 7.0 or higher
- Verify VPN permission is granted

## License

This project is provided as-is for educational purposes.

## Contributing

Feel free to fork and modify this project for your needs.
