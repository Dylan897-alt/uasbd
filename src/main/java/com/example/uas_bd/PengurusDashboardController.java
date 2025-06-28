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

public class PengurusDashboardController {

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
                }

            } catch (SQLException e) {
                showErrorPopup("Kesalahan database", e.getMessage());
                welcomeLabel.setText("Selamat datang, Pengurus!");
            }
        }
    }

    @FXML
    public void handleGoToEditClubs(ActionEvent event) {
        navigateTo(event, "edit-club.fxml");
    }

    @FXML
    public void handleGoToTambahKegiatan(ActionEvent event) {
        navigateTo(event, "kegiatan-baru.fxml");
    }

    private void navigateTo(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showErrorPopup("Kesalahan memuat halaman", e.getMessage());
        }
    }

    private void showErrorPopup(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Kesalahan");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}