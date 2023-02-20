package com.myproject.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.myproject.R;
import com.myproject.room.Device;
import com.myproject.room.DeviceViewModel;
import com.myproject.ui.adapters.RecyclerViewAdapter;

import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Device> mDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new GridLayoutManager(this, 3);
        recyclerViewAdapter = new RecyclerViewAdapter();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);


        DeviceViewModel deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
        deviceViewModel.getAllDevices().observe(this, new Observer<List<Device>>() {
            @Override
            public void onChanged(List<Device> devices) {
                recyclerViewAdapter.setDevices(devices);
            }
        });

        deviceViewModel.insert(new Device("heater", "heater"));
        deviceViewModel.insert(new Device("motion", "motion"));
        deviceViewModel.insert(new Device("temperature", "temperature"));


    }
}