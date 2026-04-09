package com.campus.Campus_Connect.repository;

import com.campus.Campus_Connect.model.UserPresence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserPresenceRepository {

    @Autowired
    private DataSource dataSource;

    // 🔹 UPDATE / INSERT (UPSERT)
    public void updatePresence(UserPresence user) {

        String sql = "INSERT INTO user_presence (user_id, latitude, longitude, last_update) " +
                "VALUES (?, ?, ?, NOW()) " +
                "ON DUPLICATE KEY UPDATE " +
                "latitude = VALUES(latitude), " +
                "longitude = VALUES(longitude), " +
                "last_update = NOW()";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, user.getUserId());
            ps.setDouble(2, user.getLatitude());
            ps.setDouble(3, user.getLongitude());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating user presence", e);
        }
    }

    // 🔹 GET ACTIVE USERS
    public List<UserPresence> getVisibleUsers() {

        List<UserPresence> list = new ArrayList<>();

        String sql = "SELECT u.id, u.username, up.latitude, up.longitude " +
                "FROM user_presence up " +
                "JOIN users u ON u.id = up.user_id " +
                "WHERE up.last_update > NOW() - INTERVAL 5 MINUTE";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new UserPresence(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching visible users", e);
        }

        return list;
    }
}