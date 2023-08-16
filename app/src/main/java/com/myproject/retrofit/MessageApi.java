package com.myproject.retrofit;

import com.myproject.model.MqttMsg;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface MessageApi {

    @GET("/message/get-all")
    Call<List<MqttMsg>> getAllMessages();

    @POST("/message/save")
    Call<MqttMsg> save(@Body MqttMsg message);

}
