package com.myproject.room;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class DeviceViewModel extends AndroidViewModel {

    private DeviceRepo deviceRepo;
    private LiveData<List<Device>> allDevices;
    private LiveData<List<Device>> securityDevices;

    private LiveData<List<Device>> temperatureDevices;



    public DeviceViewModel(@NonNull Application application) {
        super(application);
        deviceRepo = new DeviceRepo(application);
        allDevices = deviceRepo.getAllDevices();
        securityDevices = deviceRepo.getSecurityDevices();
        temperatureDevices = deviceRepo.getTemperatureDevices();
    }

    public void insert(Device device){
        deviceRepo.insert(device);
    }

    public void update(Device device){
        deviceRepo.update(device);
    }

    public void delete(Device device){
        deviceRepo.delete(device);
    }

    public void deleteAllDevices(){
        deviceRepo.deleteAllDevices();
    }

    public LiveData<List<Device>> getAllDevices(){
        return allDevices;
    }

    public LiveData<List<Device>> getTemperatureDevices(){
        return temperatureDevices;
    }

    public LiveData<List<Device>> getSecurityDevices(){
        return securityDevices;
    }
}
