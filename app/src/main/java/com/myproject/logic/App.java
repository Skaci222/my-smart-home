package com.myproject.logic;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.myproject.R;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class App extends Application {


    public static final String TEMPERATURE_NOTIFICATION = "temperature_notification";
    public static final String SERVICE_NOTIFICATION = "service_notification";


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    public void createNotificationChannels(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //API 26
            NotificationChannel tempNotification = new NotificationChannel(
                    TEMPERATURE_NOTIFICATION,
                    "Temperature channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            tempNotification.setDescription("this is Temperature channel");
            tempNotification.enableVibration(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(tempNotification);

            NotificationChannel serviceNotification = new NotificationChannel(
                    SERVICE_NOTIFICATION, "Service notification channel",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(serviceNotification);
        }
    }
}
