package com.myproject.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.myproject.R;
import com.myproject.logic.MqttService;
import com.myproject.receiverlistener.DeleteDeviceListener;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;


public class TemperatureFragment extends Fragment  {

    private TextView tvHumidityValue, deviceName;
    public TextView  tvTempValue;
    public static final String ARG_TEMP = "Temperature";
    public static final String ARG_HUM = "Humidity";
    private String tempText;
    private String humText;
    private String name;
    private int id;
    private String updatedName;
    private ExtendedFloatingActionButton fabMenuTempFrag;
    private FloatingActionButton fabTempData, fabDeleteTempDevice;
    private TextView fabTvTempData, fabTvDeleteTempDevice, tvHumidity;
    private ImageView ivTemp;
    private boolean isFabExtended = false;
    private MqttService mqttService;
    public DeleteDeviceListener deleteDeviceListener;
    public interface FabMenuListener{
        void fabExtended(boolean b);
    }
    private FabMenuListener listener;

    /*public static TemperatureFragment newInstance(String temp, String humidity, String name){
        Bundle args = new Bundle();
        TemperatureFragment fragment = new TemperatureFragment();
        args.putString(ARG_TEMP, temp);
        args.putString(ARG_HUM, humidity);
        fragment.setArguments(args);
        return fragment;
    }*/
    public static TemperatureFragment newInstance(String temp, String name){
        Bundle args = new Bundle();
        TemperatureFragment fragment = new TemperatureFragment();
        args.putString(ARG_TEMP, temp);
        args.putString("deviceName", name);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.temp_frag, container, false);

        tvTempValue = v.findViewById(R.id.tvTempValue);
        tvHumidityValue = v.findViewById(R.id.tvHumidityValue);
        ivTemp = v.findViewById(R.id.ivTemp);
        tvHumidity = v.findViewById(R.id.tvHumidity);
        deviceName = v.findViewById(R.id.textViewTitle);
        fabMenuTempFrag = v.findViewById(R.id.fabMenuTempFrag);
        fabTempData = v.findViewById(R.id.fabTempData);
        fabDeleteTempDevice = v.findViewById(R.id.fabDeleteTempDevice);
        fabTvTempData = v.findViewById(R.id.fabTvTempData);
        fabTvDeleteTempDevice = v.findViewById(R.id.fabTvDeleteTempDevice);
        fabTempData.hide();
        fabDeleteTempDevice.hide();
        fabTvTempData.setVisibility(View.GONE);
        fabTvDeleteTempDevice.setVisibility(View.GONE);

        if(getArguments() != null){
            tempText = getArguments().getString(ARG_TEMP);
            humText = getArguments().getString(ARG_HUM);
            tvTempValue.setText(tempText + "\u2103");
            tvHumidityValue.setText(humText);
            name = getArguments().getString("deviceName");
            updatedName = getArguments().getString("updatedName");
            deviceName.setText(name);
            mqttService = getArguments().getParcelable("mqtt");
            if(tempText == null){
                tvTempValue.setText("----");
            }

        }

        fabMenuTempFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFabExtended){
                    ivTemp.setAlpha(0.1f);
                    tvHumidity.setAlpha(0.1f);
                    tvHumidityValue.setAlpha(0.1f);
                    tvTempValue.setAlpha(0.1f);
                    fabTempData.show();
                    fabTvTempData.setVisibility(View.VISIBLE);
                    fabDeleteTempDevice.show();
                    fabTvDeleteTempDevice.setVisibility(View.VISIBLE);
                    isFabExtended = true;
                    listener = (FabMenuListener) getActivity();
                    listener.fabExtended(true);
                } else {
                    ivTemp.setAlpha(1f);
                    tvHumidity.setAlpha(1f);
                    tvHumidityValue.setAlpha(1f);
                    tvTempValue.setAlpha(1f);
                    fabTempData.hide();
                    fabTvTempData.setVisibility(View.GONE);
                    fabDeleteTempDevice.hide();
                    fabTvDeleteTempDevice.setVisibility(View.GONE);
                    isFabExtended = false;
                    listener = (FabMenuListener) getActivity();
                    listener.fabExtended(false);
                }
            }
        });
        fabTempData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), HistoricalTempData.class);
                startActivity(i);

                fabTempData.hide();
                fabTvTempData.setVisibility(View.GONE);
                fabDeleteTempDevice.hide();
                fabTvDeleteTempDevice.setVisibility(View.GONE);
                ivTemp.setAlpha(1f);
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
                fabTempData.hide();
                fabTvTempData.setVisibility(View.GONE);
                fabDeleteTempDevice.hide();
                fabTvDeleteTempDevice.setVisibility(View.GONE);
                ivTemp.setAlpha(1f);
                tvHumidity.setAlpha(1f);
                tvHumidityValue.setAlpha(1f);
                tvTempValue.setAlpha(1f);
            }
        });

        return v;
    }



}
