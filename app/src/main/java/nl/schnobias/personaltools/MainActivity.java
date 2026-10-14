package nl.schnobias.personaltools;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.util.*;

/** A small, native, extensible home for Personal Tools. */
public final class MainActivity extends Activity {
    private LinearLayout page;
    private int ink, muted, accent, surface, outline;
    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable shape(int color, int stroke, float radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius));
        if (stroke != 0) d.setStroke(dp(1), stroke); return d;
    }
    private TextView label(String value, float size, int color, boolean bold) {
        TextView v = new TextView(this); v.setText(value); v.setTextSize(size); v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD); v.setGravity(Gravity.CENTER_VERTICAL); return v;
    }
    private LinearLayout column() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.VERTICAL); return v; }
    private LinearLayout row() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.HORIZONTAL); v.setGravity(Gravity.CENTER_VERTICAL); return v; }
    private LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w < 0 ? w : dp(w), h < 0 ? h : dp(h)); }
    private void gap(LinearLayout parent, int height) { parent.addView(new View(this), lp(1,height)); }

    @Override public void onCreate(Bundle state) { super.onCreate(state); }
    @Override public void onResume() { super.onResume(); render(); }
    private void render() {
        boolean dark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        ink = Color.parseColor(dark ? "#F1F5F2" : "#17231D");
        muted = Color.parseColor(dark ? "#AAB9B0" : "#66746C");
        accent = Color.parseColor(dark ? "#81D9AA" : "#176B46");
        surface = Color.parseColor(dark ? "#161E19" : "#FFFFFF");
        outline = Color.parseColor(dark ? "#334139" : "#E2EAE4");
        getWindow().setStatusBarColor(Color.TRANSPARENT); getWindow().setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 30) {
            int light = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
            getWindow().getInsetsController().setSystemBarsAppearance(dark ? 0 : light, light);
        }
        boolean enabled = BluetoothReceiver.store(this).getBoolean("enabled", false);
        BluetoothReceiver.configure(this, enabled); BluetoothReceiver.createChannel(this);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setClipToPadding(false);
        page = column(); page.setClipToPadding(false); page.setPadding(dp(22),dp(18),dp(22),dp(32)); scroll.addView(page);
        setContentView(scroll);
        if (Build.VERSION.SDK_INT >= 30) scroll.setOnApplyWindowInsetsListener((v,in)->{
            android.graphics.Insets safe = in.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            v.setPadding(safe.left, safe.top, safe.right, safe.bottom); return in;
        });

        LinearLayout brand = row();
        ImageView mark = new ImageView(this); mark.setImageResource(R.drawable.ic_launcher_foreground);
        brand.addView(mark,lp(42,42));
        LinearLayout brandCopy = column(); brandCopy.setPadding(dp(10),0,0,0);
        brandCopy.addView(label("PERSONAL",10,muted,true)); brandCopy.addView(label("TOOLS",17,ink,true));
        brand.addView(brandCopy); page.addView(brand); gap(page,30);

        page.addView(label("Your everyday\nessentials.",32,ink,true)); gap(page,10);
        page.addView(label("A growing collection of small, useful tools.",15,muted,false)); gap(page,26);

        LinearLayout titleRow=row();
        titleRow.addView(label("YOUR TOOLS",11,muted,true),new LinearLayout.LayoutParams(0,dp(28),1));
        TextView count=label("02 TOOLS",10,accent,true); count.setPadding(dp(10),dp(6),dp(10),dp(6));
        count.setBackground(shape(dark?Color.parseColor("#203B2C"):Color.parseColor("#EAF5EE"),0,30)); titleRow.addView(count);
        page.addView(titleRow); gap(page,12);

        LinearLayout card=column(); card.setPadding(dp(17),dp(17),dp(17),dp(17)); card.setBackground(shape(surface,outline,22));
        LinearLayout cardHead=row();
        TextView icon=label("⌁",30,dark?ink:accent,true); icon.setGravity(Gravity.CENTER); icon.setBackground(shape(dark?Color.parseColor("#26332B"):Color.parseColor("#EFF6F1"),0,15));
        cardHead.addView(icon,lp(50,50));
        LinearLayout title=column(); title.setPadding(dp(12),0,0,0);
        title.addView(label("Bluetooth",19,ink,true)); title.addView(label("DEVICE COMPANION",10,muted,true));
        cardHead.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        TextView badge=label("READY",10,accent,true); badge.setPadding(dp(9),dp(6),dp(9),dp(6)); badge.setBackground(shape(dark?Color.parseColor("#203B2C"):Color.parseColor("#EAF5EE"),0,30)); cardHead.addView(badge);
        card.addView(cardHead); gap(card,14);
        card.addView(label("Know where your headphones stand.",15,ink,true)); gap(card,5);
        card.addView(label("Immediate battery updates, with a configurable low-battery threshold. Disconnect reminders included.",14,muted,false)); gap(card,16);
        LinearLayout control=row();
        LinearLayout status=column();
        boolean nearby=Build.VERSION.SDK_INT<31||checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED;
        NotificationManager manager=getSystemService(NotificationManager.class);
        boolean notifications=manager.areNotificationsEnabled() && manager.getNotificationChannel(BluetoothReceiver.CHANNEL)!=null && manager.getNotificationChannel(BluetoothReceiver.CHANNEL).getImportance()!=NotificationManager.IMPORTANCE_NONE;
        status.addView(label(enabled?"MONITORING ON":"MONITORING OFF",11,enabled?accent:muted,true));
        status.addView(label(!nearby?"Nearby access needed":!notifications?"Notification access needed":"Events only · low battery use",11,muted,false));
        control.addView(status,new LinearLayout.LayoutParams(0,-2,1));
        Switch toggle=new Switch(this); toggle.setChecked(enabled); control.addView(toggle); card.addView(control);
        toggle.setOnCheckedChangeListener((b,on)->{
            BluetoothReceiver.store(this).edit().putBoolean("enabled",on).apply(); BluetoothReceiver.configure(this,on);
            if(on) requestRequiredPermissions(); else render();
        });
        if(!nearby||!notifications){
            gap(card,12); Button grant=actionButton(!nearby?"Grant nearby devices access":"Review notification settings",false);
            grant.setOnClickListener(v->{if(!nearby)requestRequiredPermissions();else startActivity(new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()).putExtra(Settings.EXTRA_CHANNEL_ID,BluetoothReceiver.CHANNEL));});card.addView(grant);
        }
        Switch onlyLow=new Switch(this);onlyLow.setText("Only notify at or below threshold");onlyLow.setTextColor(ink);onlyLow.setChecked(BluetoothReceiver.store(this).getBoolean("only_low",false));
        onlyLow.setOnCheckedChangeListener((button,on)->BluetoothReceiver.store(this).edit().putBoolean("only_low",on).apply());card.addView(onlyLow);
        Switch disconnect=new Switch(this);disconnect.setText("Always notify on disconnect");disconnect.setTextColor(ink);
        disconnect.setChecked(BluetoothReceiver.store(this).getBoolean("always_disconnect",false));
        disconnect.setOnCheckedChangeListener((button,on)->BluetoothReceiver.store(this).edit().putBoolean("always_disconnect",on).apply());card.addView(disconnect);
        int threshold=BatteryNotificationPolicy.threshold(BluetoothReceiver.store(this).getInt("threshold",30));
        TextView thresholdLabel=label("Battery threshold: "+threshold+"%",14,ink,true);card.addView(thresholdLabel);
        SeekBar thresholdSlider=new SeekBar(this);thresholdSlider.setMax(100);thresholdSlider.setProgress(threshold);thresholdSlider.setContentDescription("Battery notification threshold, percent");
        thresholdSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar bar,int value,boolean user){thresholdLabel.setText("Battery threshold: "+value+"%");if(user)BluetoothReceiver.store(MainActivity.this).edit().putInt("threshold",value).apply();}
            public void onStartTrackingTouch(SeekBar bar){} public void onStopTrackingTouch(SeekBar bar){}
        });card.addView(thresholdSlider);
        card.addView(label("Always notify on disconnect bypasses the filter, including unknown batteries. Above-threshold disconnects are silent. Sound uses the selected threshold.",12,muted,false));
        Button alerts=actionButton("Low battery sound settings",false); alerts.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()).putExtra(Settings.EXTRA_CHANNEL_ID,BluetoothReceiver.LOW_CHANNEL))); card.addView(alerts); page.addView(card); gap(page,20);

        LinearLayout section=row(); section.addView(label("RECENT BATTERY",11,muted,true)); page.addView(section); gap(page,10);
        Map<String,?> all=BluetoothReceiver.store(this).getAll(); List<String> keys=new ArrayList<>(all.keySet()); Collections.sort(keys); boolean any=false;
        for(String key:keys) if(key.endsWith(".level")){
            String id=key.substring(0,key.length()-6); LinearLayout reading=row(); reading.setPadding(dp(15),dp(11),dp(15),dp(11)); reading.setBackground(shape(surface,outline,16));
            LinearLayout copy=column(); copy.addView(label(BluetoothReceiver.store(this).getString(id+".name","Bluetooth device"),14,ink,true));
            copy.addView(label(BatteryReading.message((Integer)all.get(key),BluetoothReceiver.store(this).getLong(id+".time",0),System.currentTimeMillis()),12,muted,false));
            reading.addView(copy,new LinearLayout.LayoutParams(0,-2,1)); page.addView(reading); gap(page,7); any=true;
        }
        if(!any){TextView empty=label("Your device readings will appear here after they report a battery level.",13,muted,false);empty.setPadding(dp(14),dp(14),dp(14),dp(14));empty.setBackground(shape(surface,outline,16));page.addView(empty);}

        gap(page,24);
        page.addView(label("Quick Answer",20,ink,true)); gap(page,6); page.addView(label("Select text in another app, then choose Quick Answer to ask Gemini Nano on this phone.",14,muted,false)); Button answer=actionButton("Open Quick Answer",false);answer.setOnClickListener(v->startActivity(new Intent(this,QuickAnswerActivity.class)));page.addView(answer);
        gap(page,24); LinearLayout future=column(); future.setPadding(dp(17),dp(16),dp(17),dp(16)); future.setBackground(shape(dark?Color.parseColor("#191F1B"):Color.parseColor("#F2F6F3"),0,20));
        future.addView(label("MORE TOOLS,\nWHEN YOU NEED THEM.",11,muted,true)); gap(future,5);
        future.addView(label("This space is ready to grow with you.",14,ink,false));page.addView(future);
        gap(page,18); LinearLayout footer=row(); footer.addView(label("PRIVATE BY DESIGN",10,muted,true),new LinearLayout.LayoutParams(0,-2,1));
        TextView clear=label("Clear readings",11,muted,true);footer.addView(clear);clear.setOnClickListener(v->{BluetoothReceiver.store(this).edit().clear().putBoolean("enabled",BluetoothReceiver.store(this).getBoolean("enabled",false)).putBoolean("only_low",BluetoothReceiver.store(this).getBoolean("only_low",false)).putInt("threshold",BluetoothReceiver.store(this).getInt("threshold",30)).putBoolean("always_disconnect",BluetoothReceiver.store(this).getBoolean("always_disconnect",false)).apply();render();}); page.addView(footer);
    }

    private Button actionButton(String text,boolean primary){Button b=new Button(this);b.setText(text);b.setAllCaps(false);b.setTextSize(13);b.setTextColor(primary?Color.WHITE:accent);if(Build.VERSION.SDK_INT>=21)b.setBackground(shape(primary?accent:Color.TRANSPARENT,primary?0:outline,14));return b;}
    private void requestRequiredPermissions(){List<String> p=new ArrayList<>();if(Build.VERSION.SDK_INT>=31&&checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED)p.add(Manifest.permission.BLUETOOTH_CONNECT);if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)p.add(Manifest.permission.POST_NOTIFICATIONS);if(!p.isEmpty())requestPermissions(p.toArray(new String[0]),1);else render();}
    @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] results){super.onRequestPermissionsResult(code,permissions,results);render();}
}




