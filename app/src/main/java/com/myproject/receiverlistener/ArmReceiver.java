package com.myproject.receiverlistener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.myproject.logic.NotificationHelper;

public class ArmReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("ARMRECEIVER", "armReceiver");
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getAlarmNotification("Device has been armed",
                "Your device has been armed");
        notificationHelper.getManager().notify(1, nb.build());

        Intent broadcastIntent = new Intent("arm_device_notification");
        context.sendBroadcast(broadcastIntent);


    }
}
