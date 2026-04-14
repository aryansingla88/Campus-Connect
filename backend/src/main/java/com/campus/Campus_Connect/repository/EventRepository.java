package com.campus.Campus_Connect.repository;

import com.campus.Campus_Connect.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Repository
public class EventRepository {

    @Autowired
    private DataSource dataSource;

    public int createEvent(Event event) {
        int eventId = 0;

        try (Connection conn = dataSource.getConnection()) {
            String query = "INSERT INTO events (title, description, latitude, longitude, event_time, created_by) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setDouble(3, event.getLatitude());
            ps.setDouble(4, event.getLongitude());
            ps.setString(5, event.getEvent_time());
            ps.setInt(6, event.getCreated_by());

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

        try (Connection conn = dataSource.getConnection()) { // ✅ fixed

            String query = "SELECT * FROM events";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Event e = new Event();
                e.setId(rs.getInt("id"));
                e.setCreated_by(rs.getInt("created_by"));
                e.setTitle(rs.getString("title"));
                e.setDescription(rs.getString("description"));
                e.setEvent_time(rs.getString("event_time"));
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