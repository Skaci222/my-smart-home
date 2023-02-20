package com.myproject.ui.activities;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {

    TimePickerDialog.OnTimeSetListener listener;

    public static TimePickerFragment newInstance(){
        return new TimePickerFragment();
    }

    public TimePickerFragment(){

    }
    public TimePickerFragment(TimePickerDialog.OnTimeSetListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), listener, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void setListener(TimePickerDialog.OnTimeSetListener listener){
        this.listener = listener;
    }
}
