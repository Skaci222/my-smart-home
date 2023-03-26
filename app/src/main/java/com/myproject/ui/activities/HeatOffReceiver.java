package com.myproject.ui.activities;

import static com.myproject.ui.activities.StartScreen.RELAY_CONTROL_TOPIC;
import static com.myproject.ui.activities.StartScreen.RELAY_VALUE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

public class HeatOffReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("HEATOFFRECEIVER", "heatOffReceiver");
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getAlarmNotification("Heater Has Turned Off",
                "Heater has been turned off");
        notificationHelper.getManager().notify(5, nb.build());

        Intent broadcastIntent = new Intent("switch_off_notification");
        context.sendBroadcast(broadcastIntent);
    }
}
