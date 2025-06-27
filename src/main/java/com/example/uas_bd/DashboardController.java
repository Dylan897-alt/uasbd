package com.example.uas_bd;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DashboardController {

    @FXML
    private Label jumlahMahasiswaLabel;

    @FXML
    private Label jumlahClubLabel;

    @FXML
    public void initialize() {
        loadStatistik();
    }
    @FXML
    private void handleTambahData() {
        System.out.println("Tambah Data diklik.");
        // Di sini bisa diarahkan ke form tambah data, atau dialog input
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Refresh diklik.");
        loadStatistik(); // panggil ulang fungsi yang isi data
    }


    private void loadStatistik() {
        Connection conn = DatabaseConnector.connect();
        if (conn == null) {
            System.err.println("Gagal konek ke database.");
            return;
        }

        try (Statement stmt = conn.createStatement()) {

            // Ambil jumlah mahasiswa
            ResultSet rsMahasiswa = stmt.executeQuery("SELECT COUNT(*) AS total FROM mahasiswa");
            if (rsMahasiswa.next()) {
                int totalMahasiswa = rsMahasiswa.getInt("total");
                jumlahMahasiswaLabel.setText(String.valueOf(totalMahasiswa));
            }

            // Ambil jumlah club
            ResultSet rsClub = stmt.executeQuery("SELECT COUNT(*) AS total FROM club");
            if (rsClub.next()) {
                int totalClub = rsClub.getInt("total");
                jumlahClubLabel.setText(String.valueOf(totalClub));
            }

        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
    }
}
