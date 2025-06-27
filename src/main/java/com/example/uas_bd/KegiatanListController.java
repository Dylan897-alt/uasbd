package com.example.uas_bd; // Ganti dengan package Anda

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class KegiatanListController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> jenisKegiatanFilterComboBox;
    @FXML
    private ComboBox<String> clubFilterComboBox;
    @FXML
    private Button resetFilterButton;
    @FXML
    private Button addKegiatanButton; // Hanya untuk pengurus
    @FXML
    private FlowPane kegiatanFlowPane; // Wadah untuk kartu-kartu kegiatan

    // Data kegiatan yang dimuat dari DB
    private ObservableList<Kegiatan> masterKegiatanList = FXCollections.observableArrayList();
    // Filtered list (untuk pencarian/filter)
    private ObservableList<Kegiatan> filteredKegiatanList = FXCollections.observableArrayList();

    // Simulasikan role pengguna yang sedang login
    // Nanti ini akan didapatkan dari objek sesi pengguna setelah login berhasil
    private String currentUserRole = "Anggota"; // Default atau ambil dari sesi

//    // Database Configuration
//    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Project_1_BasisData"; // Ganti
//    private static final String DB_USER = "postgres"; // Ganti
//    private static final String DB_PASS = "Untukkuliah123"; // Ganti

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/uas_bd";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "Dylan030506";

//    private static final String DB_URL = "jdbc:postgresql://localhost:5432/HR BD A";
//    private static final String DB_USER = "postgres";
//    private static final String DB_PASS = "POSTGRESSQL";

    @FXML
    public void initialize() {
        // Atur visibilitas tombol tambah kegiatan berdasarkan role
        addKegiatanButton.setVisible("Pengurus".equals(currentUserRole));
        addKegiatanButton.setOnAction(event -> handleTambahKegiatan()); // Event handler untuk tombol tambah

        // Listener untuk pencarian dan filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterKegiatanData());
        jenisKegiatanFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterKegiatanData());
        clubFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterKegiatanData());
        resetFilterButton.setOnAction(event -> resetFilters());

        // Memuat data awal dari database
        loadKegiatanData(); // Memuat data ke masterKegiatanList
        // populateKegiatanCards(masterKegiatanList); // Initial population moved into filterKegiatanData to ensure all filters are applied

        // Memuat pilihan filter
        loadFilterOptions();
    }

    private void loadKegiatanData() {
        masterKegiatanList.clear();
        String query = "SELECT " +
                "kc.id_kegiatan, kc.nama_kegiatan, c.nama_club, kc.tanggal, " +
                "kc.waktu_mulai, kc.waktu_selesai, kc.lokasi, jk.nama_jenis " +
                "FROM kegiatan_club kc " +
                "JOIN club c ON kc.id_club = c.id_club " +
                "JOIN jenis_kegiatan jk ON kc.id_jenis = jk.id_jenis";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                masterKegiatanList.add(new Kegiatan(
                        rs.getInt("id_kegiatan"),
                        rs.getString("nama_kegiatan"),
                        rs.getString("nama_club"),
                        rs.getObject("tanggal", LocalDate.class), // Ambil sebagai LocalDate
                        rs.getObject("waktu_mulai", LocalTime.class), // Ambil sebagai LocalTime
                        rs.getObject("waktu_selesai", LocalTime.class), // Ambil sebagai LocalTime
                        rs.getString("lokasi"),
                        rs.getString("nama_jenis")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat data kegiatan: " + e.getMessage());
        }
        // Setelah data dimuat, terapkan filter awal (atau tampilkan semua jika belum ada filter)
        filterKegiatanData();
    }

    private void loadFilterOptions() {
        ObservableList<String> jenisList = FXCollections.observableArrayList();
        jenisList.add("Semua Jenis"); // Opsi untuk tidak memfilter
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nama_jenis FROM jenis_kegiatan")) {
            while (rs.next()) {
                jenisList.add(rs.getString("nama_jenis"));
            }
            jenisKegiatanFilterComboBox.setItems(jenisList);
            jenisKegiatanFilterComboBox.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat pilihan jenis kegiatan: " + e.getMessage());
        }

        ObservableList<String> clubList = FXCollections.observableArrayList();
        clubList.add("Semua Club"); // Opsi untuk tidak memfilter
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nama_club FROM club")) {
            while (rs.next()) {
                clubList.add(rs.getString("nama_club"));
            }
            clubFilterComboBox.setItems(clubList);
            clubFilterComboBox.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat pilihan club: " + e.getMessage());
        }
    }

    private void filterKegiatanData() {
        String searchText = searchField.getText().toLowerCase();
        String selectedJenis = jenisKegiatanFilterComboBox.getSelectionModel().getSelectedItem();
        String selectedClub = clubFilterComboBox.getSelectionModel().getSelectedItem();

        filteredKegiatanList.clear(); // Bersihkan daftar yang difilter

        for (Kegiatan kegiatan : masterKegiatanList) {
            boolean matchesSearch = (searchText.isEmpty() ||
                    kegiatan.getNamaKegiatan().toLowerCase().contains(searchText) ||
                    kegiatan.getLokasi().toLowerCase().contains(searchText) ||
                    kegiatan.getClubPenyelenggara().toLowerCase().contains(searchText));

            boolean matchesJenis = (selectedJenis == null || selectedJenis.equals("Semua Jenis") ||
                    kegiatan.getJenisKegiatan().equals(selectedJenis));

            boolean matchesClub = (selectedClub == null || selectedClub.equals("Semua Club") ||
                    kegiatan.getClubPenyelenggara().equals(selectedClub));

            if (matchesSearch && matchesJenis && matchesClub) {
                filteredKegiatanList.add(kegiatan);
            }
        }
        populateKegiatanCards(filteredKegiatanList); // Tampilkan kartu dari daftar yang sudah difilter
    }

    private void populateKegiatanCards(ObservableList<Kegiatan> kegiatanToDisplay) {
        kegiatanFlowPane.getChildren().clear(); // Bersihkan kartu yang ada di UI

        if (kegiatanToDisplay.isEmpty()) {
            Label noDataLabel = new Label("Tidak ada kegiatan yang ditemukan.");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555;");
            kegiatanFlowPane.getChildren().add(noDataLabel);
        } else {
            for (Kegiatan kegiatan : kegiatanToDisplay) {
                try {
                    // Pastikan path ini benar sesuai struktur proyek Anda
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("ActivityCard.fxml"));
                    Node cardNode = loader.load();
                    ActivityCardController cardController = loader.getController();

                    // Kirim data kegiatan, role pengguna, dan referensi ke controller ini
                    cardController.setKegiatanData(kegiatan, currentUserRole, this);

                    kegiatanFlowPane.getChildren().add(cardNode);
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(AlertType.ERROR, "Kesalahan Loading Kartu", "Gagal memuat kartu kegiatan: " + e.getMessage());
                }
            }
        }
    }

    private void resetFilters() {
        searchField.clear();
        jenisKegiatanFilterComboBox.getSelectionModel().selectFirst();
        clubFilterComboBox.getSelectionModel().selectFirst();
        // filterKegiatanData() akan otomatis terpanggil karena listener di initialize()
    }

    // --- Metode Event Handlers dari Tombol Aksi di Kartu (Dipanggil dari ActivityCardController) ---

    // Metode untuk Anggota
    public void handleLihatDetailAnggota(Kegiatan kegiatan) {
        System.out.println("Anggota melihat detail kegiatan: " + kegiatan.getNamaKegiatan());
        // Implementasi navigasi ke Halaman "Detail Kegiatan & Pendaftaran"
        // Anda perlu memuat FXML halaman detail dan meneruskan objek 'kegiatan'
        showAlert(AlertType.INFORMATION, "Lihat Detail", "Akan menampilkan detail kegiatan: " + kegiatan.getNamaKegiatan());
    }

    public void handleDaftarKegiatan(Kegiatan kegiatan, ActivityCardController cardController) {
        String loggedInNrp = UserSession.getLoggedInNrp();
        if (loggedInNrp == null || !UserSession.isLoggedIn()) {
            showAlert(AlertType.ERROR, "Sesi Tidak Valid", "Anda harus login untuk bisa mendaftar kegiatan.");
            return;
        }

        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Pendaftaran");
        confirmAlert.setHeaderText("Mendaftar ke: " + kegiatan.getNamaKegiatan());
        confirmAlert.setContentText("Apakah Anda yakin ingin mendaftar ke kegiatan ini?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Kirim cardController ke metode registrasi
                registerToActivityInDb(loggedInNrp, kegiatan.getIdKegiatan(), kegiatan.getNamaKegiatan(), cardController);
            }
        });
    }

    /**
     * Logika untuk memasukkan data pendaftaran ke tabel 'registrasi' di database.
     * @param nrp NRP pengguna yang mendaftar.
     * @param idKegiatan ID kegiatan yang didaftari.
     * @param namaKegiatan Nama kegiatan untuk pesan notifikasi.
     */
    private void registerToActivityInDb(String nrp, int idKegiatan, String namaKegiatan, ActivityCardController cardController) {
        String query = "INSERT INTO registrasi (nrp, id_kegiatan, tanggal_registrasi, status_registrasi) VALUES (?, ?, CURRENT_TIMESTAMP, ?)";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nrp);
            pstmt.setInt(2, idKegiatan);
            pstmt.setString(3, "Terdaftar");

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert(AlertType.INFORMATION, "Pendaftaran Berhasil", "Anda berhasil terdaftar di kegiatan: " + namaKegiatan);
                // Panggil metode untuk update UI kartu
                cardController.updateToRegisteredState();
            }

        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // Handle jika sudah terdaftar
                showAlert(AlertType.WARNING, "Pendaftaran Gagal", "Anda sudah pernah terdaftar di kegiatan ini.");
                // Jika sudah terdaftar, update juga UI-nya agar konsisten
                cardController.updateToRegisteredState();
            } else {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Kesalahan Database", "Gagal mendaftar kegiatan: " + e.getMessage());
            }
        }
    }

    // Metode untuk Pengurus
    public void handleEditKegiatan(Kegiatan kegiatan) {
        System.out.println("Pengurus mengedit kegiatan: " + kegiatan.getNamaKegiatan());
        // Implementasi navigasi ke "Form Tambah/Edit Kegiatan" (mode edit)
        // Anda perlu memuat FXML form dan meneruskan objek 'kegiatan'
        showAlert(AlertType.INFORMATION, "Edit Kegiatan", "Akan membuka form edit untuk: " + kegiatan.getNamaKegiatan());
    }

    public void handleHapusKegiatan(Kegiatan kegiatan) {
        System.out.println("Pengurus menghapus kegiatan: " + kegiatan.getNamaKegiatan());
        // Implementasi konfirmasi hapus (Alert) dan logika penghapusan dari DB
        // Contoh konfirmasi:
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Hapus");
        confirmAlert.setHeaderText("Hapus Kegiatan?");
        confirmAlert.setContentText("Apakah Anda yakin ingin menghapus kegiatan '" + kegiatan.getNamaKegiatan() + "'?");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // Logika penghapusan dari database
                deleteKegiatanFromDb(kegiatan.getIdKegiatan());
            }
        });
    }

    // Logika penghapusan database (Contoh)
    private void deleteKegiatanFromDb(int idKegiatan) {
        String query = "DELETE FROM kegiatan_club WHERE id_kegiatan = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idKegiatan);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert(AlertType.INFORMATION, "Hapus Berhasil", "Kegiatan berhasil dihapus.");
                loadKegiatanData(); // Muat ulang data setelah penghapusan
            } else {
                showAlert(AlertType.ERROR, "Hapus Gagal", "Kegiatan tidak ditemukan atau gagal dihapus.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Kesalahan Database", "Gagal menghapus kegiatan: " + e.getMessage());
        }
    }

    public void handleTambahKegiatan() {
        System.out.println("Pengurus menambah kegiatan baru...");
        // Implementasi navigasi ke "Form Tambah/Edit Kegiatan" (mode tambah baru)
        showAlert(AlertType.INFORMATION, "Tambah Kegiatan", "Akan membuka form untuk menambah kegiatan baru.");
    }

    // Helper Method untuk Menampilkan Alert
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}