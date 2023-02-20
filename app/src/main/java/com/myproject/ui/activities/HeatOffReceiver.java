package com.myproject.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;

public class HeatOffReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getAlarmNotification("Heater Has Turned Off",
                "Heater has been turned off");
        notificationHelper.getManager().notify(5, nb.build());
        try {
            StartScreen.getInstance().heaterControl(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
