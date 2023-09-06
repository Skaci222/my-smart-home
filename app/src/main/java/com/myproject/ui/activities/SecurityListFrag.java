package com.myproject.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myproject.R;
import com.myproject.receiverlistener.DeviceClickListener;
import com.myproject.room.Device;
import com.myproject.room.DeviceViewModel;
import com.myproject.ui.adapters.SecurityRVAdapter;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class SecurityListFrag extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private SecurityRVAdapter adapter;

    private DeviceViewModel deviceViewModel;
    private SecurityFragment securityFragment;

    private Device device;
    private String deviceName;

    private String deviceUniqueId;

    private String deviceType;
    private int deviceId;

    private DeviceClickListener deviceClickListener;

    public static SecurityListFrag newInstance(String name, String mac){
        Bundle bundle = new Bundle();
        SecurityListFrag securityFragment = new SecurityListFrag();
        bundle.putString("name", name);
        bundle.putString("mac", mac);
        securityFragment.setArguments(bundle);
        return securityFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.security_frag_list, container, false);

        recyclerView = v.findViewById(R.id.recycler_view_security);
        manager = new LinearLayoutManager(this.getContext());
        adapter = new SecurityRVAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
        securityFragment = new SecurityFragment();

        deviceViewModel = new ViewModelProvider(getActivity()).get(DeviceViewModel.class);
        deviceViewModel.getSecurityDevices().observe(getActivity(), new Observer<List<Device>>() {
            @Override
            public void onChanged(List<Device> devices) {
                adapter.setDevices(devices);
            }
        });

        adapter.setOnItemClickListener(new SecurityRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) throws JSONException, InterruptedException, MqttException {
                device = adapter.getDeviceAt(position);
                deviceName = device.getName();
                deviceId = device.getId();
                deviceUniqueId = device.getMac();
                deviceType = device.getType();
                Bundle b = new Bundle();
                b.putString("deviceName", deviceName);
                b.putInt("id", deviceId);
                securityFragment.setArguments(b);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_layout, securityFragment, "security_fragment")
                        .addToBackStack("security_fragmant")
                        .commit();

                deviceClickListener = (DeviceClickListener) getActivity();
                deviceClickListener.onDeviceClicked(deviceType, deviceUniqueId);
            }

            @Override
            public void onItemLongClick(Device device) {

            }
        });

        return v;
    }
}
