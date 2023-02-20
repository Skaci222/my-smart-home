package com.myproject.rest;

import retrofit2.Call;
import retrofit2.http.GET;

public interface HiveDb {

    @GET("mqtt/clients")
    Call<JSONResponse> getMqttClients();
}
