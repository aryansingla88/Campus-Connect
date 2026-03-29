package com.example.demo.repository;

import com.example.demo.model.Event;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class EventRepository {

    private String url = "jdbc:mysql://localhost:3306/campusbackend";
    private String username = "root";
    private String password = "Root@321";

    public int createEvent(Event event) {
        int eventId = 0;

        try {
            Connection conn = DriverManager.getConnection(url, username, password);

            String query = "INSERT INTO events (user_id, title, description, date, time, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, event.getUser_id());
            ps.setString(2, event.getTitle());
            ps.setString(3, event.getDescription());
            ps.setString(4, event.getDate());
            ps.setString(5, event.getTime());
            ps.setDouble(6, event.getLatitude());
            ps.setDouble(7, event.getLongitude());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                eventId = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return eventId;
    }

    public List<Event> getAllEvents() {
        List<Event> list = new ArrayList<>();

        try {
            Connection conn = DriverManager.getConnection(url, username, password);

            String query = "SELECT * FROM events";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Event e = new Event();
                e.setId(rs.getInt("id"));
                e.setUser_id(rs.getInt("user_id"));
                e.setTitle(rs.getString("title"));
                e.setDescription(rs.getString("description"));
                e.setDate(rs.getString("date"));
                e.setTime(rs.getString("time"));
                e.setLatitude(rs.getDouble("latitude"));
                e.setLongitude(rs.getDouble("longitude"));

                list.add(e);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
