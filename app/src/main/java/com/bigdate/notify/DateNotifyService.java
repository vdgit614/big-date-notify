package com.bigdate.notify;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Person;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.app.Service;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class DateNotifyService extends Service {

    private static final String CHANNEL_ID = "BigDateChannel";
    private static final String SHORTCUT_ID = "big_date_shortcut";
    private static final int NOTIFICATION_ID = 1;
    private Handler handler = new Handler();
    private Runnable updateRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        createConversationShortcut();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateNotification();
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateNotification();
                handler.postDelayed(this, 60000);
            }
        };
        handler.postDelayed(updateRunnable, 60000);
        return START_STICKY;
    }

    private void updateNotification() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

        Date now = new Date();
        String currentDate = dateFormat.format(now);
        String currentDay = dayFormat.format(now);
        String currentTime = timeFormat.format(now);
        String fullText = currentDate + "\n" + currentDay + "\n" + currentTime;

        RemoteViews customView = new RemoteViews(getPackageName(), R.layout.notification_big_date);
        customView.setTextViewText(R.id.date_text, currentDate);
        customView.setTextViewText(R.id.day_text, currentDay);
        customView.setTextViewText(R.id.time_text, currentTime);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setCustomContentView(customView)
                .setCustomBigContentView(customView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSound(null)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE) // Message Category
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setShortcutId(SHORTCUT_ID); // यही जादू है

        // Android 10+ के लिए Person बना कर Conversation में डालना
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Person person = new Person.Builder()
                    .setName("Big Date Notify")
                    .setImportant(true)
                    .build();
            
            NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle(person)
                    .setConversationTitle("Big Date Notify")
                    .addMessage(fullText, System.currentTimeMillis(), person)
                    .setGroupConversation(false);
            
            builder.setStyle(messagingStyle);
            // custom view फिर भी दिखेगा क्योंकि DecoratedCustomViewStyle है
            builder.setCustomContentView(customView);
            builder.setCustomBigContentView(customView);
        }

        startForeground(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Big Date Service", NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Shows Big Date on Top");
            channel.setSound(null, null);
            channel.enableVibration(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void createConversationShortcut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                ShortcutManager shortcutManager = (ShortcutManager) getSystemService(Context.SHORTCUT_SERVICE);
                if (shortcutManager != null) {
                    Icon icon = Icon.createWithResource(this, android.R.drawable.ic_dialog_info);
                    Person person = new Person.Builder().setName("Big Date Notify").build();
                    ShortcutInfo shortcut = new ShortcutInfo.Builder(this, SHORTCUT_ID)
                            .setLocusId(new android.content.LocusId(SHORTCUT_ID))
                            .setLongLived(true)
                            .setShortLabel("Big Date Notify")
                            .setIcon(icon)
                            .setPerson(person)
                            .build();
                    shortcutManager.pushDynamicShortcut(shortcut);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && updateRunnable != null) handler.removeCallbacks(updateRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
