package com.example.uas_bd;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ManagePresensiController {
    @FXML
    private VBox kegiatanContainer;

    @FXML
    public void initialize() {
        loadKegiatanList();
    }

    private void loadKegiatanList() {
        String query = "SELECT id_kegiatan, nama_kegiatan FROM kegiatan_club ORDER BY tanggal DESC";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_kegiatan");
                String nama = rs.getString("nama_kegiatan");

                Button btn = new Button(nama);
                btn.setOnAction(e -> openPresensiDetailPage(id));
                kegiatanContainer.getChildren().add(btn);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Kesalahan Database", "Gagal memuat daftar kegiatan.\n\n" + getErrorMessage(e));
        }
    }

    private void openPresensiDetailPage(int idKegiatan) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("presensi-detail.fxml"));
            Parent root = loader.load();
            PresensiDetailController controller = loader.getController();
            controller.setKegiatanId(idKegiatan);

            Stage stage = (Stage) kegiatanContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Kesalahan UI", "Gagal membuka halaman presensi.\n\n" + getErrorMessage(e));
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getErrorMessage(Exception e) {
        return (e.getMessage() != null && !e.getMessage().isBlank())
                ? e.getMessage()
                : "Terjadi kesalahan tak dikenal.";
    }
}