package com.myproject.receiverlistener;

import com.myproject.ui.adapters.TemperatureRVAdapter;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

public interface DeviceClickListener {
    void onDeviceClicked(String deviceType, String deviceId) throws MqttException, JSONException;
}
