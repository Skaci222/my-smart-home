package com.myproject.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.myproject.R;
import com.myproject.room.Device;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class SecurityRVAdapter extends RecyclerView.Adapter<SecurityRVAdapter.MViewHolder> {

    private List<Device> devices = new ArrayList<>();

    private SecurityRVAdapter.OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(int position)throws JSONException, InterruptedException, MqttException;
        void onItemLongClick(Device device);
    }

    public void setOnItemClickListener(SecurityRVAdapter.OnItemClickListener listener){
        this.listener = listener;
    }


    public static class MViewHolder extends RecyclerView.ViewHolder {

        private TextView tvDeviceName;
        private SwitchCompat armedSwitch;

        public MViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            tvDeviceName = itemView.findViewById(R.id.tvSecurityDeviceName);
            armedSwitch = itemView.findViewById(R.id.armedSelect);

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.security_item, parent, false);
        return new MViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MViewHolder holder, int position) {
        Device currentDevice = devices.get(position);
        String deviceName = currentDevice.getName();
        holder.tvDeviceName.setText(deviceName);
        if(holder.armedSwitch.isChecked()){
            Toast.makeText(holder.armedSwitch.getContext(), "Switch is checked on", Toast.LENGTH_SHORT).show();
        }
        

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

