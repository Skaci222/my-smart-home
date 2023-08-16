package com.myproject.retrofit;

import android.util.Log;

import com.google.gson.Gson;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {

    private Retrofit retrofit;

    public RetrofitService(){
        initializeRetrofit();
    }

    public void initializeRetrofit(){
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.112:8080")
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();
        Log.i("RETROFIT", "initializeRetrofit complete");
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
