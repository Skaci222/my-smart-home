package com.myproject.ui.activities;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.myproject.R;
import com.myproject.provisioning.ProvisionLanding;
import com.myproject.ui.adapters.RecyclerViewAdapter;

//import org.eclipse.paho.android.service.MqttAndroidClient;
import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;
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
import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends AppCompatActivity {

    public static final String TEMPERATURE_NOTIFICATION = "temperatureNotification";

    private NotificationManagerCompat notificationManager;

    private ImageView imageView;

    private TextView tvTempValue, tvHumidityValue, tvTempConfig, tvDeviceName;

    //MQTT
    public static final String TAG = "MQTT: ";
    public static final String HOST = "tcp://y94ieb.messaging.internetofthings.ibmcloud.com:1883";
    public static final String TIME_INTERVAL = "time_interval";
    public static final String TEMP_TOPIC = "iot-2/type/Microcontroller/id/ESP32/evt/temperature/fmt/json";
    public static final String STATUS_TOPIC = "iot-2/type/Microcontroller/id/ESP32/evt/status/fmt/json";
    public static final String PUB_REQUEST_TOPIC = "iot-2/type/Microcontroller/id/ESP32/cmd/request/fmt/json";
    public static final String PUB_CONTROL_TOPIC = "iot-2/type/Microcontroller/id/ESP32/cmd/control/fmt/json";
    public static final String ALARM_TOPIC = "iot-2/type/Microcontroller/id/ESP32/evt/alarm/fmt/json";

    private MqttAndroidClient client;
    private String clientId;


    private static HomeActivity ins;

    private String timeText;
    private String tempText;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TIME_TEXT = "time_text";
    public static final String TEMP_TEXT = "temp_text";
    private boolean isServiceRunning;
    private String timeInterval;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        ins = this;

        imageView = findViewById(R.id.ivConnected);
        tvTempValue = findViewById(R.id.tvTempValue);
        tvHumidityValue = findViewById(R.id.tvHumidityValue);
        tvTempConfig = findViewById(R.id.tvHowOftenTempIsUpdated);
        tvDeviceName = findViewById(R.id.tvDeviceName);

        clientId = "a:y94ieb:andId-001";
        client = new MqttAndroidClient(HomeActivity.this, HOST,
                clientId, Ack.AUTO_ACK);

        notificationManager = NotificationManagerCompat.from(this);

       try {
            connectMQTT();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        loadData();
        updateViews();

    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService();
    }

    public void updateViews(){
        tvTempConfig.setText(timeText);

    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        timeText = sharedPreferences.getString(TIME_TEXT, "");
    }

    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TIME_TEXT, tvTempConfig.getText().toString());
        editor.apply();
        Log.i(TAG, "time configuration has been saved");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.actionAddDevice:
                Intent i = new Intent(this, ProvisionLanding.class);
                startActivity(i);
                break;

            case R.id.updateOneMin:
                try {
                    timeConfigShort();
                    tvTempConfig.setText("Values updated every 1 minute");
                    saveData();
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.updateFiveMin:
                try {
                    timeConfigMed();
                    tvTempConfig.setText("Values updated every 5 minutes");
                    saveData();
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.updateTenMin:
                try {
                    timeConfigLong();
                    tvTempConfig.setText("Values updated every 10 minutes");
                    saveData();
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }

        }
        return super.onOptionsItemSelected(item);

    }

    public static HomeActivity getInstance(){
        return  ins;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startForegroundService(){
        Intent serviceIntent = (new Intent(getApplicationContext(), MyService.class));
        startService(serviceIntent);
        isServiceRunning = true;

    }
    public void stopService(){
        Intent serviceIntent = (new Intent(this, MyService.class));
        stopService(serviceIntent);
        isServiceRunning = false;
    }

    public void initRequest() throws JSONException, MqttException {
        JSONObject requestObject = new JSONObject();
        requestObject.put("temperature", 1);
        requestObject.put("humidity",1);
        MqttMessage mqttMessage = new MqttMessage(requestObject.toString().getBytes(StandardCharsets.UTF_8));
        client.publish(PUB_REQUEST_TOPIC, mqttMessage);
    }

    public void timeConfigShort() throws JSONException, MqttException{
        JSONObject configObject = new JSONObject();
        configObject.put(TIME_INTERVAL, "1");
        timeInterval = configObject.getString(TIME_INTERVAL);
        MqttMessage mqttMessage = new MqttMessage(configObject.toString().getBytes(StandardCharsets.UTF_8));
        client.publish(PUB_CONTROL_TOPIC, mqttMessage);
        Log.i(TAG, "published " + mqttMessage.toString() + "to " + PUB_CONTROL_TOPIC);
    }
    public void timeConfigMed() throws JSONException, MqttException{
        JSONObject configObject = new JSONObject();
        configObject.put(TIME_INTERVAL, "5");
        timeInterval = configObject.getString(TIME_INTERVAL);
        MqttMessage mqttMessage = new MqttMessage(configObject.toString().getBytes(StandardCharsets.UTF_8));
        client.publish(PUB_CONTROL_TOPIC, mqttMessage);
        Log.i(TAG, "published " + mqttMessage.toString() + "to " + PUB_CONTROL_TOPIC);

    }

    public void timeConfigLong() throws JSONException, MqttException{
        JSONObject configObject = new JSONObject();
        configObject.put(TIME_INTERVAL, "10");
        timeInterval = configObject.getString(TIME_INTERVAL);
        MqttMessage mqttMessage = new MqttMessage(configObject.toString().getBytes(StandardCharsets.UTF_8));
        client.publish(PUB_CONTROL_TOPIC, mqttMessage);
        Log.i(TAG, "published " + mqttMessage.toString() + "to " + PUB_CONTROL_TOPIC);

    }

    public void sendTempNotification() {
            String title = "Temperature updated";
            String message = "Click to check your latest temperature reading";

       Intent activityIntent = new Intent(Intent.ACTION_MAIN);
       activityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
       activityIntent.setClass(this, HomeActivity.class);
       activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        //Intent activityIntent = new Intent(this, HomeActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);

            Notification notification = new NotificationCompat.Builder(this.getApplicationContext(), App.TEMPERATURE_NOTIFICATION)
                    .setSmallIcon(R.drawable.ic_baseline_check_box_24)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(contentIntent)
                    //.addAction(0, "check temp", actionIntent)
                    .build();


            notificationManager.notify(1, notification);
            Log.i(TAG, "sent notification");

    }


    public void mqttCallbacks(){
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connection has been lost.." + cause);
                Toast.makeText(HomeActivity.this,
                        "connection has been lost...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                JSONObject myMessage = new JSONObject(new String(message.getPayload()));
                String mTemperature;
                String mHumidity;
                String mStatus;

                switch(topic){
                    case TEMP_TOPIC:
                        mTemperature = myMessage.getString("Temperature");
                        mTemperature = mTemperature.substring(0, 5);
                        mHumidity = myMessage.getString("Humidity");
                        mHumidity = mHumidity.substring(0, 5);

                        tvTempValue.setText(mTemperature);
                        tvHumidityValue.setText(mHumidity);
                        Log.i(TAG, "temperature is : " + mTemperature);
                        Log.i(TAG, "humidity is : " + mHumidity);

                        if(!isAppForeground(getApplicationContext())) {
                            sendTempNotification();
                        } else{
                            return;
                        }

                    case STATUS_TOPIC:
                        mStatus = myMessage.getString("version");
                        Log.i(TAG, mStatus);
                        if(!isAppForeground(getApplicationContext())) {
                            sendTempNotification();
                        } else{
                            return;
                        }
                    case ALARM_TOPIC:
                        if(!isAppForeground(getApplicationContext())) {
                            sendTempNotification();
                        } else{
                            return;
                        }
                    default:
                        return;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "your message delivery is complete");

            }
        });
    }

    public void subscribe() throws MqttException {
        client.subscribe(TEMP_TOPIC, 0);
        client.subscribe(STATUS_TOPIC, 0);
        Log.i(TAG, "subscribed to topics " + TEMP_TOPIC + "\n" + STATUS_TOPIC);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStop() {
        super.onStop();
        if(!isAppForeground(getApplicationContext())) {
            startForegroundService();
        }
    }

    public void connectMQTT() throws MqttException {

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName("a-y94ieb-avdxw7gowr");
        options.setPassword("@mcEx)FCK?RdA98czQ".toCharArray());

        Log.d(TAG, "connecting to server: " + HOST);

            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "Connected");
                    imageView.setImageResource(R.drawable.ic_baseline_check_24);
                    mqttCallbacks();

                    try {
                        subscribe();
                    } catch (MqttException  e) {
                        e.printStackTrace();
                    }

                    try {
                        initRequest();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "Did not connect");
                }
            });

    }
    public static boolean isAppForeground(Context context) {

        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> l = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : l) {
            if (info.uid == context.getApplicationInfo().uid &&
                    info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }
    public void changeDeviceName(String s){
        tvDeviceName.setText(s);
    }

}