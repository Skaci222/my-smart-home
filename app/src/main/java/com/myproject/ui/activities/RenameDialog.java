package com.myproject.ui.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.myproject.R;
import com.myproject.room.Device;
import com.myproject.room.DeviceViewModel;

public class RenameDialog extends DialogFragment {
    private EditText editText;
    private DeviceViewModel deviceViewModel;

    public interface OnInputListener{
        void sendInput(String input, String type, int id);
    }
    public OnInputListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.layout_dialog, null);
        builder.setView(v)
                .setTitle("Rename Your Device")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //saveDevice();
                        mListener = (OnInputListener) getActivity();
                        String name = editText.getText().toString();
                        String type = getArguments().getString("type");
                        int id = getArguments().getInt("id");
                        mListener.sendInput(name, type, id);
                        dismiss();
                        Toast.makeText(getActivity(), "OK", Toast.LENGTH_SHORT).show();
                    }
                });

        editText = v.findViewById(R.id.etRenameDevice);

        return builder.create();
    }

}
