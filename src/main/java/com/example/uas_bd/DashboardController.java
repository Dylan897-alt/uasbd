package com.example.uas_bd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

/**
 * Controller untuk halaman Dashboard utama.
 * Menampilkan welcome message dan navigasi ke halaman lain.
 */
public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        try {
            String nrp = UserSession.getLoggedInNrp();

            if (nrp != null) {
                Connection conn = DatabaseConnector.connect();
                if (conn == null) {
                    welcomeLabel.setText("Selamat datang!");
                    showErrorPopup("Koneksi ke database gagal.");
                    return;
                }

                String query = "SELECT nama FROM mahasiswa WHERE nrp = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, nrp);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String nama = rs.getString("nama");
                        welcomeLabel.setText("Selamat datang, " + nama + "!");
                    } else {
                        welcomeLabel.setText("Selamat datang!");
                    }
                }
            } else {
                welcomeLabel.setText("Selamat datang!");
            }

        } catch (Exception e) {
            welcomeLabel.setText("Selamat datang!");
            showErrorPopup("Terjadi kesalahan saat memuat data pengguna.\n\n" + cleanErrorMessage(e));
        }
    }

    @FXML
    private void handleLihatProfil(ActionEvent event) {
        loadScene(event, "profile.fxml");
    }

    @FXML
    private void handleLihatKegiatan(ActionEvent event) {
        loadScene(event, "KegiatanList.fxml");
    }

    @FXML
    private void handleKegiatanSaya(ActionEvent event) {
        loadScene(event, "KegiatanSaya.fxml");
    }

    @FXML
    private void handleSertifikat(ActionEvent event) {
        loadScene(event, "sertifikat.fxml");
    }

    private void loadScene(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            showErrorPopup("Gagal memuat halaman: " + fxmlFile + "\n\n" + cleanErrorMessage(e));
        }
    }

    private void showErrorPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Kesalahan");
        alert.setHeaderText("Terjadi Kesalahan");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String cleanErrorMessage(Exception e) {
        String msg = e.getMessage();
        return (msg != null && !msg.isBlank()) ? msg : "Kesalahan tak dikenal.";
    }
}