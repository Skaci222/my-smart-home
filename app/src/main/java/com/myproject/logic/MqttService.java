package com.myproject.logic;

import static com.myproject.ui.activities.StartScreen.HIVE_BROKER;
import static com.myproject.ui.activities.StartScreen.PUB_ARM_DISARM_REQUEST;
import static com.myproject.ui.activities.StartScreen.PUB_CONTROL_TOPIC;
import static com.myproject.ui.activities.StartScreen.RELAY_CONTROL_TOPIC;
import static com.myproject.ui.activities.StartScreen.RELAY_STATUS_SUB;
import static com.myproject.ui.activities.StartScreen.RELAY_VALUE;
import static com.myproject.ui.activities.StartScreen.RESET_REQUEST;
import static com.myproject.ui.activities.StartScreen.TEMP_PUB;
import static com.myproject.ui.activities.StartScreen.TEMP_SUB;

import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

//import org.eclipse.paho.android.service.MqttAndroidClient;
import androidx.lifecycle.ViewModelProvider;

import com.myproject.model.MqttMsg;
import com.myproject.retrofit.MessageApi;
import com.myproject.retrofit.RetrofitService;
import com.myproject.room.Device;
import com.myproject.room.DeviceViewModel;
import com.myproject.room.MessageViewModel;
import com.myproject.ui.activities.MainTest;
import com.myproject.ui.activities.StartScreen;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MqttService implements Parcelable{

    public static final String TAG = "MqttService";

    public MqttAndroidClient client;
    private Context context;
    private String tempSubTopic;
    private String tempPubTopic;
    private String relayControlTopic;
    private String relayStatusSubTopic;
    private RetrofitService retrofitService = new RetrofitService();
    private MessageApi messageApi = retrofitService.getRetrofit().create(MessageApi.class);

    public interface CallBackListener{
        void messageReceived(String topic, String key, String message);
    }

    public CallBackListener listener;

    public MqttService(Context context) throws MqttException {
        this.context = context.getApplicationContext();
        this.client = new MqttAndroidClient(context, HIVE_BROKER, UUID.randomUUID().toString(), Ack.AUTO_ACK);
        new Thread(() -> {
            try {
                connectMqtt();
                Log.i(TAG, "connected via new Thread in MqttService.java");
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        }).start();
       // this.client.setCallback(listener);

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

    public void setCallBackListener(CallBackListener listener){
        this.listener = listener;
    }

    public MqttConnectOptions getMqttOptions(){
        MqttConnectOptions options = new MqttConnectOptions();
        options.setPassword("password".toCharArray());
        options.setUserName("maji22");
        options.setCleanSession(true);
        options.setKeepAliveInterval(60000000);
        return options;
    }

    public void connectMqtt() throws MqttException {
        IMqttToken token = client.connect(getMqttOptions());
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i(TAG, "onSuccess");
                client.setCallback(new MqttCallbackExtended() {
                    @Override
                    public void connectComplete(boolean reconnect, String serverURI) {

                    }

                    @Override
                    public void connectionLost(Throwable cause) {
                        try {
                            connectMqtt();
                        } catch (MqttException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        JSONObject object = new JSONObject(new String(message.getPayload()));
                       String key= "";
                        if(object.toString().contains("Temperature")){
                            key = "Temperature";
                        } if(object.toString().contains("Relay")){
                            key = "Relay";
                        }
                        String msg = object.getString(key);

                        if(listener != null) {
                            listener.messageReceived(topic,key, msg);
                     //   } else {
                          //  Log.i(TAG, "listener is null");
                        }

                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {

                    }
                });
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i(TAG, "did not connect :(");
            }
        });
    }

   /* public void publish(String topic, String key, String value) throws JSONException, MqttException {
        JSONObject object = new JSONObject();
        object.put(key, value);
        MqttMessage message = new MqttMessage(object.toString().getBytes(StandardCharsets.UTF_8));
        client.publish(topic, message);
        Log.i(TAG, "published " + message + "to " + topic);
    }*/

    /**
     * publishing request with device MAC appended to topic name
     * @param deviceMac
     * @throws JSONException
     * @throws MqttException
     */
    public void publishTempRequest(String deviceMac) throws JSONException, MqttException {
        JSONObject object = new JSONObject();
        object.put("request", "1");
        MqttMessage message = new MqttMessage(object.toString().getBytes(StandardCharsets.UTF_8));
        client.publish(TEMP_PUB, message);
               // +"/"+deviceMac, message);
        Log.i(TAG, "published " + message + "to " + TEMP_PUB+"/"+deviceMac);
    }

    public void publishRelayStatusRequest() throws JSONException, MqttException {
        JSONObject object = new JSONObject();
        object.put("request", "1");
        MqttMessage message = new MqttMessage(object.toString().getBytes(StandardCharsets.UTF_8));
        client.publish(RELAY_STATUS_SUB, message);
        Log.i(TAG, "published " + message + "to " + RELAY_STATUS_SUB);
    }

    public void publishArmDevice() throws JSONException, MqttException {
        JSONObject object = new JSONObject();
        object.put("arm_value", "1");
        MqttMessage message = new MqttMessage(object.toString().getBytes(StandardCharsets.UTF_8));
        client.publish(PUB_ARM_DISARM_REQUEST, message);
        Log.i(TAG, "published " + message + "to " + PUB_ARM_DISARM_REQUEST);
    }
    public void publishDisarmDevice() throws JSONException, MqttException {
        JSONObject object = new JSONObject();
        object.put("arm_value", "0");
        MqttMessage message = new MqttMessage(object.toString().getBytes(StandardCharsets.UTF_8));
        client.publish(PUB_ARM_DISARM_REQUEST, message);
        Log.i(TAG, "published " + message + "to " + PUB_ARM_DISARM_REQUEST);
    }
    public void publishResetDevice() throws JSONException, MqttException {
        JSONObject object = new JSONObject();
        object.put("reset", "0");
        MqttMessage message = new MqttMessage(object.toString().getBytes(StandardCharsets.UTF_8));
        client.publish(RESET_REQUEST, message);
        Log.i(TAG, "published " + message + "to " + RESET_REQUEST);
    }

    public void subscribeToTemp()throws MqttException {
        client.subscribe(TEMP_SUB, 0);
        Log.i(TAG, "subscribed to: " + TEMP_SUB);
    }
    public void subscribeToRelay()throws MqttException {
        client.subscribe(RELAY_STATUS_SUB, 0);
        Log.i(TAG, "subscribed to: " + RELAY_STATUS_SUB);
    }

    public void unsubscribe(String topic){
        client.unsubscribe(topic);
        Log.i(TAG, "unsubscribed from: " + topic);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        //parcel.writeParcelable((Parcelable) MqttService.this, 0);
    }

}
