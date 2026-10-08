package nl.schnobias.personaltools;

import android.Manifest;
import android.app.*;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;

public final class BluetoothReceiver extends BroadcastReceiver {
    // AOSP system broadcast; not a public SDK contract. No hidden-method reflection.
    static final String BATTERY_ACTION = "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED";
    static final String BATTERY_EXTRA = "android.bluetooth.device.extra.BATTERY_LEVEL";
    static final String CHANNEL = "bluetooth_disconnect";

    static void configure(Context context, boolean enabled) {
        context.getPackageManager().setComponentEnabledSetting(
                new ComponentName(context, BluetoothReceiver.class),
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    static void createChannel(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        NotificationChannel channel = new NotificationChannel(CHANNEL, "Bluetooth disconnects", NotificationManager.IMPORTANCE_LOW);
        channel.setSound(null, null);
        channel.enableVibration(false);
        channel.setDescription("Last reported accessory battery when its Bluetooth link disconnects");
        manager.createNotificationChannel(channel);
    }

    static SharedPreferences store(Context context) {
        return context.getSharedPreferences("bluetooth_battery", Context.MODE_PRIVATE);
    }

    @Override public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!BATTERY_ACTION.equals(action) && !BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) return;
        SharedPreferences prefs = store(context);
        if (!prefs.getBoolean("enabled", false)) return;
        if (Build.VERSION.SDK_INT >= 31 && context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return;
        BluetoothDevice device = Build.VERSION.SDK_INT >= 33
                ? intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class)
                : intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (device == null) return;
        try {
            // Only paired devices, with device identity supplied by the protected system broadcast.
            if (device.getBondState() != BluetoothDevice.BOND_BONDED) return;
            String key = device.getAddress();
            String name = device.getName();
            if (name == null || name.trim().isEmpty()) name = "Bluetooth device";
            long now = System.currentTimeMillis();
            if (BATTERY_ACTION.equals(action)) {
                int percentage = intent.getIntExtra(BATTERY_EXTRA, -1);
                // Disconnect often emits -1; retain the previous valid reading, including 0%.
                if (BatteryReading.valid(percentage)) prefs.edit().putInt(key + ".level", percentage)
                        .putLong(key + ".time", now).putString(key + ".name", name).apply();
                return;
            }
            if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (!manager.areNotificationsEnabled()) return;
            createChannel(context);
            Intent open = new Intent(context, MainActivity.class);
            PendingIntent content = PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            String text = BatteryReading.message(prefs.getInt(key + ".level", -1), prefs.getLong(key + ".time", 0), now);
            Notification notification = new Notification.Builder(context, CHANNEL)
                    .setSmallIcon(R.drawable.ic_battery).setContentTitle(name + " disconnected")
                    .setContentText(text).setStyle(new Notification.BigTextStyle().bigText(text))
                    .setContentIntent(content).setAutoCancel(true)
                    .setVisibility(Notification.VISIBILITY_PRIVATE).build();
            // A distinct stable tag prevents devices replacing one another's notifications.
            manager.notify(key, 1, notification);
        } catch (SecurityException ignored) {
            // Permissions can be revoked while handling an event.
        }
    }
}
