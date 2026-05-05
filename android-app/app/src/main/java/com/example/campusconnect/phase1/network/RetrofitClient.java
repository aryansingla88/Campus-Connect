package com.example.campusconnect.network;

import com.example.campusconnect.network.config.ApiConfig;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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


    public static AuthService getAuthService() {
        return getInstance().create(AuthService.class);
    }


    public static MapService getMapService() {
        return getInstance().create(MapService.class);
    }


    public static PostService getPostService() {
        return getInstance().create(PostService.class);
    }
}