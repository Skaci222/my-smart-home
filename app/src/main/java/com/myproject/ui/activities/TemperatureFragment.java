package com.myproject.ui.activities;

import static com.myproject.ui.activities.StartScreen.PUB_CONTROL_TOPIC;
import static com.myproject.ui.activities.StartScreen.RELAY_CONTROL_TOPIC;
import static com.myproject.ui.activities.StartScreen.TEMP_STATUS;
import static com.myproject.ui.activities.StartScreen.TIME_INTERVAL;

import android.app.AlertDialog;
import android.app.FragmentContainer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.myproject.R;
import com.myproject.room.Device;
import com.myproject.room.DeviceViewModel;
import com.myproject.room.Message;
import com.myproject.room.MessageViewModel;
import com.myproject.ui.adapters.RecyclerViewAdapter;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class TemperatureFragment extends Fragment  {

    private TextView tvTempValue, tvHumidityValue, deviceName;

    private RecyclerViewAdapter.MyViewHolder adapter;
    public static final String ARG_TEMP = "Temperature";
    public static final String ARG_HUM = "Humidity";
    private String tempText;
    private String humText;
    private int tempValue;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int itemSelected;
    private String name;
    private int id;
    private String updatedName;
    private ExtendedFloatingActionButton fabMenuTempFrag;
    private FloatingActionButton fabTimeConfig, fabTempData, fabDeleteTempDevice;
    private TextView fabTvTimeConfig, fabTvTempData, fabTvDeleteTempDevice, tvTemp, tvHumidity;
    private boolean isFabExtended = false;
    private AlertDialog dialog;
    private MqttService mqttService;
    public DeleteDeviceListener deleteDeviceListener;



    public static TemperatureFragment newInstance(String temp, String humidity){
        Bundle args = new Bundle();
        TemperatureFragment fragment = new TemperatureFragment();
        args.putString(ARG_TEMP, temp);
        args.putString(ARG_HUM, humidity);
        fragment.setArguments(args);
        return fragment;
    }
    public static TemperatureFragment newInstance(int temp){
        Bundle args = new Bundle();
        TemperatureFragment fragment = new TemperatureFragment();
        args.putInt(ARG_TEMP, temp);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.temp_frag, container, false);

        tvTempValue = v.findViewById(R.id.tvTempValue);
        tvHumidityValue = v.findViewById(R.id.tvHumidityValue);
        tvTemp = v.findViewById(R.id.tvTemp);
        tvHumidity = v.findViewById(R.id.tvHumidity);
        deviceName = v.findViewById(R.id.textViewTitle);
        fabMenuTempFrag = v.findViewById(R.id.fabMenuTempFrag);
        fabTimeConfig = v.findViewById(R.id.fabTimeConfig);
        fabTempData = v.findViewById(R.id.fabTempData);
        fabDeleteTempDevice = v.findViewById(R.id.fabDeleteTempDevice);
        fabTvTimeConfig = v.findViewById(R.id.fabTvTimeConfig);
        fabTvTempData = v.findViewById(R.id.fabTvTempData);
        fabTvDeleteTempDevice = v.findViewById(R.id.fabTvDeleteTempDevice);
        fabTimeConfig.hide();
        fabTempData.hide();
        fabDeleteTempDevice.hide();
        fabTvTimeConfig.setVisibility(View.GONE);
        fabTvTempData.setVisibility(View.GONE);
        fabTvDeleteTempDevice.setVisibility(View.GONE);

        fabMenuTempFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFabExtended){
                    tvTemp.setAlpha(0.1f);
                    tvHumidity.setAlpha(0.1f);
                    tvHumidityValue.setAlpha(0.1f);
                    tvTempValue.setAlpha(0.1f);

                    fabTimeConfig.show();
                    fabTvTimeConfig.setVisibility(View.VISIBLE);
                    fabTempData.show();
                    fabTvTempData.setVisibility(View.VISIBLE);
                    fabDeleteTempDevice.show();
                    fabTvDeleteTempDevice.setVisibility(View.VISIBLE);
                    isFabExtended = true;
                } else {
                    tvTemp.setAlpha(1f);
                    tvHumidity.setAlpha(1f);
                    tvHumidityValue.setAlpha(1f);
                    tvTempValue.setAlpha(1f);
                    fabTimeConfig.hide();
                    fabTvTimeConfig.setVisibility(View.GONE);
                    fabTempData.hide();
                    fabTvTempData.setVisibility(View.GONE);
                    fabDeleteTempDevice.hide();
                    fabTvDeleteTempDevice.setVisibility(View.GONE);
                    isFabExtended = false;
                }
            }
        });
        fabTempData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), HistoricalTempData.class);
                startActivity(i);

                fabTimeConfig.hide();
                fabTvTimeConfig.setVisibility(View.GONE);
                fabTempData.hide();
                fabTvTempData.setVisibility(View.GONE);
                fabDeleteTempDevice.hide();
                fabTvDeleteTempDevice.setVisibility(View.GONE);
                tvTemp.setAlpha(1f);
                tvHumidity.setAlpha(1f);
                tvHumidityValue.setAlpha(1f);
                tvTempValue.setAlpha(1f);
            }
        });
        fabDeleteTempDevice.setOnClickListener(new View.OnClickListener() {
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
                fabTvTimeConfig.setVisibility(View.GONE);
                fabTempData.hide();
                fabTvTempData.setVisibility(View.GONE);
                fabDeleteTempDevice.hide();
                fabTvDeleteTempDevice.setVisibility(View.GONE);
                tvTemp.setAlpha(1f);
                tvHumidity.setAlpha(1f);
                tvHumidityValue.setAlpha(1f);
                tvTempValue.setAlpha(1f);
            }
        });
        fabTimeConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View v = getActivity().getLayoutInflater().inflate(R.layout.time_config_temp_dialog, null);

                RadioButton rbOneMin, rbFiveMin, rbTenMin;
                rbOneMin = v.findViewById(R.id.rbOneMin);
                rbFiveMin = v.findViewById(R.id.rbFiveMin);
                rbTenMin = v.findViewById(R.id.rbTenMin);

                builder.setView(v)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(rbOneMin.isChecked()){
                                    rbOneMin.setChecked(true);
                                    try {
                                        mqttService.publish(PUB_CONTROL_TOPIC, TIME_INTERVAL, "1");
                                    } catch (JSONException | MqttException e) {
                                        e.printStackTrace();
                                    }
                                } else if (rbFiveMin.isChecked()){
                                    rbFiveMin.setChecked(true);
                                    try {
                                        mqttService.publish(PUB_CONTROL_TOPIC, TIME_INTERVAL, "5");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (MqttException e) {
                                        e.printStackTrace();
                                    }
                                } else if (rbTenMin.isChecked()){
                                    rbOneMin.setChecked(true);
                                    try {
                                        mqttService.publish(PUB_CONTROL_TOPIC, TIME_INTERVAL, "10");
                                    } catch (JSONException | MqttException e) {
                                        e.printStackTrace();
                                    }
                                }
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

                fabTimeConfig.hide();
                fabTvTimeConfig.setVisibility(View.GONE);
                fabTempData.hide();
                fabTvTempData.setVisibility(View.GONE);
                fabDeleteTempDevice.hide();
                fabTvDeleteTempDevice.setVisibility(View.GONE);
                tvTemp.setAlpha(1f);
                tvHumidity.setAlpha(1f);
                tvHumidityValue.setAlpha(1f);
                tvTempValue.setAlpha(1f);
            }
        });

        if(getArguments() != null){
            tempValue = getArguments().getInt(ARG_TEMP);
            humText = getArguments().getString(ARG_HUM);
            tempText = String.valueOf(tempValue);
            tvTempValue.setText(tempText);
            tvHumidityValue.setText(humText);
            name = getArguments().getString("deviceName");
            updatedName = getArguments().getString("updatedName");
            deviceName.setText(name);
            mqttService = getArguments().getParcelable("mqtt");
            if(tempText == null){
                tvTempValue.setText("----");
            }
            if(humText == null){
                tvHumidityValue.setText("----");
            }
       }

        return v;
    }

}
