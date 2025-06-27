package com.example.uas_bd;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class KegiatanBaruController {

    // --- Elemen FXML dari UI ---
    @FXML
    private TextField namaKegiatanField;
    @FXML
    private TextField lokasiField;
    @FXML
    private DatePicker tanggalPicker;
    @FXML
    private TextField waktuMulaiField;
    @FXML
    private TextField waktuSelesaiField;
    @FXML
    private Button simpanButton;
    @FXML
    private ComboBox<Club> clubComboBox;
    @FXML
    private ComboBox<JenisKegiatan> jenisKegiatanComboBox;
    @FXML
    private Label validationLabel;
    @FXML
    private VBox rootVBox; // fx:id untuk root VBox

    @FXML
    public void initialize() {
        // Atur label validasi agar tidak terlihat dan tidak memakan tempat
        validationLabel.setVisible(false);
        validationLabel.setManaged(false);

        // Validasi hak akses. Hanya 'Pengurus' yang boleh mengakses halaman ini.
        if (!"Pengurus".equals(UserSession.getLoggedInRole())) {
            showError("Akses Ditolak", "Hanya pengurus yang dapat menambah kegiatan baru.");
            // Nonaktifkan seluruh form jika pengguna bukan pengurus
            if (rootVBox != null) {
                rootVBox.setDisable(true);
            }
            return;
        }

        // Muat data awal untuk ComboBox
        loadClubs();
        loadJenisKegiatan();
    }

    /**
     * Memuat daftar klub dari database ke dalam clubComboBox.
     */
    private void loadClubs() {
        ObservableList<Club> clubs = FXCollections.observableArrayList();
        String query = "SELECT id_club, nama_club FROM club ORDER BY nama_club ASC";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                clubs.add(new Club(rs.getInt("id_club"), rs.getString("nama_club")));
            }
            clubComboBox.setItems(clubs);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Kesalahan Database", "Gagal memuat daftar klub: " + e.getMessage());
        }
    }

    /**
     * Memuat daftar jenis kegiatan dari database ke dalam jenisKegiatanComboBox.
     */
    private void loadJenisKegiatan() {
        ObservableList<JenisKegiatan> jenisList = FXCollections.observableArrayList();
        String query = "SELECT id_jenis, nama_jenis FROM jenis_kegiatan ORDER BY nama_jenis ASC";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                jenisList.add(new JenisKegiatan(rs.getInt("id_jenis"), rs.getString("nama_jenis")));
            }
            jenisKegiatanComboBox.setItems(jenisList);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Kesalahan Database", "Gagal memuat jenis kegiatan: " + e.getMessage());
        }
    }

    /**
     * Dijalankan saat tombol "Simpan Kegiatan" diklik.
     * Mengelola validasi input sebelum menyimpan.
     */
    @FXML
    private void handleSimpanKegiatanAction() {
        // Sembunyikan label error setiap kali tombol diklik
        validationLabel.setVisible(false);
        validationLabel.setManaged(false);

        // 1. Validasi field kosong
        if (isAnyFieldEmpty()) {
            showValidationError("Semua field wajib diisi.");
            return;
        }

        try {
            // 2. Validasi format waktu dan logika waktu
            LocalTime waktuMulai = LocalTime.parse(waktuMulaiField.getText());
            LocalTime waktuSelesai = LocalTime.parse(waktuSelesaiField.getText());

            if (waktuSelesai.isBefore(waktuMulai)) {
                showValidationError("Waktu Selesai tidak boleh sebelum Waktu Mulai.");
                return;
            }

            // 3. Jika semua validasi lolos, lanjutkan proses penyimpanan ke database
            saveKegiatanToDatabase(waktuMulai, waktuSelesai);

        } catch (DateTimeParseException e) {
            // Tangani jika format waktu salah (bukan HH:mm)
            showValidationError("Format waktu salah. Gunakan HH:mm (contoh: 14:30).");
        }
    }

    /**
     * Menyimpan data kegiatan ke dalam database.
     */
    private void saveKegiatanToDatabase(LocalTime waktuMulai, LocalTime waktuSelesai) {
        String query = "INSERT INTO kegiatan_club (nama_kegiatan, lokasi, tanggal, waktu_mulai, waktu_selesai, id_club, id_jenis) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, namaKegiatanField.getText());
            pstmt.setString(2, lokasiField.getText());
            pstmt.setDate(3, java.sql.Date.valueOf(tanggalPicker.getValue()));
            pstmt.setTime(4, Time.valueOf(waktuMulai));
            pstmt.setTime(5, Time.valueOf(waktuSelesai));
            pstmt.setInt(6, clubComboBox.getValue().getId());
            pstmt.setInt(7, jenisKegiatanComboBox.getValue().getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Kegiatan baru berhasil disimpan!");
                clearForm(); // Kosongkan form setelah berhasil
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Kesalahan Database", "Terjadi error saat menyimpan: " + e.getMessage());
        }
    }

    /**
     * Memeriksa apakah ada field input yang masih kosong.
     * @return true jika ada field yang kosong.
     */
    private boolean isAnyFieldEmpty() {
        return namaKegiatanField.getText().trim().isEmpty() ||
                lokasiField.getText().trim().isEmpty() ||
                tanggalPicker.getValue() == null ||
                waktuMulaiField.getText().trim().isEmpty() ||
                waktuSelesaiField.getText().trim().isEmpty() ||
                clubComboBox.getValue() == null ||
                jenisKegiatanComboBox.getValue() == null;
    }

    /**
     * Menampilkan pesan error validasi langsung di UI menggunakan Label.
     * @param message Pesan error yang akan ditampilkan.
     */
    private void showValidationError(String message) {
        validationLabel.setText(message);
        validationLabel.setVisible(true);
        validationLabel.setManaged(true);
    }

    /**
     * Membersihkan semua input field di form.
     */
    private void clearForm() {
        namaKegiatanField.clear();
        lokasiField.clear();
        tanggalPicker.setValue(null);
        waktuMulaiField.clear();
        waktuSelesaiField.clear();
        clubComboBox.getSelectionModel().clearSelection();
        jenisKegiatanComboBox.getSelectionModel().clearSelection();
    }

    // Helper methods untuk menampilkan dialog Alert
    private void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- Kelas inner untuk ComboBox ---
    public static class Club {
        private final int id;
        private final String nama;
        public Club(int id, String nama) { this.id = id; this.nama = nama; }
        public int getId() { return id; }
        @Override public String toString() { return nama; }
    }

    public static class JenisKegiatan {
        private final int id;
        private final String nama;
        public JenisKegiatan(int id, String nama) { this.id = id; this.nama = nama; }
        public int getId() { return id; }
        @Override public String toString() { return nama; }
    }
}