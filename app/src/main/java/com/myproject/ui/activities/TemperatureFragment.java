package com.myproject.ui.activities;

import android.app.FragmentContainer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.myproject.R;
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

import java.util.ArrayList;
import java.util.List;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;

public class TemperatureFragment extends Fragment  {

    private TextView tvTempValue, tvHumidityValue, allValues;

    private RecyclerViewAdapter.MyViewHolder adapter;
    public static final String ARG_TEMP = "Temperature";
    public static final String ARG_HUM = "Humidity";
    private String tempText;
    private String humText;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int itemSelected;

    public static TemperatureFragment newInstance(String temp, String humidity){
        Bundle args = new Bundle();
        TemperatureFragment fragment = new TemperatureFragment();
        args.putString(ARG_TEMP, temp);
        args.putString(ARG_HUM, humidity);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.temp_frag, container, false);

        tvTempValue = v.findViewById(R.id.tvTempValue);
        tvHumidityValue = v.findViewById(R.id.tvHumidityValue);

        //initial temp reading
        try {
            StartScreen.getInstance().initRequest();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        if(getArguments() != null){
            tempText = getArguments().getString(ARG_TEMP);
            humText = getArguments().getString(ARG_HUM);
            tvTempValue.setText(tempText);
            tvHumidityValue.setText(humText);
       }
        setHasOptionsMenu(true);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_temp_frag, menu);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        itemSelected = sharedPreferences.getInt("timePrefs", itemSelected);
        if(itemSelected == 1){
            menu.findItem(R.id.updateOneMin).setChecked(true);
        } else if(itemSelected == 5){
            menu.findItem(R.id.updateFiveMin).setChecked(true);
        } else if(itemSelected == 10){
            menu.findItem(R.id.updateTenMin).setChecked(true
            );
        }
        Log.i("TAG", "item selected is " + itemSelected);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.actionAddDevice).setVisible(false);
        menu.findItem(R.id.disconnect).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = sharedPreferences.edit();

        switch (id){
            case R.id.updateOneMin:
                try {
                    item.setChecked(true);
                    StartScreen.getInstance().timeConfig(1);
                    itemSelected = 1;
                    editor.putInt("timePrefs", itemSelected);
                    editor.commit();
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.updateFiveMin:
                try {
                    item.setChecked(true);
                    StartScreen.getInstance().timeConfig(5);
                    itemSelected = 5;
                    editor.putInt("timePrefs", itemSelected);
                    editor.commit();
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.updateTenMin:
                try {
                    item.setChecked(true);
                    StartScreen.getInstance().timeConfig(10);
                    itemSelected = 10;
                    editor.putInt("timePrefs", itemSelected);
                    editor.commit();
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
