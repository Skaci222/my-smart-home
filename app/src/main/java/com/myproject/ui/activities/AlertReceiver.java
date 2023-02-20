package com.myproject.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.json.JSONException;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

public class AlertReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getAlarmNotification("Heater Has Turned On",
                "Heater has been turned on at the pre-set time");
        notificationHelper.getManager().notify(1, nb.build());
        try {
            StartScreen.getInstance().heaterControl(1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
