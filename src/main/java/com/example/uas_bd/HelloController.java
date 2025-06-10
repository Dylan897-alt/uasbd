package com.example.uas_bd;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private Label queryResultText;

    @FXML
    protected void onHelloButtonClick() {
        Connection conn = DatabaseConnector.connect();
        if (conn != null) {
            welcomeText.setText("✅ Connected to PostgreSQL!");
            String query = "SELECT kota FROM stasiun WHERE kode_stasiun = ?";

            try {
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, "3");
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String kota = rs.getString("kota");
                    queryResultText.setText(kota);  // SET TEXT TO NEW LABEL
                } else {
                    queryResultText.setText("No user found.");
                }

                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                queryResultText.setText("Query failed: " + e.getMessage());
            }
        } else {
            welcomeText.setText("❌ Connection Failed!");
        }

    }
}