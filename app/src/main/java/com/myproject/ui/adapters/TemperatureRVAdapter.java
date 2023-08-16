package com.myproject.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myproject.R;
import com.myproject.room.Device;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class TemperatureRVAdapter extends RecyclerView.Adapter<TemperatureRVAdapter.MViewHolder> {

    private List<Device> devices = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(int position)throws JSONException, InterruptedException, MqttException;
        void onItemLongClick(Device device);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
    public static class MViewHolder extends RecyclerView.ViewHolder{

        TextView tvTempDeviceName;
        ImageView thermostatImage;

        public MViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            tvTempDeviceName = itemView.findViewById(R.id.tvTempDeviceName);
            thermostatImage = itemView.findViewById(R.id.thermostat_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            try {
                                listener.onItemClick(position);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public MViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.items, parent, false);
        return new MViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MViewHolder holder, int position) {
        Device device = devices.get(position);
        String deviceName = device.getName();
        holder.tvTempDeviceName.setText(deviceName);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void setDevices(List<Device> devices){
        this.devices = devices;
        notifyDataSetChanged();
    }
    public Device getDeviceAt(int position) {
        return devices.get(position);
    }
}
