package com.example.campus_connect.api;

import com.example.campus_connect.model.LoginRequest;
import com.example.campus_connect.model.LoginResponse;
import com.example.campus_connect.model.RegisterRequest;
import com.example.campus_connect.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);
    @POST("/auth/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest request);

}