package com.myproject.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.myproject.R;
import com.myproject.provisioning.EspMainActivity;
import com.myproject.room.Device;
import com.myproject.room.DeviceViewModel;
import com.myproject.room.Message;
import com.myproject.room.MessageViewModel;
import com.myproject.ui.adapters.RecyclerViewAdapter;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StartScreen extends AppCompatActivity implements RenameDialog.OnInputListener, DeleteDeviceListener, MqttCallbackExtended {

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


    private int menuItemSelected = -1;
    private MqttAndroidClient client;
    private String clientId;

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private NotificationManagerCompat notificationManager;
    private static StartScreen ins;
    private ImageView ivBackground;
    private AlertDialog dialog;
    public String name;
    private String newName;
    private String deviceType;
    private String mac;
    private int deviceId;

    private HeaterFragment heaterFrag;
    private TemperatureFragment tempfrag;
    private ExtendedFloatingActionButton menuBtn;
    private FloatingActionButton addBtn;
    private TextView tvAddDevice;
    private boolean fabItemsVisible = false;

    private DeviceViewModel deviceViewModel;
    private MessageViewModel messageViewModel;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Device device;
    private List<Message> allMessages = new ArrayList<>();
    public MqttService mqttService;
    private BroadcastReceiver onReceiver, offReceiver, messageReceiver;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        ins = this;
        getSupportActionBar().hide();

        ivBackground = findViewById(R.id.ivBackground);
        tvAddDevice = findViewById(R.id.tvFabAdd);
        addBtn = findViewById(R.id.floatingAddBtn);
        menuBtn = findViewById(R.id.floatingMenuBtn);
        addBtn.setVisibility(View.GONE);
        tvAddDevice.setVisibility(View.GONE);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fabItemsVisible) {
                    ivBackground.setAlpha(0.1f);
                    addBtn.show();
                    tvAddDevice.setVisibility(View.VISIBLE);
                    fabItemsVisible = true;

                } else {
                    ivBackground.setAlpha(.25f);
                    addBtn.hide();
                    tvAddDevice.setVisibility(View.GONE);
                    fabItemsVisible = false;
                }
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initialDialog();
                addBtn.hide();
                tvAddDevice.setVisibility(View.GONE);
                ivBackground.setAlpha(.25f);
            }

        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("switch_on_notification");
        onReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    mqttService.publish(RELAY_CONTROL_TOPIC, RELAY_VALUE, "1");
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        };
        registerReceiver(onReceiver, intentFilter);

        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("switch_off_notification");
        offReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    mqttService.publish(RELAY_CONTROL_TOPIC, RELAY_VALUE, "0");
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        };
        registerReceiver(offReceiver, intentFilter2);
        mqttService = new MqttService(this, this);
        try {
            mqttService.connectMqtt();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        notificationManager = NotificationManagerCompat.from(this);
        clientId = "AndroidDevice";
        // clientId = "a:y94ieb:andId-001";
        mRecyclerView = findViewById(R.id.recycler_view);
        layoutManager = new GridLayoutManager(this, 3);
        mRecyclerViewAdapter = new RecyclerViewAdapter();
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        tempfrag = new TemperatureFragment();
        heaterFrag = new HeaterFragment();


        mRecyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) throws InterruptedException, JSONException, MqttException {
                device = mRecyclerViewAdapter.getDeviceAt(position);
                String deviceName = mRecyclerViewAdapter.getDeviceAt(position).getName();
                deviceId = mRecyclerViewAdapter.getDeviceAt(position).getId();
                Bundle b = new Bundle();
                b.putString("deviceName", deviceName);
                b.putInt("id", deviceId);
                b.putParcelable("mqtt", mqttService);
                heaterFrag.setArguments(b);
                tempfrag.setArguments(b);


                if (mRecyclerViewAdapter.getDeviceAt(position).getType().equals("heater")) {
                    if (tempfrag != null && tempfrag.isAdded()) {
                        getSupportFragmentManager().popBackStack();
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_layout, heaterFrag)
                            .addToBackStack(null)
                            .commit();
                    mqttService.subscribe(RELAY_STATUS_TOPIC);
                    mqttService.publish(RELAY_VALUE, "relay", "relay message");

                } else if (mRecyclerViewAdapter.getDeviceAt(position).getType().equals("temperature")) {
                    if (heaterFrag != null && heaterFrag.isAdded()) {
                        getSupportFragmentManager().popBackStack();
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_layout, tempfrag)
                            .addToBackStack(null)
                            .commit();
                    mqttService.subscribe(TEMP_TOPIC);
                    mqttService.subscribe("iot-2/evt/temperature/fmt/json");
                    mqttService.publish("iot-2/cmd/temp/fmt/json", "request", "1");

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

        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
        deviceViewModel.getAllDevices().observe(this, new Observer<List<Device>>() {
            @Override
            public void onChanged(List<Device> devices) {
                mRecyclerViewAdapter.setDevices(devices);
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
                deviceViewModel.delete(mRecyclerViewAdapter.getDeviceAt(viewHolder.getAdapterPosition()));
                Toast.makeText(StartScreen.this, "Note deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(mRecyclerView);

      /* try {
            connectMQTT();
        } catch (MqttException e) {
            e.printStackTrace();
        }*/

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
                        Device device = new Device(name, deviceType, mac);
                        deviceViewModel.insert(device);
                        //tvEmptyRecyclerView.setVisibility(View.INVISIBLE);
                        Log.i(TAG, "name and type: " + name + ", " + deviceType);
                        Intent intent = new Intent(StartScreen.this, EspMainActivity.class);
                        Bundle b = new Bundle();
                        b.putString("name", name);
                        b.putString("type", deviceType);
                        intent.putExtras(b);
                        startActivity(intent);
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

   /* public static StartScreen getInstance() {
        return ins;
    }*/

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
                try {
                    client.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.unProv:
                try {
                    removeProvisioning();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent serviceIntent = (new Intent(this, MyService.class));
        stopService(serviceIntent);
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
        //client.publish(TEMP_STATUS, mqttMessage);
    }

    //check if heater is currently on or off
    public void heaterStatusRequest() throws JSONException {
        JSONObject statusRequest = new JSONObject();
        statusRequest.put(RELAY_VALUE, "1"); //String or int for value? What is key?
        MqttMessage mqttMessage = new MqttMessage(statusRequest.toString().getBytes(StandardCharsets.UTF_8));
        //client.publish(RELAY_REQUEST_TOPIC, mqttMessage);
    }

    //request to turn heater on or off
    public void heaterControl(int control) throws JSONException, MqttException {
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
    public void timeConfig(int time) throws JSONException, MqttException {
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

    //remove provisioning from device
    public void removeProvisioning() throws JSONException, MqttException {
        JSONObject object = new JSONObject();
        object.put("reset", 0);
        MqttMessage mqttMessage = new MqttMessage(object.toString().getBytes(StandardCharsets.UTF_8));
        client.publish("iot-2/cmd/reset/fmt/json", mqttMessage);
        Log.i(TAG, "published " + mqttMessage + "to " + "iot-2/cmd/reset/fmt/json");

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
            Device device = new Device(newName, deviceType, mac);
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
                .setSmallIcon(R.drawable.ic_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .build();

        notificationManager.notify(1, notification);
        Log.i(TAG, "sent notification");

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

    @Override
    public void deleteDevice() throws JSONException, MqttException {
        deviceViewModel.delete(device);
        mqttService.publish("iot-2/cmd/reset/fmt/json", "reset", "0");
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.i(TAG, "connected from StartScreen");
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.i(TAG, "connection lost");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.i(TAG, "message arrived: " +message + "from topic: " +topic);
        int mTemperature;
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
            case "iot-2/evt/temperature/fmt/json":
                mTemperature = myMessage.getInt("Temperature");
                //mTemperature = mTemperature.substring(0, 5);
                //mHumidity = myMessage.getString("Humidity");
                //mHumidity = mHumidity.substring(0, 5);

                temperatureFragment = TemperatureFragment.newInstance(mTemperature);
                Log.i(TAG, "temperature is : " + mTemperature);
                //Log.i(TAG, "humidity is : " + mHumidity);
                getSupportFragmentManager().beginTransaction().replace(
                                R.id.fragment_layout, temperatureFragment)
                        .addToBackStack("tempFrag")
                        .commit();

                if (!isAppForeground(getApplicationContext())) {
                    sendTempNotification();
                } else return;
        }


    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i(TAG, "delivered the message");
    }
}