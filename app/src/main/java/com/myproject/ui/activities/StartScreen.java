package com.myproject.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.app.AlertDialog;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.myproject.R;
import com.myproject.model.MqttMsg;
import com.myproject.logic.MqttService;
import com.myproject.logic.MyService;
import com.myproject.provisioning.EspMainActivity;
import com.myproject.receiverlistener.ArmDisarmListener;
import com.myproject.receiverlistener.DeleteDeviceListener;
import com.myproject.receiverlistener.DeviceClickListener;
import com.myproject.retrofit.MessageApi;
import com.myproject.retrofit.RetrofitService;
import com.myproject.room.Device;
import com.myproject.room.DeviceViewModel;
import com.myproject.room.Message;
import com.myproject.room.MessageViewModel;
import com.myproject.ui.adapters.RecyclerViewAdapter;
import com.myproject.ui.adapters.TemperatureRVAdapter;


import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StartScreen extends AppCompatActivity implements RenameDialog.OnInputListener, DeleteDeviceListener, TemperatureFragment.FabMenuListener, ArmDisarmListener,
        DeviceClickListener {

    public static final String TAG = "StartScreen: ";
    public static final String HOST = "tcp://y94ieb.messaging.internetofthings.ibmcloud.com:1883";
    public static final String TIME_INTERVAL = "time_interval";
    public static final String RELAY_VALUE = "relay_value";
    public static final String RELAY_CONTROL_TOPIC = "iot-2/cmd/control/fmt/json";
    public static final String RELAY_REQUEST_TOPIC = "iot-2/cmd/request/fmt/json";
    public static final String TEMP_PUB = "iot-2/cmd/temp/fmt/json";
    public static final String TEMP_SUB = "iot-2/evt/temperature/fmt/json";
    public static final String RELAY_STATUS_SUB = "iot-2/evt/relay/fmt/json";
    public static final String PUB_ARM_DISARM_REQUEST = "iot-2/cmd/accel/fmt/json";
    public static final String RESET_REQUEST = "iot-2/cmd/reset/fmt/json";
    public static final String PUB_CONTROL_TOPIC = "iot-2/type/Microcontroller/id/ESP32/cmd/control/fmt/json";
    public static final String ALARM_TOPIC = "iot-2/type/Microcontroller/id/ESP32/evt/alarm/fmt/json";
    public static final String SHARED_PREFS = "shared_preferences";
    public static final String HIVE_BROKER = "ssl://e7ea538cb0564a42b068269a96574848.s1.eu.hivemq.cloud:8883";

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private NotificationManagerCompat notificationManager;
    private ImageView ivBackground;
    private AlertDialog dialog;
    public String name;
    private String deviceName;
    private String newName;
    private String deviceType;
    private String deviceMac;
    private int deviceId;
    int mTemperature;
    String mHumidity;
    String mRelayValue;
    private List<Message> valuesByKey = new ArrayList<>();
    private List<Message> valuesByDay = new ArrayList<>();
    private Calendar calStart, calEnd;
    private long dateInMillis;
    private List<MqttMsg> mqttMessageList = new ArrayList<>();

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
    public MqttService mqttService;
    private BroadcastReceiver armReceiver, disarmReceiver, tempReceiver;
    private ScheduledExecutorService tempExecutorService1, tempExecutiveService2;
    private boolean isMenuExtended;
    private boolean isSubscribedTempTopic;

    private SecurityListFrag securityListFragment;

    private TemperatureListFrag temperatureListFrag;

    RetrofitService retrofitService;
    MessageApi messageApi;
    private Button securityDeviceBtn, tempDeviceBtn;

    private DateFormat dateFormat = new SimpleDateFormat("yy/mm/dd hh:mm:ss");

    private ArmDisarmListener armDisarmListener;

    private boolean isRequestingTempStatus;

    private Bundle bundle;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        getSupportActionBar().hide();

        ivBackground = findViewById(R.id.ivBackground);
        tvAddDevice = findViewById(R.id.tvFabAdd);
        addBtn = findViewById(R.id.floatingAddBtn);
        menuBtn = findViewById(R.id.floatingMenuBtn);
        addBtn.setVisibility(View.GONE);
        tvAddDevice.setVisibility(View.GONE);

        securityDeviceBtn = findViewById(R.id.secDeviceBtn);
        tempDeviceBtn = findViewById(R.id.tempDeviceBtn);
        securityListFragment = new SecurityListFrag();
        temperatureListFrag = new TemperatureListFrag();
        securityDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_layout, securityListFragment, "security_frag")
                        .addToBackStack("security_frag")
                        .commit();
                securityDeviceBtn.setVisibility(View.INVISIBLE);
                tempDeviceBtn.setVisibility(View.INVISIBLE);

            }
        });

        tempDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_layout, temperatureListFrag, "temp_lst_frag")
                        .addToBackStack("temp_list_frag")
                        .commit();
                securityDeviceBtn.setVisibility(View.INVISIBLE);
                tempDeviceBtn.setVisibility(View.INVISIBLE);
            }
        });
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


        //init();
        retrofitService = new RetrofitService();
        messageApi = retrofitService.getRetrofit().create(MessageApi.class);

        IntentFilter tempIntentFilter = new IntentFilter();
        tempIntentFilter.addAction("temp_low_notification");

        //old: turn switch on from alertReceiver (startHeater method in heaterFragment)
        //now: arm device from armReceiver (arm method in security fragment)
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("arm_device_notification");
        armReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    mqttService.publishArmDevice();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        };
        registerReceiver(armReceiver, intentFilter);

        //turn switch off from heatOffReceiver (stopHeater method in heaterFragment)
        //now: disarm device from disarmReceiver (disarm method in security fragment)
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("disarm_device_notification");
        disarmReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    mqttService.publishDisarmDevice();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        };
        registerReceiver(disarmReceiver, intentFilter2);
        try {
            mqttService = new MqttService(this);
        } catch (MqttException e) {
            e.printStackTrace();
        }

       /* new Thread(() -> {
            try {
                mqttService.connectMqtt();
                Log.i(TAG, "connected from new thread");
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        }).start();*/
        mqttService.setCallBackListener(new MqttService.CallBackListener() {
            @Override
            public void messageReceived(String topic, String key, String message) {
                TemperatureFragment temperatureFragment;
                SecurityFragment securityFrag;
                //Message msg = new Message(topic, key, message, new Date());
                // messageViewModel.insert(msg);
                //RetrofitService retrofitService = new RetrofitService();
                //MessageApi messageApi = retrofitService.getRetrofit().create(MessageApi.class);

                MqttMsg mqttMsg = new MqttMsg();
                mqttMsg.setTopic(topic);
                mqttMsg.setHashKey(key);
                mqttMsg.setValue(message);
                mqttMsg.setDate(dateFormat.format(new Date()));

                messageApi.save(mqttMsg)
                        .enqueue(new Callback<MqttMsg>() {
                            @Override
                            public void onResponse(Call<MqttMsg> call, Response<MqttMsg> response) {
                                Toast.makeText(StartScreen.this, "save successful", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<MqttMsg> call, Throwable t) {
                                Toast.makeText(StartScreen.this, "save failed", Toast.LENGTH_SHORT).show();
                                Logger.getLogger(MainTest.class.getName()).log(Level.SEVERE, "error", t);
                            }
                        });


                Log.i(TAG, "info received from listener: " + "Topic: " + topic + " Key: " + key + " Message: " + message);
                switch (topic) {
                    case RELAY_STATUS_SUB:
                        if (message.contains("1")) {
                            Toast.makeText(StartScreen.this, "device is armed", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "device is armed");
                            securityFrag = SecurityFragment.newInstance("1", deviceName);
                            getSupportFragmentManager().beginTransaction().replace(
                                    R.id.fragment_layout, securityFrag).addToBackStack("securityFrag").commit();
                        } else if (message.contains("0")) {
                            Log.i(TAG, "device is disarmed");
                            Toast.makeText(StartScreen.this, "device is disarmed", Toast.LENGTH_SHORT).show();
                            securityFrag = SecurityFragment.newInstance("0", deviceName);
                            getSupportFragmentManager().beginTransaction().replace(
                                    R.id.fragment_layout, securityFrag).addToBackStack("securityFrag").commit();
                        }
                    case TEMP_SUB:
                        String value = message.substring(0, 2);
                        //mTemperature = message.substring(0, 5);
                        //mHumidity = myMessage.getString("Humidity");
                        //mHumidity = mHumidity.substring(0, 5);
                        if (isMenuExtended) {
                            return;
                        } else {
                            temperatureFragment = TemperatureFragment.newInstance(value, deviceName);
                            Log.i(TAG, "temperature is : " + value);
                            //Log.i(TAG, "humidity is : " + mHumidity);
                            getSupportFragmentManager().beginTransaction()
                                    .add(R.id.fragment_layout, temperatureFragment)
                                    .addToBackStack("tempFrag")
                                    .commit();
                        }
                }
            }
        });

        notificationManager = NotificationManagerCompat.from(this);
        // clientId = "a:y94ieb:andId-001";
        mRecyclerView = findViewById(R.id.recycler_view);
        layoutManager = new GridLayoutManager(this, 3);
        mRecyclerViewAdapter = new RecyclerViewAdapter();
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        tempfrag = new TemperatureFragment();
        //heaterFrag = new SecurityFragment();

       /* mRecyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) throws InterruptedException, JSONException, MqttException {
                device = mRecyclerViewAdapter.getDeviceAt(position);
                deviceName = mRecyclerViewAdapter.getDeviceAt(position).getName();
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
                            .setCustomAnimations(R.anim.slide_in, R.anim.slide_in, R.anim.slide_out, R.anim.fade_out)
                            .replace(R.id.fragment_layout, heaterFrag, "heater_frag")
                            .addToBackStack("heater_frag")
                            .commit();
                    mqttService.subscribeToRelay();
                    mqttService.publishRelayStatusRequest();//find out Plad's key and relay topic
                    if (tempExecutorService1 != null) {
                        tempExecutorService1.shutdown();
                    }
                    if (tempExecutiveService2 != null) {
                        tempExecutiveService2.shutdown();
                    }

                } else if (mRecyclerViewAdapter.getDeviceAt(position).getType().equals("temperature")) {
                    if (heaterFrag != null && heaterFrag.isAdded()) {
                        getSupportFragmentManager().popBackStack();
                    }
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.slide_in, R.anim.slide_in, R.anim.slide_out, R.anim.fade_out)
                            .replace(R.id.fragment_layout, tempfrag, "temp_frag")
                            .addToBackStack("temp_frag")
                            .commit();
                    mqttService.subscribeToTemp();
                    isSubscribedTempTopic = true;
                    mqttService.publishTempRequest(device.getMac()); //figure out which method to remove, also in try/catch block below
                    Runnable tempRequest = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mqttService.publishTempRequest(device.getMac());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    tempExecutorService1 = Executors.newScheduledThreadPool(1);
                    tempExecutorService1.scheduleAtFixedRate(tempRequest, 0, 5, TimeUnit.SECONDS);

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
        });*/

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
        messageViewModel.getMessagesFromKey("Temperature").observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                //setMessagesByKey(messages);
                for (Message m : messages) {
                    //Log.i(TAG, "temperature message: " + m.getValue() + " " + m.getDate());
                }
            }
        });

        calStart = Calendar.getInstance();
        calEnd = Calendar.getInstance();
        calStart.set(2023, 04, 03, 00, 00, 00);
        calEnd.set(2023, 03, 28, 23, 59, 59);
        dateInMillis = calStart.getTimeInMillis();


        /*messageViewModel.getMessagesFromDate(dateInMillis, TEMP_SUB).observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                Log.i(TAG, "onChanged called from getMessageFromDate: " + ": " + messages.size());
                for(Message m: messages){
                    String key = m.getKey();
                    String msgVal = m.getValue();
                    Log.i(TAG, "messages from date: " + key + " , " + msgVal);

                }
            }
        });*/

        //delete device item in list
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //deviceViewModel.delete(mRecyclerViewAdapter.getDeviceAt(viewHolder.getAdapterPosition()));
                //Toast.makeText(StartScreen.this, "Note deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(mRecyclerView);

       // bundle = getIntent().getExtras();
       // deviceMac = bundle.getString("mac");
    }

    public void setMessagesByKey(List<Message> mMessages) {
        valuesByKey = mMessages;
    }

    public void setMessagesByDay(List<Message> mMessages) {
        valuesByDay = mMessages;
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
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = deviceName.getText().toString();
                //deviceMac = bundle.getString("mac"); //attempting to insert device in ProvisionActivity, may have to remove it
                                        //from StartScreen activity

                if (rbTemp.isChecked()) {
                    deviceType = "temperature";
                } else if (rbMotion.isChecked()) {
                    deviceType = "motion";
                } else if (rbHeater.isChecked()) {
                    deviceType = "heater";
                }

                if (TextUtils.isEmpty(deviceName.getText().toString())) {
                    Toast.makeText(StartScreen.this, "please enter all fields", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(deviceType)) {
                    Toast.makeText(StartScreen.this, "please enter all fields", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                   //Device device = new Device(name, deviceType, deviceMac);
                   // deviceViewModel.insert(device);

                    //tvEmptyRecyclerView.setVisibility(View.INVISIBLE);
                    Log.i(TAG, "name and type: " + name + ", " + deviceType);
                    Intent intent = new Intent(StartScreen.this, EspMainActivity.class);
                    Bundle b = new Bundle();
                    b.putString("name", name);
                    b.putString("type", deviceType);
                    intent.putExtras(b);
                    startActivity(intent);
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService();
        /**
         * if temperature_fragment is visible, start executor 2 in order to publish temperature status request
         */
        if (getSupportFragmentManager().findFragmentByTag("temperature_fragment") != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        mqttService.publishTempRequest(deviceMac); //device may not be recognized here
                        Log.i(TAG, "temp executor 2 is running");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            };

            tempExecutiveService2 = Executors.newScheduledThreadPool(1);
            tempExecutiveService2.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);
        } else{
            Log.i(TAG, "temperature fragment is null");
        }
        if (getSupportFragmentManager().findFragmentByTag("temperature_fragment") != null && !isSubscribedTempTopic) {
            try {
                mqttService.subscribeToTemp();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
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
            /**
             * if temperature_fragment is not null and if app is publishing to request temperature status, shut down
             * executor 1 and shut down executor 2 if it is running
             */
            if(getSupportFragmentManager().findFragmentByTag("temperature_fragment") != null && isRequestingTempStatus){
                tempExecutorService1.shutdown();
                Log.i(TAG, "shutting down executor 1...");

                if(tempExecutiveService2 != null) {
                    tempExecutiveService2.shutdown();
                    Log.i(TAG, "shutting down executor 2...");

                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
         * stop foreground service when app is closed, should probably remove this code
         */
        Intent serviceIntent = (new Intent(this, MyService.class));
        stopService(serviceIntent);

    }

    /**
     *  check if app is in foreground
     */
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
        getSupportFragmentManager().popBackStack("temperature_fragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if (isRequestingTempStatus) {
            mqttService.unsubscribe("iot-2/evt/temperature/fmt/json");
            isSubscribedTempTopic = false;
           // tempfrag.tvTempValue.setVisibility(View.GONE);

            /**
             * if temp executors 1 or 2 are in use, shut them down
             */
            if (tempExecutorService1 != null) {
                Log.i(TAG, "tempExecService is not null");
                tempExecutorService1.shutdown();
                Log.i(TAG, "shutting down temp executor 1");
            }
            if (tempExecutiveService2 != null) {
                tempExecutiveService2.shutdown();
                Log.i(TAG, "shutting down temp executor 2");

            }
        }
        if(securityListFragment.isAdded()){
            getSupportFragmentManager().popBackStack("security_fragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        }

        securityDeviceBtn.setVisibility(View.VISIBLE);
        tempDeviceBtn.setVisibility(View.VISIBLE);

    }

    //interface method from RenameDialog to change device name
    @Override
    public void sendInput(String input, String type, int id) {
        if (TextUtils.isEmpty(input)) {
            Toast.makeText(this, "Input field empty", Toast.LENGTH_SHORT).show();
        } else {
            newName = input;
            deviceType = type;
            deviceId = id;
            Device device = new Device(newName, deviceType, deviceMac);
            device.setId(deviceId);
            deviceViewModel.update(device);
            Toast.makeText(this, "device name updated", Toast.LENGTH_SHORT).show();

        }
    }

    public void init() {
        RetrofitService retrofitService = new RetrofitService();
        MessageApi messageApi = retrofitService.getRetrofit().create(MessageApi.class);

        MqttMsg mqttMsg = new MqttMsg();
        mqttMsg.setTopic("topic");
        mqttMsg.setHashKey("key");
        mqttMsg.setValue("value");

        messageApi.save(mqttMsg)
                .enqueue(new Callback<MqttMsg>() {
                    @Override
                    public void onResponse(Call<MqttMsg> call, Response<MqttMsg> response) {
                        Toast.makeText(StartScreen.this, "save successful", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<MqttMsg> call, Throwable t) {
                        Toast.makeText(StartScreen.this, "save failed", Toast.LENGTH_SHORT).show();
                        Logger.getLogger(MainTest.class.getName()).log(Level.SEVERE, "error", t);
                    }
                });



    }

   /* public void sendTempNotification() {
        String title = "Temperature updated";
        String message = "Furnace temperature is " + tempMsg + " degrees Celsius";

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

    }*/

    @Override
    public void deleteDevice() throws JSONException, MqttException {
        deviceViewModel.delete(device);
        mqttService.publishResetDevice();
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

    }

  /*  @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.i(TAG, "connected from StartScreen");
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.i(TAG, "connection lost");
      if(isAppForeground(this)){
          try {
              mqttService.connectMqtt();
          } catch (MqttException e) {
              e.printStackTrace();
          }
         /* if(tempfrag.isAdded()){
              try {
                  mqttService.publish("iot-2/cmd/temp/fmt/json", "request", "1");
              } catch (JSONException e) {
                  e.printStackTrace();
              } catch (MqttException e) {
                  e.printStackTrace();
              }
          }*/
    /*  }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.i(TAG, "message arrived: " + message + "from topic: " + topic);
        int mTemperature;
        String mHumidity;
        String mRelayValue;
        TemperatureFragment temperatureFragment;
        HeaterFragment heaterFragment;

        JSONObject myMessage = new JSONObject(new String(message.getPayload()));

        switch (topic) {
            case RELAY_STATUS_SUB:
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
            case TEMP_SUB:
                mTemperature = myMessage.getInt("Temperature");
                tempMsg = String.valueOf(mTemperature);
                Message msg = new Message(TEMP_SUB, "Temperature", tempMsg, new Date());
                messageViewModel.insert(msg);
                //mTemperature = mTemperature.substring(0, 5);
                //mHumidity = myMessage.getString("Humidity");
                //mHumidity = mHumidity.substring(0, 5);
                if(isMenuExtended){
                    return;
                } else {
                    temperatureFragment = TemperatureFragment.newInstance(tempMsg, deviceName);
                    Log.i(TAG, "temperature is : " + mTemperature);
                    //Log.i(TAG, "humidity is : " + mHumidity);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_layout, temperatureFragment)
                            .addToBackStack("tempFrag")
                            .commit();
                }
            case "low temp":
                NotificationHelper notificationHelper = new NotificationHelper(this);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i(TAG, "delivered the message");
    }*/


    @Override
    public void fabExtended(boolean b) {
        if (b == true) {
            isMenuExtended = true;
        } else {
            isMenuExtended = false;
        }
    }

    @Override
    public void armDevice() throws MqttException, JSONException {
        mqttService.publishArmDevice();
    }

    @Override
    public void disarmDevice() throws MqttException, JSONException {
        mqttService.publishDisarmDevice();
    }

    @Override
    public void onDeviceClicked(String mac) {
        Log.i(TAG, "onDeviceClicked, mac is: "+ mac);
        isRequestingTempStatus = true;
        //deviceMac = mac;

        /**
         * Runs temp executor 1 to publish temperature request and display value in TemperatureFragment when visible
         */
        Runnable tempRequest = new Runnable() {
            @Override
            public void run() {
                try {
                    mqttService.publishTempRequest(mac);
                    Log.i(TAG, "temp executor 1 running");

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        };
        tempExecutorService1 = Executors.newScheduledThreadPool(1);
        tempExecutorService1.scheduleAtFixedRate(tempRequest, 0, 5, TimeUnit.SECONDS);

    }
}