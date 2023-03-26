package com.myproject.room;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MessageViewModel extends AndroidViewModel {

    private MessageRepo messageRepo;
    private LiveData<List<Message>> allMessages;
    public static final String TAG = "ViewModelClass";
    public MqttAndroidClient mqttAndroidClient;
    private Context context;
    private ArrayList<Message> values = new ArrayList<>();

    public MessageViewModel(@NonNull Application application) {
        super(application);
        messageRepo = new MessageRepo(application);
        allMessages = messageRepo.getAllMessages();
        context = application.getApplicationContext();
    }

    public void insert(Message message) {
        messageRepo.insert(message);
    }

    public void update(Message message) {
        messageRepo.update(message);
    }

    public void delete(Message message) {
        messageRepo.delete(message);
    }

    public void deleteAllMessages() {
        messageRepo.deleteAllMessages();
    }

    public LiveData<List<Message>> getAllMessages() {
        return allMessages;
    }


}