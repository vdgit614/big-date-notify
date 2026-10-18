package com.bigdate.notify;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateNotifyService extends Service {
    private static final String CHANNEL_ID = "big_date_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Date now = new Date();
        String dateStr = new SimpleDateFormat("dd- MM- yyyy", Locale.ENGLISH).format(now);
        String dayStr = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(now);
        String timeStr = new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(now).toLowerCase();

        // क्रम: Date, Day, Time
        String finalText = dateStr + "\n" + dayStr + "\n" + timeStr;

        RemoteViews customView = new RemoteViews(getPackageName(), R.layout.notification_date);
        customView.setTextViewText(R.id.bigDateText, finalText);

        Intent clockIntent = new Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, clockIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                .setCustomContentView(customView)
                .setCustomBigContentView(customView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContentIntent(pIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();

        startForeground(1001, notification);
        scheduleMidnightUpdate();
        return START_STICKY;
    }

    private void scheduleMidnightUpdate() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, BootReceiver.class);
        intent.setAction("UPDATE_DATE");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 5);
        calendar.set(Calendar.MILLISECOND, 0);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Big Date", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
