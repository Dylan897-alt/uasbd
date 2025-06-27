package com.example.uas_bd;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SertifikatController {

    @FXML
    private ListView<String> sertifikatListView;

    @FXML
    public void initialize() {
        loadSertifikat();
    }

    private void loadSertifikat() {
        String sql = """
            SELECT p.no_sertif, k.nama_kegiatan, c.nama_club
            FROM presensi p
            JOIN kegiatan_club k ON p.id_kegiatan = k.id_kegiatan
            JOIN club c ON k.id_club = c.id_club
            WHERE p.no_sertif IS NOT NULL
        """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String noSertif = rs.getString("no_sertif");
                String kegiatanName = rs.getString("nama_kegiatan");
                String clubName = rs.getString("nama_club");

                String display = String.format("Sertifikat: %s\nKegiatan: %s\nClub: %s", noSertif, kegiatanName, clubName);
                sertifikatListView.getItems().add(display);
            }

        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
