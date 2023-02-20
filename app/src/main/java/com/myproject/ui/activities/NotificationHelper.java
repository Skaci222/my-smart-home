package com.myproject.ui.activities;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.myproject.R;

public class NotificationHelper extends ContextWrapper {

    public static final String ALARM_CHANNEL_ID = "alarmChannelId";
    public static final String ALARM_CHANNEL_NAME = "alarm_channel";

    private NotificationManager mManager;

    public NotificationHelper(Context base) {
        super(base);
            createNotificationChannel();

    }


    public void createNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel alarmChannel = new NotificationChannel(
                    ALARM_CHANNEL_ID, ALARM_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            alarmChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getManager().createNotificationChannel(alarmChannel);
        }
    }

    public NotificationManager getManager(){
        if(mManager == null){
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public NotificationCompat.Builder getAlarmNotification(String title, String message){
        return new NotificationCompat.Builder(getApplicationContext(), ALARM_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_car);
    }
}
