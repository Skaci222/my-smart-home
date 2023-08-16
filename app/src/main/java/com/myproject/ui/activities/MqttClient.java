package com.myproject.ui.activities;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.UUID;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;

public class MqttClient implements Parcelable, MqttCallbackExtended {

    public static final String TEMP_SUB = "iot-2/evt/temperature/fmt/json";
    public static final String TEMP_PUB = "iot-2/cmd/temp/fmt/json";
    private String broker = "ssl://e7ea538cb0564a42b068269a96574848.s1.eu.hivemq.cloud:8883";
    private String clientId = UUID.randomUUID().toString();
    private Context context;
    private MqttAndroidClient androidClient;

    public MqttClient(Context context) {
        this.context = context.getApplicationContext();
        this.androidClient = new MqttAndroidClient(context, broker, clientId, Ack.AUTO_ACK);
        connectMqtt();

    }

    public MqttConnectOptions getMqttOptions(){
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("maji22");
        options.setPassword("password".toCharArray());
        options.setCleanSession(true);
        options.setKeepAliveInterval(6000000);
        return options;
    }

    public void connectMqtt(){
        IMqttToken token = androidClient.connect(getMqttOptions());
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i("TAG", "connected successfully");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i("TAG", "did not connect");
            }
        });
    }

    public void subscribeTempTopic(){
        androidClient.subscribe(TEMP_SUB,0);
    }

    public void publishTempTopic(){
        JSONObject object = new JSONObject();

    }

    protected MqttClient(Parcel in) {
    }

    public static final Creator<MqttClient> CREATOR = new Creator<MqttClient>() {
        @Override
        public MqttClient createFromParcel(Parcel in) {
            return new MqttClient(in);
        }

        @Override
        public MqttClient[] newArray(int size) {
            return new MqttClient[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {

    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
