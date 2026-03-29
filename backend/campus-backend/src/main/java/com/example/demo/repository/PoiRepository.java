package com.example.demo.repository;

import com.example.demo.model.Poi;

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

        try {
            Connection conn = dataSource.getConnection();

            String query = "SELECT * FROM poi";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Poi poi = new Poi(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                );
                pois.add(poi);
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return pois;
    }
}
