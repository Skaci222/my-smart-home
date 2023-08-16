package com.myproject.receiverlistener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.myproject.logic.NotificationHelper;

public class DisarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("DISARMRECEIVER", "disarmReceiver");
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getAlarmNotification("Device Disarmed",
                "Device has been disarmed");
        notificationHelper.getManager().notify(5, nb.build());

        Intent broadcastIntent = new Intent("disarm_device_notification");
        context.sendBroadcast(broadcastIntent);
    }
}
