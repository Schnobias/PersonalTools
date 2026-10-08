package nl.schnobias.personaltools;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.provider.Settings;
import android.widget.*;
import java.util.*;

public final class MainActivity extends Activity {
    private LinearLayout layout;
    @Override public void onCreate(Bundle state) { super.onCreate(state); }
    @Override public void onResume() { super.onResume(); render(); }
    private void text(String value, int size) {
        TextView view = new TextView(this); view.setText(value); view.setTextSize(size);
        view.setPadding(0, 12, 0, 12); layout.addView(view);
    }
    private void render() {
        ScrollView scroll = new ScrollView(this); layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding); scroll.addView(layout); setContentView(scroll);
        text("Personal Tools", 28); text("Bluetooth battery reminders", 22);
        text("Receive the last reported battery level when a paired Bluetooth device disconnects. No polling, scanning or always-running service.", 16);
        Switch enabled = new Switch(this); enabled.setText("Disconnect reminders");
        enabled.setChecked(BluetoothReceiver.store(this).getBoolean("enabled", false));
        enabled.setOnCheckedChangeListener((button, checked) -> {
            BluetoothReceiver.store(this).edit().putBoolean("enabled", checked).apply();
            if (checked) requestRequiredPermissions();
        }); layout.addView(enabled);
        boolean bluetooth = Build.VERSION.SDK_INT < 31 || checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        boolean notifications = getSystemService(NotificationManager.class).areNotificationsEnabled();
        text("Nearby devices: " + (bluetooth ? "allowed" : "permission needed") + "\nNotifications: " + (notifications ? "allowed" : "permission or settings needed"), 16);
        Button permissions = new Button(this); permissions.setText("Grant permissions");
        permissions.setOnClickListener(view -> requestRequiredPermissions()); layout.addView(permissions);
        Button settings = new Button(this); settings.setText("Notification settings");
        settings.setOnClickListener(view -> startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()))); layout.addView(settings);
        text("Some devices or Android versions do not expose battery reports. Readings start after enabling reminders; reconnect your device to obtain its next report. Old readings keep their age. Force-stopping the app prevents reminders until you reopen it.", 16);
        text("Last reported batteries", 20);
        Map<String, ?> values = BluetoothReceiver.store(this).getAll();
        List<String> keys = new ArrayList<>(values.keySet()); Collections.sort(keys);
        boolean found = false;
        for (String key : keys) if (key.endsWith(".level")) {
            String address = key.substring(0, key.length() - 6);
            text(BluetoothReceiver.store(this).getString(address + ".name", "Bluetooth device") + "\n"
                    + BatteryReading.message((Integer) values.get(key), BluetoothReceiver.store(this).getLong(address + ".time", 0), System.currentTimeMillis()), 16);
            found = true;
        }
        if (!found) text("No battery reports received yet.", 16);
        Button clear = new Button(this); clear.setText("Clear saved battery readings");
        clear.setOnClickListener(view -> {
            boolean on = BluetoothReceiver.store(this).getBoolean("enabled", false);
            BluetoothReceiver.store(this).edit().clear().putBoolean("enabled", on).apply(); render();
        }); layout.addView(clear);
    }
    private void requestRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        if (!permissions.isEmpty()) requestPermissions(permissions.toArray(new String[0]), 1);
        else render();
    }
    @Override public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code, permissions, results); render();
    }
}
