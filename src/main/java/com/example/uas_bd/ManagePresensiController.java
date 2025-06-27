package com.example.uas_bd;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.example.uas_bd.PresensiDetailController;



public class ManagePresensiController {
    @FXML
    private VBox kegiatanContainer;

    @FXML public void initialize() {
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
            e.printStackTrace();
        }
    }

    private void openPresensiDetailPage(int idKegiatan) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("presensi-detail.fxml"));
            Parent root = loader.load(); // ini WAJIB
            PresensiDetailController controller = loader.getController(); // baru ini bisa
            controller.setKegiatanId(idKegiatan);

            Stage stage = (Stage) kegiatanContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
