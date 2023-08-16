package com.myproject.logic;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.myproject.R;
import com.myproject.ui.activities.StartScreen;

public class NotificationHelper extends ContextWrapper {

    public static final String ALARM_CHANNEL_ID = "alarmChannelId";
    public static final String ALARM_CHANNEL_NAME = "alarm_channel";
    public static final String TEMP_CHANNEL_ID = "tempChannelId";
    public static final String TEMP_CHANNEL_NAME = "temp_channel";

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
                .setSmallIcon(R.drawable.ic_info);
    }

    public NotificationCompat.Builder sendTempNotification(String title, String message) {
       /* Intent activityIntent = new Intent(Intent.ACTION_MAIN);
        activityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        activityIntent.setClass(this, StartScreen.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);*/

       return new NotificationCompat.Builder(this.getApplicationContext(), TEMP_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_thermostat)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);
                //.setContentIntent(contentIntent);


    }
}
