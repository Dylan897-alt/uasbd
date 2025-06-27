package com.example.uas_bd;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.time.*;

public class KegiatanBaruController {

    @FXML private TextField namaKegiatanField, lokasiField, waktuMulaiField, waktuSelesaiField;
    @FXML private DatePicker tanggalPicker;

    // Simulasi: ID club dari user yang login
    private int idClubAdmin = 1;

    @FXML
    private void handleSimpan() {
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/hr_bd_a", "username", "password")) {

            String nama = namaKegiatanField.getText();
            String lokasi = lokasiField.getText();
            LocalDate tanggal = tanggalPicker.getValue();
            LocalTime waktuMulai = LocalTime.parse(waktuMulaiField.getText());
            LocalTime waktuSelesai = LocalTime.parse(waktuSelesaiField.getText());

            // Gunakan INSERT tanpa id_kegiatan → biar auto-generate
            String sql = "INSERT INTO kegiatan_club (nama_kegiatan, waktu_mulai, waktu_selesai, tanggal, lokasi, id_club) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, nama);
            stmt.setTime(2, Time.valueOf(waktuMulai));
            stmt.setTime(3, Time.valueOf(waktuSelesai));
            stmt.setTimestamp(4, Timestamp.valueOf(tanggal.atStartOfDay()));
            stmt.setString(5, lokasi);
            stmt.setInt(6, idClubAdmin); // diambil dari session/login admin

            stmt.executeUpdate();

            new Alert(Alert.AlertType.INFORMATION, "Kegiatan berhasil ditambahkan!").show();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Gagal simpan: " + e.getMessage()).show();
        }
    }

    // Kalau nanti mau inject ID club dari luar:
    public void setIdClubAdmin(int id) {
        this.idClubAdmin = id;
    }
}
