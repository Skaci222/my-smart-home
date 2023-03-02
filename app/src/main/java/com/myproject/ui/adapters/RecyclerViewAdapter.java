package com.myproject.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myproject.R;
import com.myproject.room.Device;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private List<Device> mDeviceItems = new ArrayList<>();
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onItemLongClick(Device device);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        return new MyViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Device currentItem = mDeviceItems.get(position);
        String deviceName = currentItem.getName();
        String deviceType = currentItem.getType();

       holder.tvCardName.setText(deviceName);
      /*  switch(deviceType){
            case "temperature":
                holder.ivType.setImageResource(R.drawable.ic_thermostat);
                break;
            case "heater":
                holder.ivType.setImageResource(R.drawable.ic_baseline_sync_24);
                break;
            case "motion":
                holder.ivType.setImageResource(R.drawable.ic_security);
                break;

        }*/

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mListener.onItemLongClick(currentItem);
                Toast.makeText(view.getContext(), "ItemLong clicked", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDeviceItems.size();
    }

    public void setDevices(List<Device> devices) {
        mDeviceItems = devices;
        notifyDataSetChanged();
    }

    public Device getDeviceAt(int position) {
        return mDeviceItems.get(position);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView tvCardName;
        public ImageView ivType;

        public MyViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            tvCardName = itemView.findViewById(R.id.tvCardName);
            ivType = itemView.findViewById(R.id.ivType);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }

            });

        }
    }


}
