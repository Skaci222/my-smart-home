package com.myproject.ui.activities;

import static com.myproject.ui.activities.MqttClient.TEMP_SUB;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.myproject.R;
import com.myproject.room.Message;
import com.myproject.room.MessageViewModel;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class HistoricalTempData extends AppCompatActivity {

    private Button btnDatePicker;
    private MessageViewModel messageViewModel;
    private double value, time;
    private GraphView graph;
    private LineGraphSeries<DataPoint> series;
    private DateFormat dateFormat = new SimpleDateFormat("HH:MM");
    private Calendar c, cal;
    private Date date = new Date();
    long dateInMillis, l;
    private Date d1, d2, d3, d4, d5;

    private DatePickerDialog.OnDateSetListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_data);

        graph = findViewById(R.id.tempGraph);
        //series = new LineGraphSeries<>(getDataPoint());
        /*graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
           @Override
            public String formatLabel(double value, boolean isValueX) {
                if(isValueX){
                    return simpleDateFormat.format(new Date((long) value));
                } else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });*/

        //graph.getGridLabelRenderer().setNumHorizontalLabels(4);

        c = Calendar.getInstance();

        btnDatePicker = findViewById(R.id.btnDatePicker);
        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(HistoricalTempData.this, listener, c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                c.set(Calendar.YEAR, i);
                c.set(Calendar.MONTH, i1);
                c.set(Calendar.DAY_OF_MONTH, i2);
                c.set(i, i1, i2);
                dateInMillis = c.getTimeInMillis();
                String dateString = DateFormat.getDateInstance().format(c.getTime());
                Toast.makeText(HistoricalTempData.this, "Date is set: " + dateString, Toast.LENGTH_SHORT).show();
                Log.i("HISTORICALDATA", "Date is set: " + dateString);
            }
        };

        /*SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        String key = sharedPreferences.getString("key","");
        String value = sharedPreferences.getString("value", "");
        Log.i("HISTORICALTEMPDATA", "received from sharedPrefs: " + key + ", " +value);*/


        cal = Calendar.getInstance();
        d1 = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, 2);
        d2 = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, 2);
        d3 = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, 2);
        d4 = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, 2);
        d5 = cal.getTime();


        // set manual x bounds to have nice steps
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(d1.getTime());
        graph.getViewport().setMaxX(d5.getTime());
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(30);
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.getViewport().scrollToEnd();
        //graph.getViewport().setScalableY(true);
        //graph.getViewport().setScrollableY(true);

      /*  series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(d1, 5),
                new DataPoint(d2, 10),
                new DataPoint(d3, 5),
                new DataPoint(d4, 12),
                new DataPoint(d5, 5),

        });*/
        series = new LineGraphSeries<>();
        graph.addSeries(series);

        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    //return dateFormat.format(value);
                    return super.formatLabel(value, isValueX);
                }
                return super.formatLabel(value, isValueX);
            }
        });
        // graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(graph.getContext(), new SimpleDateFormat("d", Locale.CANADA)));
        graph.getGridLabelRenderer().setNumHorizontalLabels(4);



        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        /*messageViewModel.getAllMessages().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                //tvTempData.append(m.getValue() + "\n");
                for (Message m : messages) {
                    value = Double.parseDouble(m.getValue());
                    Log.i("TAG", "message value is: " + value);
                    //series.appendData(new DataPoint(d3, value),true, 10);
                }
            }
        });*/

        if(dateInMillis == 0){
            c.get(Calendar.YEAR);
            c.get(Calendar.MONTH);
            c.get(Calendar.DAY_OF_MONTH);
            dateInMillis = c.getTimeInMillis();
        }
       messageViewModel.getMessagesFromDate(dateInMillis, TEMP_SUB).observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                    Message m = messages.get(messages.size()-1);
                    Date d = m.getDate();
                    l = d.getTime();
                    String day = String.valueOf(m.getDate());
                    value = Double.parseDouble(m.getValue());
                    //Log.i("HISTORICALTEMP", "# messages graph activity: " + messages.size());
                   // Log.i("HISTORICALTEMP", "messages from date in graph activity: " + day + ": " + value);
                        series.appendData(new DataPoint(d, value), true, 12);
                        Log.i("HISTORICALDATA", "# messages in message List: " + messages.size());

                }

        });
    }

    private DataPoint[] getDataPoint() {
        DataPoint[] dp = new DataPoint[]{
                new DataPoint(9, 4),
                new DataPoint(10, 22),
                new DataPoint(11, 35),
                new DataPoint(12, 30),
                new DataPoint(13, 10),
        };
        return dp;
    }

    private DataPoint[] getDataPointFromDate(double x, double y) {
        DataPoint[] dp = new DataPoint[]{
                new DataPoint(x, y)
        };
        return dp;
    }


}