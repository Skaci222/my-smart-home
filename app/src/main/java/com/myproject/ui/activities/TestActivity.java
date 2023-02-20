package com.myproject.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.myproject.R;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;

public class TestActivity extends AppCompatActivity {
    public static final String HOST = "tcp://y94ieb.messaging.internetofthings.ibmcloud.com:1883";
    public static final String TAG = "TestActivity";

    private MqttAndroidClient client;
    private String clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        clientId = "a:y94ieb:andId-001";
        client = new MqttAndroidClient(getApplicationContext(), HOST,
                clientId, Ack.AUTO_ACK);

        try {
            connectMQTT();
        } catch (MqttException e) {
            e.printStackTrace();
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

            }
            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i(TAG, "Did not connect: " + exception.getCause() + ", " +  exception.getMessage());
            }
        });

    }
}