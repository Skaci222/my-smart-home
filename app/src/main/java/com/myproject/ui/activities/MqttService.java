package com.myproject.ui.activities;

import static com.myproject.ui.activities.StartScreen.HIVE_BROKER;
import static com.myproject.ui.activities.StartScreen.RELAY_STATUS_TOPIC;
import static com.myproject.ui.activities.StartScreen.TEMP_TOPIC;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.myproject.R;
import com.myproject.room.Message;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;

public class MqttService implements Parcelable{

    public static final String TAG = "MqttService";

    public MqttAndroidClient client;
    private Context context;
    private String uri;
    private String clientiId;

    public MqttService(Context context, MqttCallbackExtended listener){
        this.context = context.getApplicationContext();
        this.client = new MqttAndroidClient(context, HIVE_BROKER, "AndroidDevice");
        this.client.setCallback(listener);
    }

    protected MqttService(Parcel in) {
    }

    public static final Creator<MqttService> CREATOR = new Creator<MqttService>() {
        @Override
        public MqttService createFromParcel(Parcel in) {
            return new MqttService(in);
        }

        @Override
        public MqttService[] newArray(int size) {
            return new MqttService[size];
        }
    };

    public void initMqttClient(String uri, String clientId) throws MqttException {
        client = new MqttAndroidClient(context, uri, clientId);
        connectMqtt();
    }

    public MqttConnectOptions getMqttOptions(){
        MqttConnectOptions options = new MqttConnectOptions();
        options.setPassword("password".toCharArray());
        options.setUserName("maji22");
        options.setCleanSession(true);
        return options;
    }

    public void connectMqtt() throws MqttException {
        IMqttToken token = client.connect(getMqttOptions());
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i(TAG, "connected :)");
                //mqttCallback();

            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i(TAG, "did not connect :(");
            }
        });
    }

    public void publish(String topic, String key, String value) throws JSONException, MqttException {
        JSONObject object = new JSONObject();
        object.put(key, value);
        MqttMessage message = new MqttMessage(object.toString().getBytes(StandardCharsets.UTF_8));
        client.publish(topic, message);
        Log.i(TAG, "published " + message + "to " + topic);
    }

    public void subscribe(String topic) throws MqttException {
        client.subscribe(topic, 0);
        Log.i(TAG, "subscribed to: " + topic);
    }

    /*public void mqttCallback(){

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.i(TAG, "connectComplete");
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                theMessage = message;
                Log.i(TAG, "message arrived: " + theMessage + " topic: " + topic );
                /*String mTemperature;
                String mHumidity;
                String mRelayValue;

                JSONObject myMessage = new JSONObject(new String(message.getPayload()));

                switch (topic) {
                    case RELAY_STATUS_TOPIC:
                        mRelayValue = myMessage.getString("Relay");
                        //Intent relayMessageIntent = new Intent("message_broadcast");
                        //relayMessageIntent.putExtra("relay_value", mRelayValue);
                        //getApplicationContext().sendBroadcast(relayMessageIntent);
                        Log.i("MqttService", "message arrived! msg is: " + message);


                    case TEMP_TOPIC:
                        mTemperature = myMessage.getString("Temperature").substring(0,5);
                        mHumidity = myMessage.getString("Humidity").substring(0, 5);
                        //Intent temperatureMessageIntent = new Intent("message_broadcast");
                        //temperatureMessageIntent.putExtra("temperature_value", mTemperature);
                        //temperatureMessageIntent.putExtra("humidity_value", mHumidity);
                        //getApplicationContext().sendBroadcast(temperatureMessageIntent);
                        Log.i("MqttService", "message arrived! msg is: " + message);

                }


            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i("MqttService", "delivery complete");            }
        });
    }*/


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        //parcel.writeParcelable((Parcelable) MqttService.this, 0);
    }

}
