package com.myproject.ui.activities;

import static com.myproject.ui.activities.StartScreen.RELAY_CONTROL_TOPIC;
import static com.myproject.ui.activities.StartScreen.RELAY_VALUE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

public class AlertReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("ALERTRECEIVER", "alertReceiver");
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getAlarmNotification("Heater Has Turned On",
                "Heater has been turned on at the pre-set time");
        notificationHelper.getManager().notify(1, nb.build());

        Intent broadcastIntent = new Intent("switch_on_notification");
        context.sendBroadcast(broadcastIntent);


    }
}
