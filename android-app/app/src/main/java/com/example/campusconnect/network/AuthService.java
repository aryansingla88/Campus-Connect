package com.example.campusconnect.network;

import com.example.campusconnect.model.LoginRequest;
import com.example.campusconnect.model.LoginResponse;
import com.example.campusconnect.model.RegisterRequest;
import com.example.campusconnect.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {

    @POST("/auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);
    @POST("/auth/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest request);

}