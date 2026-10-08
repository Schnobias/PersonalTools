# Personal Tools

Native Android utilities, starting with Bluetooth battery disconnect reminders. Java and Android framework UI; Android 8.0+; no runtime libraries, network permission, scanning, polling, alarms, wake locks, jobs, or persistent service.

## Bluetooth battery reminders

Open the app, enable **Disconnect reminders**, and grant Nearby devices (Android 12+) and Notifications (Android 13+). Reconnect your paired headphones, speaker, or other accessory so Android can report its battery. On an ACL disconnect the app posts a notification with the last valid percentage and how long ago it was reported. If there is no report, it explicitly shows battery unavailable. Each device has its own notification and persisted reading; 0% is valid, and a disconnect's unknown (-1) report never overwrites it. Readings stay local, backups are disabled, and you can clear them in the app.

Monitoring defaults off. Its manifest receiver is disabled until reminders are enabled, and turning reminders off disables that component again, avoiding app launches for Bluetooth events while off. When enabled, the receiver exits immediately if permission is missing. Battery updates and disconnect events wake the process only briefly. This design minimizes app overhead, but physical-device power consumption has not been measured.

### Pixel 11 Pro setup

The app now compiles and targets Android 16 (API 36), with modern Android behavior rather than SDK 34 compatibility settings. System-bar and display-cutout insets keep controls clear of the camera cutout and gesture bar. System dark mode uses a black background suited to an OLED screen; the UI has no continuous animation or refresh loop. Java-only APKs contain no native libraries requiring CPU or 16 KB page-size variants.

Keep Pixel app battery usage at its normal **Optimized** setting. No battery-optimization exemption is requested. On a fresh install, disconnect notifications use a silent, non-vibrating low-importance channel; the notification settings button opens that channel directly. Existing channel settings and user choices are preserved on upgrades. The app reports a disabled channel as notifications needing settings, even when notification permission is granted.

Pixel-specific acceptance remains required: no phone is currently connected for installation or testing, so neither this phone's Android build nor accessory battery delivery has been verified. Test screen-off disconnects with Optimized battery usage and Battery Saver, and compare overnight idle use with reminders on and off. Do not infer guaranteed delivery or measured battery savings from the implementation alone.

### Compatibility limits

Android exposes ACL disconnects to manifest receivers, but the battery broadcast (`android.bluetooth.device.action.BATTERY_LEVEL_CHANGED`) is an AOSP system interface, not a guaranteed public SDK API. AOSP includes background receivers when sending it; manufacturers may differ. The app uses the broadcast strings, without reflection, hidden API calls, or opening extra Bluetooth connections. Accessories that do not report battery, and phones that do not deliver this broadcast, show unavailable. There is no universally supported third-party API for every accessory's battery percentage.

An existing connection may not produce a battery update until its level changes or it reconnects. Historical readings are deliberately retained and labeled with their age. ACL disconnect means the Bluetooth link ended; disconnecting only one audio profile may leave that link connected. Force-stop, denied notification permissions, disabled channels, or manufacturer background restrictions can prevent notifications. Reopen the app after force-stop. Notifications use private lock-screen visibility.

Sources: [Android broadcast exceptions](https://developer.android.com/develop/background-work/background-tasks/broadcasts/broadcast-exceptions), [AOSP battery broadcast implementation](https://android.googlesource.com/platform/packages/modules/Bluetooth/+/refs/heads/main/android/app/src/com/android/bluetooth/btservice/RemoteDevices.java).

## Build and verification

Use JDK 17+, Android SDK 36, and the included Gradle 8.13 wrapper (distribution checksum pinned):

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. CI builds, tests, lints, and uploads the debug APK.

Before relying on reminders, test on a physical phone with real Bluetooth accessories:

1. Enable reminders and grant both permissions. Reconnect an accessory and check that its reading appears in the app.
2. Close the UI, turn the screen off, then disconnect. Confirm the notification identifies the right device, percentage, and reading age.
3. Repeat with a second device and one that has no battery reporting; verify independent notifications and unavailable text.
4. Test permission denial, disabled reminders, reconnects, reboot, and force-stop/reopen. Unknown reports must retain the previous reading.
5. Compare idle battery use with reminders enabled and disabled over comparable periods. No physical-device battery or Bluetooth acceptance is claimed by the build checks.
