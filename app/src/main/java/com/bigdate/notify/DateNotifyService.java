package com.bigdate.notify;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateNotifyService extends Service {

    private static final String CHANNEL_ID = "BigDateChannel";
    private static final int NOTIFICATION_ID = 1;
    private Handler handler = new Handler();
    private Runnable updateRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd- MM- yyyy", Locale.ENGLISH);
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH); // AM/PM के साथ

        Date now = new Date();
        String currentDate = dateFormat.format(now);
        String currentDay = dayFormat.format(now);
        String currentTime = timeFormat.format(now);

        RemoteViews customView = new RemoteViews(getPackageName(), R.layout.notification_big_date);
        customView.setTextViewText(R.id.date_text, currentDate);
        customView.setTextViewText(R.id.day_text, currentDay);
        customView.setTextViewText(R.id.time_text, currentTime);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setCustomContentView(customView)
                .setCustomBigContentView(customView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setOngoing(true) // हटेगी नहीं
                .setOnlyAlertOnce(true) // हर मिनट Tone/Popup नहीं
                .setSound(null)
                .setCategory(NotificationCompat.CATEGORY_SERVICE) // सबसे ऊपर रखने के लिए
                .setPriority(NotificationCompat.PRIORITY_MAX) // सबसे ऊपर
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Big Date Service",
                    NotificationManager.IMPORTANCE_HIGH // HIGH = सबसे ऊपर
            );
            channel.setDescription("Shows Big Date and Time on Top");
            channel.setSound(null, null); // HIGH है पर Sound बंद
            channel.enableVibration(false); // Vibration भी बंद
            channel.setShowBadge(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && updateRunnable != null) handler.removeCallbacks(updateRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
