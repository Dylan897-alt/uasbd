package com.example.uas_bd;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfileController {

    @FXML private Label nrpLabel;
    @FXML private Label namaLabel;
    @FXML private Label emailLabel;
    @FXML private Label programLabel;
    @FXML private Label prodiLabel;

    @FXML
    public void initialize() {
        String loggedInNrp = UserSession.getLoggedInNrp();

        if (loggedInNrp == null) {
            nrpLabel.setText("NRP: Tidak ditemukan");
            return;
        }

        String query = """
            SELECT m.nrp, m.nama, m.email, pm.nama_program, pr.nama_prodi
            FROM mahasiswa m
            JOIN program_mhs pm ON m.id_program = pm.id_program
            JOIN prodi pr ON m.id_prodi = pr.id_prodi
            WHERE m.nrp = ?
        """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, loggedInNrp);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nrpLabel.setText("NRP: " + rs.getString("nrp"));
                namaLabel.setText("Nama: " + rs.getString("nama"));
                emailLabel.setText("Email: " + rs.getString("email"));
                programLabel.setText("Program: " + rs.getString("nama_program"));
                prodiLabel.setText("Prodi: " + rs.getString("nama_prodi"));
            } else {
                nrpLabel.setText("NRP: Tidak ditemukan di database.");
            }

        } catch (SQLException e) {
            showErrorAlert("Terjadi kesalahan saat mengambil data", e.getMessage());
            nrpLabel.setText("Terjadi kesalahan saat mengambil data.");
        } catch (Exception e) {
            showErrorAlert("Kesalahan tidak terduga", e.getMessage());
            nrpLabel.setText("Terjadi kesalahan.");
        }
    }

    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}