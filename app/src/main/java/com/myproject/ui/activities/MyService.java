package com.myproject.ui.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.myproject.R;

public class MyService extends Service {

    public static final String TAG = "My Service: ";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendNotification();

        return START_STICKY;
    }
    public void sendNotification() {
        String title = "Alarm System Running";
        String message = "Foreground service activated";

        Intent notificationIntent = new Intent(this, StartScreen.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 1, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(
                this.getApplicationContext(), App.SERVICE_NOTIFICATION)
                .setSmallIcon(R.drawable.ic_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .build();
        startForeground(1, notification);
        Log.i(TAG, "service notification sent");
    }

    public void sendTempNotification() {
        String title = "Temperature updated";
        String message = "Click to check your latest temperature reading";

        Intent intent = new Intent(this, StartScreen.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this.getApplicationContext(), App.TEMPERATURE_NOTIFICATION)
                .setSmallIcon(R.drawable.ic_baseline_sync_24)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .build();

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(2, notification);

        Log.i(TAG, "sent temp notification");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
