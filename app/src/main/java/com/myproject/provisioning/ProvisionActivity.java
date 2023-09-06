// Copyright 2020 Espressif Systems (Shanghai) PTE LTD
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.myproject.provisioning;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.lifecycle.ViewModelProvider;

import com.myproject.AppConstants;
import com.myproject.logic.MqttService;
import com.myproject.provisioning.listeners.ResponseListener;
import com.myproject.R;
import com.myproject.provisioning.listeners.ProvisionListener;
import com.myproject.room.Device;
import com.myproject.room.DeviceViewModel;
import com.myproject.ui.activities.StartScreen;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ProvisionActivity extends AppCompatActivity { // this is the activity where JSON data is sent to ESP via sentToEndPoint method

    private static final String TAG = ProvisionActivity.class.getSimpleName();
    public static final String CUSTOM_ENDPOINT = "custom-data";

    private TextView tvTitle, tvBack, tvCancel;
    private ImageView tick1, tick2, tick3;
    private ContentLoadingProgressBar progress1, progress2, progress3;
    private TextView tvErrAtStep1, tvErrAtStep2, tvErrAtStep3, tvProvError;

    private CardView btnOk;
    private TextView txtOkBtn;

    private String ssidValue, passphraseValue = "";
    private ESPProvisionManager provisionManager;
    private boolean isProvisioningCompleted = false;

    private String name;
    private String deviceType;
    private android.app.AlertDialog dialog;
    private String deviceId;
    private DeviceViewModel deviceViewModel;
    private Bundle bundle;

    private MqttService mqttService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provision);

      /*  try {
            mqttService = new MqttService(getApplicationContext());
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }*/
        Intent intent = getIntent();
        ssidValue = intent.getStringExtra(AppConstants.KEY_WIFI_SSID);
        passphraseValue = intent.getStringExtra(AppConstants.KEY_WIFI_PASSWORD);
        provisionManager = ESPProvisionManager.getInstance(getApplicationContext());
        initViews();
        EventBus.getDefault().register(this);

        Log.d(TAG, "Selected AP -" + ssidValue);

        bundle = getIntent().getExtras();
        name = bundle.getString("name");
        //deviceType = bundle.getString("type");
        //deviceId = bundle.getString("ssid");

        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);

        //calling dataExchange in onCreate, check if it works
        dataExchange();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        showLoading();
        doProvisioning();
    }

    @Override
    public void onBackPressed() {
        provisionManager.getEspDevice().disconnectDevice();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DeviceConnectionEvent event) {

        Log.d(TAG, "On Device Connection Event RECEIVED : " + event.getEventType());

        switch (event.getEventType()) {

            case ESPConstants.EVENT_DEVICE_DISCONNECTED:
                if (!isFinishing() && !isProvisioningCompleted) {
                    showAlertForDeviceDisconnected();
                }
                break;
        }
    }

    /**
     * Pressing OK button when provisioning is completed
     */
    private View.OnClickListener okBtnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Device device = new Device(name, deviceType, deviceId);
            deviceViewModel.insert(device);
            //try/catch block might not work, check status of mqttService object
            /*try {
                mqttService.subscribeToTopic(deviceType, deviceId);
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }*/
            Intent i = new Intent(getApplicationContext(), StartScreen.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
    };

    private void initViews() {

        tvTitle = findViewById(R.id.main_toolbar_title);
        tvBack = findViewById(R.id.btn_back);
        tvCancel = findViewById(R.id.btn_cancel);

        tick1 = findViewById(R.id.iv_tick_1);
        tick2 = findViewById(R.id.iv_tick_2);
        tick3 = findViewById(R.id.iv_tick_3);

        progress1 = findViewById(R.id.prov_progress_1);
        progress2 = findViewById(R.id.prov_progress_2);
        progress3 = findViewById(R.id.prov_progress_3);

        tvErrAtStep1 = findViewById(R.id.tv_prov_error_1);
        tvErrAtStep2 = findViewById(R.id.tv_prov_error_2);
        tvErrAtStep3 = findViewById(R.id.tv_prov_error_3);
        tvProvError = findViewById(R.id.tv_prov_error);

        tvTitle.setText(R.string.title_activity_provisioning);
        tvBack.setVisibility(View.GONE);
        tvCancel.setVisibility(View.GONE);

        btnOk = findViewById(R.id.btn_ok);
        txtOkBtn = findViewById(R.id.text_btn);
        btnOk.findViewById(R.id.iv_arrow).setVisibility(View.GONE);

        txtOkBtn.setText(R.string.btn_ok);
        btnOk.setOnClickListener(okBtnClickListener);
    }

    public void dataExchange() {

        ESPDevice device = provisionManager.getEspDevice();

        JSONObject obj = new JSONObject();
        try {
            obj.put("hostname", "e7ea538cb0564a42b068269a96574848.s1.eu.hivemq.cloud");
            obj.put("username", "pladi");
            obj.put("password", "password");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        byte[] b = obj.toString().getBytes();
        device.sendDataToCustomEndPoint(CUSTOM_ENDPOINT, b, new ResponseListener() {
            @Override
            public void onSuccess(byte[] returnData) {
                /**
                 * obtain Mac from this callback and save it in a variable, send to StartScreen
                 */
                deviceId = new String(returnData, StandardCharsets.UTF_8);
                if(deviceId.contains("SECURITY")){
                    deviceType = "security_device";
                } else{
                    deviceType = "other_type"; //add other device types
                }
                //bundle.putString("mac", mac);
                Intent intent = new Intent(getApplicationContext(), StartScreen.class);
                intent.putExtras(bundle);
                Log.d(TAG, "return data: " + deviceId);
                Log.i(TAG, "send data to custom endpoint is successful");
            }

            @Override
            public void onFailure(Exception e) {
                Log.d("SendCustomMsg", "Failed to Send");
            }
        });

    }


    private void doProvisioning() {

        tick1.setVisibility(View.GONE);
        progress1.setVisibility(View.VISIBLE);

        provisionManager.getEspDevice().provision(ssidValue, passphraseValue, new ProvisionListener() {

            @Override
            public void createSessionFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick1.setImageResource(R.drawable.ic_error);
                        tick1.setVisibility(View.VISIBLE);
                        progress1.setVisibility(View.GONE);
                        tvErrAtStep1.setVisibility(View.VISIBLE);
                        tvErrAtStep1.setText(R.string.error_session_creation);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();;
                    }
                });
            }

            @Override
            public void wifiConfigSent() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick1.setImageResource(R.drawable.ic_baseline_check_24);
                        tick1.setVisibility(View.VISIBLE);
                        progress1.setVisibility(View.GONE);
                        tick2.setVisibility(View.GONE);
                        progress2.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void wifiConfigFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick1.setImageResource(R.drawable.ic_error);
                        tick1.setVisibility(View.VISIBLE);
                        progress1.setVisibility(View.GONE);
                        tvErrAtStep1.setVisibility(View.VISIBLE);
                        tvErrAtStep1.setText(R.string.error_prov_step_1);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void wifiConfigApplied() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick2.setImageResource(R.drawable.ic_baseline_check_24);
                        tick2.setVisibility(View.VISIBLE);
                        progress2.setVisibility(View.GONE);
                        tick3.setVisibility(View.GONE);
                        progress3.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void wifiConfigApplyFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick2.setImageResource(R.drawable.ic_error);
                        tick2.setVisibility(View.VISIBLE);
                        progress2.setVisibility(View.GONE);
                        tvErrAtStep2.setVisibility(View.VISIBLE);
                        tvErrAtStep2.setText(R.string.error_prov_step_2);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void provisioningFailedFromDevice(final ESPConstants.ProvisionFailureReason failureReason) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        switch (failureReason) {
                            case AUTH_FAILED:
                                tvErrAtStep3.setText(R.string.error_authentication_failed);
                                break;
                            case NETWORK_NOT_FOUND:
                                tvErrAtStep3.setText(R.string.error_network_not_found);
                                break;
                            case DEVICE_DISCONNECTED:
                            case UNKNOWN:
                                tvErrAtStep3.setText(R.string.error_prov_step_3);
                                break;
                        }
                        tick3.setImageResource(R.drawable.ic_error);
                        tick3.setVisibility(View.VISIBLE);
                        progress3.setVisibility(View.GONE);
                        tvErrAtStep3.setVisibility(View.VISIBLE);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void deviceProvisioningSuccess() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        isProvisioningCompleted = true;
                        tick3.setImageResource(R.drawable.ic_baseline_check_24);
                        tick3.setVisibility(View.VISIBLE);
                        progress3.setVisibility(View.GONE);
                        hideLoading();

                    }
                });
            }

            @Override
            public void onProvisioningFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick3.setImageResource(R.drawable.ic_error);
                        tick3.setVisibility(View.VISIBLE);
                        progress3.setVisibility(View.GONE);
                        tvErrAtStep3.setVisibility(View.VISIBLE);
                        tvErrAtStep3.setText(R.string.error_prov_step_3);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();
                    }
                });
            }
        });
    }

    private void showLoading() {

        btnOk.setEnabled(false);
        btnOk.setAlpha(0.5f);
    }

    public void hideLoading() {

        btnOk.setEnabled(true);
        btnOk.setAlpha(1f);
    }

    private void showAlertForDeviceDisconnected() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.error_title);
        builder.setMessage(R.string.dialog_msg_ble_device_disconnection);

        // Set up the buttons
        builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.show();
    }

}

