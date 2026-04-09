package com.campus.Campus_Connect.repository;

import com.campus.Campus_Connect.model.Poi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

@Repository
public class PoiRepository {


    @Autowired
    private DataSource dataSource;

    public List<Poi> getAllPois() {

        List<Poi> pois = new ArrayList<>();

        String query = "SELECT * FROM poi";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Poi poi = new Poi(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                );
                pois.add(poi);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error fetching POIs", e);
        }

        return pois;
    }
}
