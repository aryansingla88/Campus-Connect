package com.example.campus_connect.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.campus_connect.config.ApiConfig;

public class RetrofitClient {

    private static Retrofit retrofit;

    public static Retrofit getInstance() {

        if (retrofit == null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}