package com.myproject.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.myproject.R;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("temperature"); //should use Constants
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        //TextView tv = StartScreen.getInstance().findViewById(R.id.tvTemp);
       // tv.setText(message);
        Log.i("NotificationReceiver", "onRecieve");

    }
}
