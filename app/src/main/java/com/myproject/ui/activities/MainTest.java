package com.myproject.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.myproject.R;
import com.myproject.model.MqttMsg;
import com.myproject.retrofit.MessageApi;
import com.myproject.retrofit.RetrofitService;

import java.util.logging.Level;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainTest extends AppCompatActivity {
    TextView tvTopic, tvKey, tvValue;
    EditText etTopic, etKey, etValue;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);

        tvTopic = findViewById(R.id.tvTopic);
        tvKey = findViewById(R.id.tvKey);
        tvValue = findViewById(R.id.tvValue);
        etTopic = findViewById(R.id.etTopic);
        etKey = findViewById(R.id.etKey);
        etValue = findViewById(R.id.etValue);
        button = findViewById(R.id.btnSave);

        init();

    }

    public void init(){
        RetrofitService retrofitService = new RetrofitService();
        MessageApi messageApi = retrofitService.getRetrofit().create(MessageApi.class);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String topic = etTopic.getText().toString();
                String key = etKey.getText().toString();
                String value = etValue.getText().toString();

                MqttMsg mqttMsg = new MqttMsg();
                mqttMsg.setTopic("topic");
                mqttMsg.setHashKey("key");
                mqttMsg.setValue("value");

                messageApi.save(mqttMsg)
                        .enqueue(new Callback<MqttMsg>() {
                            @Override
                            public void onResponse(Call<MqttMsg> call, Response<MqttMsg> response) {
                                Toast.makeText(MainTest.this, "save successful", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<MqttMsg> call, Throwable t) {
                                Toast.makeText(MainTest.this, "save failed", Toast.LENGTH_SHORT).show();
                                Logger.getLogger(MainTest.class.getName()).log(Level.SEVERE, "error", t);
                            }
                        });

            }
        });

    }
}