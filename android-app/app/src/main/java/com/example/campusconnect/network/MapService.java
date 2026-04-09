package com.example.campusconnect.network;

import com.example.campusconnect.model.Poi;
import com.example.campusconnect.model.UserPresence;
import com.example.campusconnect.model.Event;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface MapService {


    @GET("presence/visible")
    Call<List<UserPresence>> getVisibleUsers();


    @POST("presence/update")
    Call<String> updatePresence(@Body UserPresence user);


    @GET("pois")
    Call<List<Poi>> getPOIs();


    @POST("/events")
    Call<Map<String, Object>> postEvent(@Body Event event);

    @GET("/events")
    Call<List<Event>> getEvents();
}