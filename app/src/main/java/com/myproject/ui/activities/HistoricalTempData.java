package com.myproject.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.myproject.R;
import com.myproject.room.Message;
import com.myproject.room.MessageViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class HistoricalTempData extends AppCompatActivity {

    private TextView tvTempData;
    private Button btnDeleteAllMsg;
    private MessageViewModel messageViewModel;
    String tempValue;
    String humValue;
    String messageValue;
    private static HistoricalTempData ins;
    private List<Message> messages = new ArrayList<>();

    private LineGraphSeries<DataPoint> series;
    GraphView graph;
    private Date date;
    private Calendar c;
    private int lastX = 0;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("H:M");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_data);
        ins = this;
       /* c = Calendar.getInstance();
        Date d1 = c.getTime();
        c.add(Calendar.DATE, 1);
        Date d2 = c.getTime();
        c.add(Calendar.DATE,1);
        Date d3 = c.getTime();
        c.add(Calendar.DATE,1);
        Date d4 = c.getTime();*/

        graph = findViewById(R.id.tempGraph);

        series = new LineGraphSeries<>(getDataPoint());

        graph.getGridLabelRenderer().setNumHorizontalLabels(4);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScrollableY(true);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);
        graph.addSeries(series);
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
            @Override
            public String formatLabel(double value, boolean isValueX) {

                if(isValueX){
                    return simpleDateFormat.format(new Date((long) value));
                }else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        btnDeleteAllMsg = findViewById(R.id.btnDeleteAllMsg);
        btnDeleteAllMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageViewModel.deleteAllMessages();
                //tvTempData.setText("");
            }
        });

        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        messageViewModel.getAllMessages().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                //tvTempData.append(m.getValue() + "\n");
               for (Message m : messages) {
                   double value = Double.parseDouble(m.getValue());
                   Log.i("TAG", "message value is: " + value);
                   //series.appendData(new DataPoint(date.getTime(), value),true, 10);
               }


            }
        });

    }
    public DataPoint[] getDataPoint(){
        DataPoint[] dp = new DataPoint[]{
                new DataPoint(new Date().getTime(),1),
                new DataPoint(new Date().getTime(),22),
                new DataPoint(new Date().getTime(),3),
                new DataPoint(new Date().getTime(),14),
                new DataPoint(new Date().getTime(),9),
                new DataPoint(new Date().getTime(),16),
                new DataPoint(new Date().getTime(),8),
                new DataPoint(new Date().getTime(),2),
                new DataPoint(new Date().getTime(),11),
                new DataPoint(new Date().getTime(),8),
                new DataPoint(new Date().getTime(),17),
        };
        return dp;
    }
}