package com.myproject.receiverlistener;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

public interface ArmDisarmListener {
    void armDevice() throws MqttException, JSONException;
    void disarmDevice() throws MqttException, JSONException;
}
