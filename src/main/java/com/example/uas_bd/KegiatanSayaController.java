package com.example.uas_bd;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Controller untuk halaman "Kegiatan Saya".
 * Menampilkan riwayat kegiatan yang diikuti oleh anggota beserta status presensinya.
 */
public class KegiatanSayaController {

    @FXML
    private VBox kegiatanContainer; // Container dari FXML untuk menampung kartu kegiatan

    @FXML
    public void initialize() {
        loadRiwayatKegiatan();
    }

    /**
     * Metode utama untuk memuat data dari database dan menampilkannya di UI.
     */
    private void loadRiwayatKegiatan() {
        kegiatanContainer.getChildren().clear(); // Bersihkan container sebelum memuat data baru

        String loggedInNrp = UserSession.getLoggedInNrp();
        if (loggedInNrp == null || !UserSession.isLoggedIn()) {
            Label errorLabel = new Label("Sesi tidak ditemukan. Silakan login kembali.");
            kegiatanContainer.getChildren().add(errorLabel);
            return;
        }

        // --- QUERY DIPERBARUI ---
        // Menambahkan pengambilan waktu_mulai, waktu_selesai, dan lokasi
        String query = "SELECT kg.nama_kegiatan, c.nama_club, kg.tanggal, " +
                "kg.waktu_mulai, kg.waktu_selesai, kg.lokasi, " +
                "COALESCE(p.status_kehadiran, 'Belum direkap') AS status_presensi " +
                "FROM registrasi r " +
                "JOIN kegiatan_club kg ON r.id_kegiatan = kg.id_kegiatan " +
                "JOIN club c ON kg.id_club = c.id_club " +
                "LEFT JOIN presensi p ON r.id_registrasi = p.id_registrasi " +
                "WHERE r.nrp = ? " +
                "ORDER BY kg.tanggal DESC";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, loggedInNrp);
            ResultSet rs = pstmt.executeQuery();

            boolean hasActivities = false;
            while (rs.next()) {
                hasActivities = true;
                String namaKegiatan = rs.getString("nama_kegiatan");
                String namaClub = rs.getString("nama_club");
                LocalDate tanggal = rs.getObject("tanggal", LocalDate.class);
                LocalTime waktuMulai = rs.getObject("waktu_mulai", LocalTime.class);
                LocalTime waktuSelesai = rs.getObject("waktu_selesai", LocalTime.class);
                String lokasi = rs.getString("lokasi");
                String statusPresensi = rs.getString("status_presensi");

                // Buat kartu visual dengan data yang lebih lengkap
                VBox card = createKegiatanCard(namaKegiatan, namaClub, tanggal, waktuMulai, waktuSelesai, lokasi, statusPresensi);
                kegiatanContainer.getChildren().add(card);
            }

            if (!hasActivities) {
                Label noActivitiesLabel = new Label("Anda belum pernah mendaftar di kegiatan mana pun.");
                noActivitiesLabel.setFont(Font.font("System", 16));
                kegiatanContainer.setAlignment(Pos.CENTER);
                kegiatanContainer.getChildren().add(noActivitiesLabel);
            }

        } catch (SQLException e) {
            Label dbErrorLabel = new Label("Gagal memuat riwayat kegiatan: Kesalahan database.");
            kegiatanContainer.getChildren().add(dbErrorLabel);
        }
    }

    /**
     * Membuat satu kartu (VBox) yang menampilkan detail satu riwayat kegiatan.
     * @return VBox yang siap ditampilkan.
     */
    private VBox createKegiatanCard(String namaKegiatan, String namaClub, LocalDate tanggal, LocalTime waktuMulai, LocalTime waktuSelesai, String lokasi, String statusPresensi) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #008000; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Baris Atas: Nama Kegiatan dan Status Presensi
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label namaKegiatanLabel = new Label(namaKegiatan);
        namaKegiatanLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label statusLabel = new Label(statusPresensi);
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        statusLabel.setPadding(new Insets(5, 10, 5, 10));
        statusLabel.setStyle("-fx-background-radius: 15;");

        switch (statusPresensi.toLowerCase()) {
            case "hadir":
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                break;
            case "tidak hadir":
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                break;
            default:
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #e2e3e5; -fx-text-fill: #383d41;");
                break;
        }

        topRow.getChildren().addAll(namaKegiatanLabel, spacer, statusLabel);

        // Informasi Tambahan
        Label clubLabel = new Label("Penyelenggara: " + namaClub);
        clubLabel.setTextFill(Color.BLACK);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
        Label tanggalLabel = new Label("Tanggal: " + tanggal.format(dateFormatter));
        tanggalLabel.setTextFill(Color.BLACK);


        // --- LABEL BARU UNTUK WAKTU DAN RUANGAN ---
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String waktuText = waktuMulai.format(timeFormatter) + " - " + waktuSelesai.format(timeFormatter);
        Label waktuLabel = new Label("Waktu: " + waktuText);
        waktuLabel.setTextFill(Color.BLACK);

        Label lokasiLabel = new Label("Ruangan: " + lokasi);
        lokasiLabel.setTextFill(Color.BLACK);

        // Menambahkan semua label ke dalam kartu
        card.getChildren().addAll(topRow, clubLabel, tanggalLabel, waktuLabel, lokasiLabel);
        return card;
    }
}
