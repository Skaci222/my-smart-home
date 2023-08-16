package com.myproject.ui.activities;

import android.os.Bundle;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myproject.R;
import com.myproject.logic.MqttService;
import com.myproject.receiverlistener.DeviceClickListener;
import com.myproject.room.Device;
import com.myproject.room.DeviceViewModel;
import com.myproject.ui.adapters.SecurityRVAdapter;
import com.myproject.ui.adapters.TemperatureRVAdapter;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TemperatureListFrag extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private TemperatureRVAdapter adapter;

    private DeviceViewModel deviceViewModel;
    private Device device;
    private String deviceName;
    private int deviceId;
    private String deviceMac;
    private MqttService mqttService;
    public ScheduledExecutorService tempExecutorService1, tempExecutiveService2;
    private DeviceClickListener deviceClickListener;

    private TemperatureFragment temperatureFragment;

   /* public static TemperatureListFrag newInstance(String name, String mac){
        Bundle bundle = new Bundle();
        TemperatureListFrag temperatureListFrag = new TemperatureListFrag();
        bundle.putString("name", name);
        bundle.putString("mac", mac);
        temperatureListFrag.setArguments(bundle);
        return temperatureListFrag;
    }*/



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.temperature_fragment_list, container, false);

        recyclerView = v.findViewById(R.id.recycler_view_temperature);
        manager = new LinearLayoutManager(this.getContext());
        adapter = new TemperatureRVAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
        temperatureFragment = new TemperatureFragment();
        try {
            mqttService = new MqttService(getContext());
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

        deviceViewModel = new ViewModelProvider(getActivity()).get(DeviceViewModel.class);
        deviceViewModel.getTemperatureDevices().observe(getActivity(), new Observer<List<Device>>() {
            @Override
            public void onChanged(List<Device> devices) {
                adapter.setDevices(devices);
            }
        });

        adapter.setOnItemClickListener(new TemperatureRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) throws JSONException, InterruptedException, MqttException {
                device = adapter.getDeviceAt(position);
                deviceId = device.getId();
                deviceName = device.getName();
                Bundle b = new Bundle();
                temperatureFragment = new TemperatureFragment();
                b.putString("deviceName", deviceName);
                b.putInt("id", deviceId);
                temperatureFragment.setArguments(b);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_layout, temperatureFragment, "temperature_fragment")
                        .addToBackStack("temperature_fragment").commit();
                mqttService.subscribeToTemp();
               // mqttService.publishTempRequest(device.getMac()); //figure out which method to remove
                deviceClickListener = (DeviceClickListener) getActivity();
                deviceClickListener.onDeviceClicked(device.getMac());
               /* Runnable tempRequest = new Runnable() {
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
                tempExecutorService1.scheduleAtFixedRate(tempRequest, 0, 5, TimeUnit.SECONDS);*/


            }

            @Override
            public void onItemLongClick(Device device) {

            }
        });



        return v;
    }

}
