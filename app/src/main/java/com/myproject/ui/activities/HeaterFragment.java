package com.myproject.ui.activities;

import static android.content.Context.ALARM_SERVICE;
import static com.myproject.ui.activities.StartScreen.RELAY_CONTROL_TOPIC;
import static com.myproject.ui.activities.StartScreen.RELAY_VALUE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.myproject.R;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class HeaterFragment extends Fragment{

    public static final String STATUS = "status";

    private TextView tvStatus, tvShowTime, tvTimeTitle, tvTimeTitle2, fabTvDeleteDevice, fabTvRelayData, fabTvTimeConfig, tvDeviceName;
    TimePickerDialog.OnTimeSetListener listener;
    private Calendar c;
    private String heaterStatus;
    private String timeText;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private DeleteDeviceListener deleteDeviceListener;
    private ExtendedFloatingActionButton fabMenuRelayFrag;
    private FloatingActionButton fabTimeConfig, fabRelayData, fabDeleteDevice;
    private boolean isFabExtended = false;
    private ImageView ivTopIcon;
    private String name;
    public MqttService mqttService;
    private AlertReceiver receiver;



    public static HeaterFragment newInstance(String status) {
        Bundle args = new Bundle();
        args.putString("status", status);
        HeaterFragment heaterFragment = new HeaterFragment();
        heaterFragment.setArguments(args);
        return heaterFragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.heater_frag, container, false);

        ivTopIcon = v.findViewById(R.id.ivTopIcon);
        tvDeviceName = v.findViewById(R.id.textViewTitle);
        tvStatus = v.findViewById(R.id.tvHeatOnOffStatus);
        tvTimeTitle = v.findViewById(R.id.tvTimeTitle);
        tvTimeTitle2 = v.findViewById(R.id.textView1);
        tvShowTime = v.findViewById(R.id.tvShowTime);
        c = Calendar.getInstance();
        sharedPreferences = getActivity().getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        fabMenuRelayFrag = v.findViewById(R.id.fabMenuRelayFrag);
        fabTimeConfig = v.findViewById(R.id.fabTimeConfig);
        fabRelayData = v.findViewById(R.id.fabRelayData);
        fabDeleteDevice = v.findViewById(R.id.fabDeleteDevice);
        fabTvTimeConfig = v.findViewById(R.id.fabTvTimeConfig);
        fabTvRelayData = v.findViewById(R.id.fabTvRelayData);
        fabTvDeleteDevice = v.findViewById(R.id.fabTvDeleteDevice);
        fabTimeConfig.hide();
        fabRelayData.hide();
        fabDeleteDevice.hide();
        fabTvTimeConfig.setVisibility(View.GONE);
        fabTvRelayData.setVisibility(View.GONE);
        fabTvDeleteDevice.setVisibility(View.GONE);
        fabMenuRelayFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFabExtended) {
                    tvShowTime.setAlpha(0.1f);
                    tvTimeTitle.setAlpha(0.1f);
                    tvTimeTitle2.setAlpha(0.1f);
                    tvStatus.setAlpha(0.1f);
                    fabTimeConfig.show();
                    fabTvTimeConfig.setVisibility(View.VISIBLE);
                    fabRelayData.show();
                    fabTvRelayData.setVisibility(View.VISIBLE);
                    fabDeleteDevice.show();
                    fabTvDeleteDevice.setVisibility(View.VISIBLE);
                    isFabExtended = true;
                } else {
                    tvShowTime.setAlpha(1f);
                    tvTimeTitle.setAlpha(1f);
                    tvTimeTitle2.setAlpha(1f);
                    tvStatus.setAlpha(1f);
                    fabTimeConfig.hide();
                    fabTvTimeConfig.setVisibility(View.GONE);
                    fabRelayData.hide();
                    fabTvRelayData.setVisibility(View.GONE);
                    fabDeleteDevice.hide();
                    fabTvDeleteDevice.setVisibility(View.GONE);
                    isFabExtended = false;
                }
            }
        });
        fabTimeConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth,
                        listener, hour, minute, false);
                timePickerDialog.show();
                fabTimeConfig.hide();
                fabDeleteDevice.hide();
                fabRelayData.hide();
                fabTvTimeConfig.setVisibility(View.INVISIBLE);
                fabTvDeleteDevice.setVisibility(View.INVISIBLE);
                fabTvRelayData.setVisibility(View.INVISIBLE);
                tvShowTime.setAlpha(1f);
                tvTimeTitle.setAlpha(1f);
                tvTimeTitle2.setAlpha(1f);
                tvStatus.setAlpha(1f);
            }
        });
        fabRelayData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabTimeConfig.hide();
                fabDeleteDevice.hide();
                fabRelayData.hide();
                fabTvTimeConfig.setVisibility(View.INVISIBLE);
                fabTvDeleteDevice.setVisibility(View.INVISIBLE);
                fabTvRelayData.setVisibility(View.INVISIBLE);
                tvShowTime.setAlpha(1f);
                tvTimeTitle.setAlpha(1f);
                tvTimeTitle2.setAlpha(1f);
                tvStatus.setAlpha(1f);
            }
        });
        fabDeleteDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDeviceListener = (DeleteDeviceListener) getActivity();
                try {
                    deleteDeviceListener.deleteDevice();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                fabTimeConfig.hide();
                fabDeleteDevice.hide();
                fabRelayData.hide();
                fabTvTimeConfig.setVisibility(View.INVISIBLE);
                fabTvDeleteDevice.setVisibility(View.INVISIBLE);
                fabTvRelayData.setVisibility(View.INVISIBLE);
                tvShowTime.setAlpha(1f);
                tvTimeTitle.setAlpha(1f);
                tvTimeTitle2.setAlpha(1f);
                tvStatus.setAlpha(1f);
            }
        });


        if (getArguments() != null) {
            name = getArguments().getString("deviceName");
            tvDeviceName.setText(name);
            mqttService = getArguments().getParcelable("mqtt");
            heaterStatus = getArguments().getString("status");
            if (heaterStatus != null) {
                if (heaterStatus.equals("1")) {
                    tvStatus.setText("Status: ON");
                } else if (heaterStatus.equals("0")) {
                    tvStatus.setText("Status: OFF");
                }
            }
        }

        listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                c.set(Calendar.HOUR_OF_DAY, i);
                c.set(Calendar.MINUTE, i1);
                startHeater(c);
                timeText = "";
                timeText += DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
                tvShowTime.setText(timeText);
                editor.putString("timeText", tvShowTime.getText().toString());
                editor.apply();
                Log.i("TAG", timeText);
                // c.add(Calendar.MINUTE, 1);
                // Log.i("TAG", "Heater will turn off at " + DateFormat.getTimeInstance().format(c.getTime()));
                stopHeater(c);

            }
        };


        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        String savedValue = this.getActivity().getSharedPreferences("shared_prefs",
                Context.MODE_PRIVATE).getString("timeText", "empty");
        if (!savedValue.equals("empty")) {
            tvShowTime.setText(savedValue);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void startHeater(Calendar cal) {
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), AlertReceiver.class);
        Bundle b = new Bundle();
        b.putParcelable("mqtt", mqttService);
        intent.putExtras(b);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 2, intent, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        Log.i("HEATERFRG", "startHeater activated");

    }

    public void stopHeater(Calendar cal) {
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), HeatOffReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 3, intent, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        c.add(Calendar.MINUTE, 1);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);

    }

}