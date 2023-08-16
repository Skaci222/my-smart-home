package com.myproject.ui.activities;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.myproject.R;
import com.myproject.logic.MqttService;
import com.myproject.receiverlistener.ArmDisarmListener;
import com.myproject.receiverlistener.ArmReceiver;
import com.myproject.receiverlistener.DeleteDeviceListener;
import com.myproject.receiverlistener.DisarmReceiver;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import java.text.DateFormat;
import java.util.Calendar;

public class SecurityFragment extends Fragment{

    public static final String STATUS = "status";

    private TextView tvStatus, tvShowTime, tvTimeTitle, fabTvDeleteDevice, fabTvRelayData, fabTvTimeConfig, tvDeviceName;
    private TimePickerDialog.OnTimeSetListener listener;
    private Calendar c;
    private String heaterStatus;
    private String timeText;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private DeleteDeviceListener deleteDeviceListener;
    private ExtendedFloatingActionButton fabMenuRelayFrag;
    private FloatingActionButton fabTimeConfig, fabRelayData, fabDeleteDevice;
    private Button btnArm;
    private boolean isFabExtended = false;
    private ImageView ivTopIcon;
    private String name;
    public MqttService mqttService;
    private ArmReceiver receiver;
    private ArmDisarmListener armDisarmListener;

    public static SecurityFragment newInstance(String status, String name) {
        Bundle args = new Bundle();
        args.putString("status", status);
        args.putString("deviceName", name);
        SecurityFragment heaterFragment = new SecurityFragment();
        heaterFragment.setArguments(args);
        return heaterFragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.security_frag, container, false);

        ivTopIcon = v.findViewById(R.id.ivTopIcon);
        tvDeviceName = v.findViewById(R.id.textViewTitle);
        tvStatus = v.findViewById(R.id.tvHeatOnOffStatus);
        btnArm = v.findViewById(R.id.btnArm);
        tvTimeTitle = v.findViewById(R.id.tvTimeTitle);
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
                    tvStatus.setAlpha(0.1f);
                    btnArm.setAlpha(0.1f);
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
                    tvStatus.setAlpha(1f);
                    btnArm.setAlpha(1f);
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
                tvStatus.setAlpha(1f);
            }
        });

        btnArm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                armDisarmListener = (ArmDisarmListener) getActivity();
                if(btnArm.getText().equals("Arm Device")){
                    try {
                        armDisarmListener.armDevice();
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    btnArm.setText("Disarm Device");
                } else if(btnArm.getText().equals("Disarm Device")){
                    try {
                        armDisarmListener.disarmDevice();
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    btnArm.setText("Arm Device");
                }

            }
        });


        if (getArguments() != null) {
            name = getArguments().getString("deviceName");
            mqttService = getArguments().getParcelable("mqtt");
            heaterStatus = getArguments().getString("status");
            if (heaterStatus != null) {
                if (heaterStatus.equals("1")) {
                    tvStatus.setText("Status: ARMED");
                } else if (heaterStatus.equals("0")) {
                    tvStatus.setText("Status: DISARMED");
                }
            }
            tvDeviceName.setText(name);
        }

        listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                c.set(Calendar.HOUR_OF_DAY, i);
                c.set(Calendar.MINUTE, i1);
                arm(c);
                timeText = "";
                timeText += DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
                tvShowTime.setText(timeText);
                editor.putString("timeText", tvShowTime.getText().toString());
                editor.apply();
                Log.i("TAG", timeText);
                disarm(c);

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

    public void arm(Calendar cal) {
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), ArmReceiver.class);
        Bundle b = new Bundle();
        b.putParcelable("mqtt", mqttService);
        intent.putExtras(b);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 2, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        Log.i("SecurityFrag", "device armed");

    }

    public void disarm(Calendar cal) {
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), DisarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 3, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        c.add(Calendar.MINUTE, 1);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        Log.i("SecurityFrag", "device disarmed");


    }

}