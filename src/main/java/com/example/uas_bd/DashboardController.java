package com.example.uas_bd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        String nrp = UserSession.getLoggedInNrp();

        if (nrp != null) {
            try (Connection conn = DatabaseConnector.connect();
                 PreparedStatement stmt = conn.prepareStatement("SELECT nama FROM mahasiswa WHERE nrp = ?")) {

                stmt.setString(1, nrp);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String nama = rs.getString("nama");
                    welcomeLabel.setText("Selamat datang, " + nama + "!");
                } else {
                    welcomeLabel.setText("Selamat datang!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                welcomeLabel.setText("Selamat datang!");
            }
        } else {
            welcomeLabel.setText("Selamat datang!");
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
