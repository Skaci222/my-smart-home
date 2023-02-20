package com.myproject.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.myproject.R;
import com.myproject.provisioning.EspMainActivity;
import com.myproject.provisioning.ProvisionLanding;
import com.myproject.rest.HiveDb;
import com.myproject.rest.JSONResponse;
import com.myproject.rest.MqttClient;
import com.myproject.room.Device;
import com.myproject.room.DeviceViewModel;
import com.myproject.room.Message;
import com.myproject.room.MessageViewModel;
import com.myproject.ui.adapters.RecyclerViewAdapter;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StartScreen extends AppCompatActivity implements RenameDialog.OnInputListener {

    public static final String TAG = "StartScreen: ";
    public static final String TAG_MQTT = "MQTT: ";
    public static final String HOST = "tcp://y94ieb.messaging.internetofthings.ibmcloud.com:1883";
    public static final String AZURE_HOST = "ssl://KACI.azure-devices.net:8883";
    public static final String TIME_INTERVAL = "time_interval";
    public static final String RELAY_VALUE = "relay_value";
    public static final String RELAY_CONTROL_TOPIC = "iot-2/cmd/control/fmt/json";
    public static final String RELAY_REQUEST_TOPIC = "iot-2/cmd/request/fmt/json";
    public static final String TEMP_TOPIC = "iot-2/type/Microcontroller/id/ESP32/evt/temperature/fmt/json";
    public static final String TEMP_STATUS = "iot-2/type/Microcontroller/id/ESP32/cmd/status/fmt/json";
    public static final String RELAY_STATUS_TOPIC = "iot-2/evt/relay/fmt/json";
    public static final String PUB_REQUEST_TOPIC = "iot-2/type/Microcontroller/id/ESP32/cmd/request/fmt/json";
    public static final String PUB_CONTROL_TOPIC = "iot-2/type/Microcontroller/id/ESP32/cmd/control/fmt/json";
    public static final String ALARM_TOPIC = "iot-2/type/Microcontroller/id/ESP32/evt/alarm/fmt/json";
    public static final String SHARED_PREFS = "shared_preferences";
    public static final String TIME_CONFIG = "time_config";
    public static final String AZURE_SUB = "devices/AndroidDevice/messages/devicebound/#";
    public static final String AZURE_PUB = "devices/AndroidDevice/messages/events/";
    public static final String HIVE_BROKER = "ssl://e7ea538cb0564a42b068269a96574848.s1.eu.hivemq.cloud:8883";
    public static final String MENU_SELECTION = "menu_selection";

    MenuItem menuItem;
    private int menuItemSelected = -1;
    private MqttAndroidClient client;
    private String clientId;

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private String timeText;

    private NotificationManagerCompat notificationManager;
    private static StartScreen ins;
    private ImageView ivConnected;
    private AlertDialog dialog;
    public String name;
    private String newName;
    private String deviceType;
    public TextView tvDevice;
    private TextView tvTimeConfig;
    private int deviceId;
    private String customSasToken;
    private HeaterFragment heaterFrag;
    TemperatureFragment tempfrag;


    private DeviceViewModel deviceViewModel;
    private MessageViewModel messageViewModel;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    HiveDb hiveDb;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        ins = this;

        notificationManager = NotificationManagerCompat.from(this);
        clientId = "AndroidDevice";
        // clientId = "a:y94ieb:andId-001";
        client = new MqttAndroidClient(getApplicationContext(), HIVE_BROKER,
                clientId, Ack.AUTO_ACK);
        mRecyclerView = findViewById(R.id.recycler_view);
        layoutManager = new GridLayoutManager(this, 2);
        mRecyclerViewAdapter = new RecyclerViewAdapter();
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        tempfrag = new TemperatureFragment();

        mRecyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (mRecyclerViewAdapter.getDeviceAt(position).getType().equals("heater")) {
                    if (tempfrag != null && tempfrag.isAdded()) {
                        getSupportFragmentManager().popBackStack();
                    }
                    heaterFrag = new HeaterFragment();
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_layout, heaterFrag, "heaterFrag")
                            .addToBackStack("heaterFrag")
                            .commit();

                    try {
                        heaterStatusRequest();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (mRecyclerViewAdapter.getDeviceAt(position).getType().equals("temperature")) {
                    if (heaterFrag != null && heaterFrag.isAdded()) {
                        getSupportFragmentManager().popBackStack();
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_layout, tempfrag)
                            .addToBackStack("tempFrag")
                            .commit();
                    try {
                        initRequest();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(StartScreen.this, "can't open next screen", Toast.LENGTH_SHORT).show();
                }

            }

            //change device name in recyclerView
            @Override
            public void onItemLongClick(Device device) {
                RenameDialog dialog = new RenameDialog();
                Bundle args = new Bundle();
                args.putString("name", device.getName());
                args.putString("type", device.getType());
                args.putInt("id", device.getId());
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "renameDialog");

            }
        });

        loadTimeConfig();
        //updateViews();

        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
        deviceViewModel.getAllDevices().observe(this, new Observer<List<Device>>() {
            @Override
            public void onChanged(List<Device> devices) {
                mRecyclerViewAdapter.setDevices(devices);
            }
        });

        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        messageViewModel.getAllMessages().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {

            }
        });

        //delete device item in list
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                deviceViewModel.delete(mRecyclerViewAdapter.getDeviceAt(viewHolder.getBindingAdapterPosition()));
                Toast.makeText(StartScreen.this, "Note deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(mRecyclerView);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://e7ea538cb0564a42b068269a96574848.s1.eu.hivemq.cloud:8883/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        hiveDb = retrofit.create(HiveDb.class);

       /* try {
            customSasToken = generateSasToken("KACI.azure-devices.net", "8lIhminu5ggTCZhRQJ8XRCZCLnangmrZ6z776ibcmGM=");
            Log.i(TAG, "generated custom SAS token: " + customSasToken);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        //customSasToken = "SharedAccessSignature sr=KACI.azure-devices.net&sig=fx1KVtSBp3YvPQy3%2BDllADiW7K%2BIJfKijBOinPfftjg%3D&se=1985201005";


        try {
            connectMQTT();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //generate custom SAS token
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String generateSasToken(String resourceUri, String key) throws Exception {
        // Token will expire in 10 years
        long expiry = Instant.now().getEpochSecond() + 315360000;

        String stringToSign = URLEncoder.encode(resourceUri, String.valueOf(StandardCharsets.UTF_8)) + "\n" + expiry;
        byte[] decodedKey = Base64.getDecoder().decode(key);

        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(decodedKey, "HmacSHA256");
        sha256HMAC.init(secretKey);
        Base64.Encoder encoder = Base64.getEncoder();

        String signature = new String(encoder.encode(
                sha256HMAC.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);

        String token = "SharedAccessSignature sr=" + URLEncoder.encode(resourceUri, String.valueOf(StandardCharsets.UTF_8))
                + "&sig=" + URLEncoder.encode(signature, StandardCharsets.UTF_8.name()) + "&se=" + expiry;

        return token;
    }


    //save time config from temp device
    public void saveTimeConfig() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
    }

    //load time config for temp device
    public void loadTimeConfig() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
    }

    //configure new device name/type and insert into list, then go to provision activity
    public void initialDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.initial_dialog, null);

        RadioButton rbTemp = v.findViewById(R.id.rbTemp);
        RadioButton rbMotion = v.findViewById(R.id.rbMotionDetect);
        RadioButton rbHeater = v.findViewById(R.id.rbHeater);
        EditText deviceName = v.findViewById(R.id.etNameDevice);

        builder.setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        name = deviceName.getText().toString();

                        if (rbTemp.isChecked()) {
                            deviceType = "temperature";
                        } else if (rbMotion.isChecked()) {
                            deviceType = "motion";
                        } else if (rbHeater.isChecked()) {
                            deviceType = "heater";
                        }
                        Device device = new Device(name, deviceType);
                        deviceViewModel.insert(device);
                        Intent intent = new Intent(StartScreen.this, EspMainActivity.class);
                        startActivity(intent);

                        Log.i(TAG, "new device added: " + name + ", " + deviceType);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });

        dialog = builder.create();
        dialog.show();

    }

    public static StartScreen getInstance() {
        return ins;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        menuItemSelected = sharedPreferences.getInt("timePrefs", menuItemSelected);

        int id = item.getItemId();

        switch (id) {
            case R.id.history:
                Intent in = new Intent(StartScreen.this, HistoricalTempData.class);
                startActivity(in);
                break;

            case R.id.actionAddDevice:
                initialDialog();
                break;

            case R.id.disconnect:
                client.disconnect();
                break;

        }
        return super.onOptionsItemSelected(item);

    }


    @Override
    protected void onResume() {
        super.onResume();
        stopService();
    }

    public void stopService() {
        Intent serviceIntent = (new Intent(this, MyService.class));
        stopService(serviceIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startForegroundService() {
        Intent serviceIntent = (new Intent(getApplicationContext(), MyService.class));
        startService(serviceIntent);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStop() {
        super.onStop();
        if (!isAppForeground(getApplicationContext())) {
            startForegroundService();
        }
    }

    //check if app is in foreground
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    //initial temperature reading
    public void initRequest() throws JSONException, MqttException {
        JSONObject requestObject = new JSONObject();
        requestObject.put("temperature", 1);
        requestObject.put("humidity", 1);
        MqttMessage mqttMessage = new MqttMessage(requestObject.toString().getBytes(StandardCharsets.UTF_8));
        client.publish(TEMP_STATUS, mqttMessage);
    }

    //check if heater is currently on or off
    public void heaterStatusRequest() throws JSONException {
        JSONObject statusRequest = new JSONObject();
        statusRequest.put(RELAY_VALUE, "1"); //String or int for value? What is key?
        MqttMessage mqttMessage = new MqttMessage(statusRequest.toString().getBytes(StandardCharsets.UTF_8));
        client.publish(RELAY_REQUEST_TOPIC, mqttMessage);
    }

    //request to turn heater on or off
    public void heaterControl(int control) throws JSONException {
        switch (control) {
            case 1:
                JSONObject heaterOn = new JSONObject();
                heaterOn.put(RELAY_VALUE, "1");
                MqttMessage onMessage = new MqttMessage(heaterOn.toString().getBytes(StandardCharsets.UTF_8));
                client.publish(RELAY_CONTROL_TOPIC, onMessage);
                Log.i(TAG_MQTT, "Published to turn heater on");
                break;
            case 0:
                JSONObject heaterOff = new JSONObject();
                heaterOff.put(RELAY_VALUE, "0");
                MqttMessage offMessage = new MqttMessage(heaterOff.toString().getBytes(StandardCharsets.UTF_8));
                client.publish(RELAY_CONTROL_TOPIC, offMessage);
                Log.i(TAG_MQTT, "Published to turn heater off");
                break;
        }

    }

    //request to update temperature values at certain times
    public void timeConfig(int time) throws JSONException {
        String timeInterval;
        switch (time) {
            case 1:
                JSONObject configShort = new JSONObject();
                configShort.put(TIME_INTERVAL, "1");
                timeInterval = configShort.getString(TIME_INTERVAL);
                MqttMessage mqttMessageShort = new MqttMessage(configShort.toString().getBytes(StandardCharsets.UTF_8));
                client.publish(PUB_CONTROL_TOPIC, mqttMessageShort);
                Log.i(TAG, "published " + mqttMessageShort.toString() + "to " + PUB_CONTROL_TOPIC);
                break;

            case 5:
                JSONObject configMed = new JSONObject();
                configMed.put(TIME_INTERVAL, "5");
                timeInterval = configMed.getString(TIME_INTERVAL);
                MqttMessage mqttMessageMed = new MqttMessage(configMed.toString().getBytes(StandardCharsets.UTF_8));
                client.publish(PUB_CONTROL_TOPIC, mqttMessageMed);
                Log.i(TAG, "published " + mqttMessageMed.toString() + "to " + PUB_CONTROL_TOPIC);
                break;

            case 10:
                JSONObject configLong = new JSONObject();
                configLong.put(TIME_INTERVAL, "10");
                timeInterval = configLong.getString(TIME_INTERVAL);
                MqttMessage mqttMessageLong = new MqttMessage(configLong.toString().getBytes(StandardCharsets.UTF_8));
                client.publish(PUB_CONTROL_TOPIC, mqttMessageLong);
                Log.i(TAG, "published " + mqttMessageLong.toString() + "to " + PUB_CONTROL_TOPIC);
                break;

        }
    }

    //interface method from RenameDialog to change device name
    @Override
    public void sendInput(String input, String type, int id) {
        if (TextUtils.isEmpty(input)) {
            Toast.makeText(ins, "Input field empty", Toast.LENGTH_SHORT).show();
        } else {
            newName = input;
            deviceType = type;
            deviceId = id;
            Device device = new Device(newName, deviceType);
            device.setId(deviceId);
            deviceViewModel.update(device);
            Toast.makeText(ins, "device name updated", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendTempNotification() {
        String title = "Temperature updated";
        String message = "Click to check your latest temperature reading";

        Intent activityIntent = new Intent(Intent.ACTION_MAIN);
        activityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        activityIntent.setClass(this, StartScreen.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);

        Notification notification = new NotificationCompat.Builder(this.getApplicationContext(), App.TEMPERATURE_NOTIFICATION)
                .setSmallIcon(R.drawable.ic_baseline_check_box_24)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .build();

        notificationManager.notify(1, notification);
        Log.i(TAG, "sent notification");

    }

    public void publish() throws MqttException {
    }


    public void subscribe() throws MqttException {
        client.subscribe(TEMP_TOPIC, 0);
        client.subscribe(RELAY_STATUS_TOPIC, 0);
        Log.i(TAG, "subscribed successfully");
    }

    public void mqttCallback() {
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connection lost: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String mTemperature;
                String mHumidity;
                String mRelayValue;
                TemperatureFragment temperatureFragment;
                HeaterFragment heaterFragment;

                JSONObject myMessage = new JSONObject(new String(message.getPayload()));

                switch (topic) {
                    case RELAY_STATUS_TOPIC:
                        mRelayValue = myMessage.getString("Relay");
                        if (mRelayValue.contains("1")) {
                            Toast.makeText(StartScreen.this, "heater is currently on", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "heater is on");
                            heaterFragment = HeaterFragment.newInstance("1");
                            getSupportFragmentManager().beginTransaction().replace(
                                            R.id.fragment_layout, heaterFragment).addToBackStack("heaterFrag")
                                    .commit();

                        } else if (mRelayValue.contains("0")) {
                            Log.i(TAG, "heater is off");
                            Toast.makeText(StartScreen.this, "heater is currently off", Toast.LENGTH_SHORT).show();
                            heaterFragment = HeaterFragment.newInstance("0");
                            getSupportFragmentManager().beginTransaction().replace(
                                            R.id.fragment_layout, heaterFragment).addToBackStack("heaterFrag")
                                    .commit();
                        }
                    case TEMP_TOPIC:
                        mTemperature = myMessage.getString("Temperature");
                        mTemperature = mTemperature.substring(0, 5);
                        mHumidity = myMessage.getString("Humidity");
                        mHumidity = mHumidity.substring(0, 5);

                        temperatureFragment = TemperatureFragment.newInstance(mTemperature, mHumidity);
                        Log.i(TAG, "temperature is : " + mTemperature);
                        Log.i(TAG, "humidity is : " + mHumidity);
                        getSupportFragmentManager().beginTransaction().replace(
                                        R.id.fragment_layout, temperatureFragment)
                                .addToBackStack("tempFrag")
                                .commit();
                        Message message1 = new Message("Temperature: " + mTemperature);
                        Message message2 = new Message("Humidity: " + mHumidity);
                        messageViewModel.insert(message1);
                        messageViewModel.insert(message2);


                        if (!isAppForeground(getApplicationContext())) {
                            sendTempNotification();
                        } else return;
                }

                Log.i(TAG_MQTT, "message arrived: " + myMessage);

                //IBM broker
                /*switch (topic) {
                    case TEMP_TOPIC:
                        mTemperature = myMessage.getString("Temperature");
                        mTemperature = mTemperature.substring(0, 5);
                        mHumidity = myMessage.getString("Humidity");
                        mHumidity = mHumidity.substring(0, 5);

                        temperatureFragment = TemperatureFragment.newInstance(mTemperature, mHumidity);
                        Log.i(TAG, "temperature is : " + mTemperature);
                        Log.i(TAG, "humidity is : " + mHumidity);
                        getSupportFragmentManager().beginTransaction().replace(
                                        R.id.fragment_layout, temperatureFragment)
                                .addToBackStack("tempFrag")
                                .commit();
                        Message message1 = new Message(mTemperature);
                        //Message message2 = new Message(mHumidity);
                        messageViewModel.insert(message1);
                        //messageViewModel.insert(message2);


                        if (!isAppForeground(getApplicationContext())) {
                            sendTempNotification();
                        } else return;

                    case ALARM_TOPIC:
                        if (!isAppForeground(getApplicationContext())) {
                            sendTempNotification();
                        } else return;
                        break;

                    default:
                }*/
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG_MQTT, "message sent");
            }
        });
    }


    public void connectMQTT() throws MqttException {

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        // options.setUserName("a-y94ieb-avdxw7gowr");
        // options.setPassword("@mcEx)FCK?RdA98czQ".toCharArray());
        //options.setUserName("KACI.azure-devices.net/AndroidDevice/?api-version=2021-04-12");
        //options.setPassword(customSasToken.toCharArray());
        options.setUserName("maji22");
        options.setPassword("password".toCharArray());

        Log.d(TAG, "connecting to server: " + HIVE_BROKER);

        IMqttToken token = client.connect(options);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i(TAG, "Connected");
                mqttCallback();
                Toast.makeText(StartScreen.this, "Connected to Cloud", Toast.LENGTH_SHORT).show();
                try {
                    subscribe();
                    getMqttClients();
                } catch (MqttException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i(TAG, "Did not connect: " + exception.getMessage() + " " + exception.getCause());
                Toast.makeText(StartScreen.this, "Disconnected from Cloud", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void getMqttClients() {
        Call<JSONResponse> call = hiveDb.getMqttClients();
        call.enqueue(new Callback<JSONResponse>() {
            @Override
            public void onResponse(Call<JSONResponse> call, Response<JSONResponse> response) {
                JSONResponse jsonResponse = response.body();
                if (!response.isSuccessful()) {
                    Log.i(TAG, "JSONresponse not successfull, code: " + response.code());
                    return;
                }
                List<MqttClient> clientList = new ArrayList<>(Arrays.asList(jsonResponse.getMqttClient()));
                for (MqttClient m : clientList) {
                    String content = "";
                    content += "ID: " + m.getId();
                    Log.i(TAG, content);
                }
            }

            @Override
            public void onFailure(Call<JSONResponse> call, Throwable t) {
                Log.i(TAG, "onFailure: " + t.getCause() + t.getMessage());
            }
        });
    }

}