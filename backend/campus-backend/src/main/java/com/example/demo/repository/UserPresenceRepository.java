package com.example.demo.repository;

import com.example.demo.model.UserPresence;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.springframework.beans.factory.annotation.Autowired;

@Repository
public class UserPresenceRepository {

    @Autowired
    private DataSource dataSource;

    public void updatePresence(UserPresence user) {

        try {
            Connection conn = dataSource.getConnection();

            String sql = "REPLACE INTO user_presence (user_id, latitude, longitude) VALUES (?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, user.getUserId());
            ps.setDouble(2, user.getLatitude());
            ps.setDouble(3, user.getLongitude());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}