package com.example.uas_bd;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class SignUpController {

    // --- Deklarasi Elemen FXML (sesuai fx:id di FXML) ---
    @FXML
    private TextField namaField;
    @FXML
    private TextField nrpField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label passwordMismatchLabel; // Untuk pesan error password tidak cocok
    @FXML
    private ComboBox<String> prodiComboBox;
    @FXML
    private ComboBox<String> programComboBox;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Button daftarButton; // Opsional, jika kamu ingin memanipulasi button ini
    @FXML
    private Hyperlink loginHyperlink;

    // --- Metode Inisialisasi ---
    @FXML
    public void initialize() {
        // 1. Sembunyikan label error password saat pertama kali dimuat
        passwordMismatchLabel.setVisible(false);

        // 2. Isi ComboBox untuk Role
        ObservableList<String> roles = FXCollections.observableArrayList("Anggota", "Pengurus");
        roleComboBox.setItems(roles);
        roleComboBox.getSelectionModel().selectFirst(); // Pilih 'Anggota' sebagai default

        // 3. Isi ComboBox untuk Prodi (data dari database)
        loadProdiData();

        // 4. Isi ComboBox untuk Program (data dari database)
        loadProgramData();

        // 5. Tambahkan listener untuk validasi password secara real-time
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePasswords();
        });
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePasswords();
        });
    }

    // --- Metode untuk Mengisi Data ComboBox dari Database ---

    private void loadProdiData() {
        ObservableList<String> prodiList = FXCollections.observableArrayList();
        String DB_URL = "jdbc:postgresql://localhost:5432/Project_1_BasisData"; // Ganti dengan URL DB Anda
        String USER = "postgres"; // Ganti dengan user DB Anda
        String PASS = "Untukkuliah123"; // Ganti dengan password DB Anda

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nama_prodi FROM prodi")) {

            while (rs.next()) {
                prodiList.add(rs.getString("nama_prodi"));
            }
            prodiComboBox.setItems(prodiList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat data Program Studi: " + e.getMessage());
        }
    }

    private void loadProgramData() {
        ObservableList<String> programList = FXCollections.observableArrayList();
        String DB_URL = "jdbc:postgresql://localhost:5432/Project_1_BasisData"; // Ganti dengan URL DB Anda
        String USER = "postgres"; // Ganti dengan user DB Anda
        String PASS = "Untukkuliah123"; // Ganti dengan password DB Anda

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nama_program FROM program_mhs")) { // Asumsi nama_program ada di program_mhs

            while (rs.next()) {
                programList.add(rs.getString("nama_program"));
            }
            programComboBox.setItems(programList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat data Program Mahasiswa: " + e.getMessage());
        }
    }


    // --- Metode Validasi Password ---
    private void validatePasswords() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!password.isEmpty() && !confirmPassword.isEmpty() && !password.equals(confirmPassword)) {
            passwordMismatchLabel.setText("*Password tidak cocok!");
            passwordMismatchLabel.setVisible(true);
        } else {
            passwordMismatchLabel.setVisible(false);
        }
    }

    // --- Metode Event Handler Tombol "Daftar" ---
    @FXML
    private void handleDaftarButtonAction() {
        // Panggil validasi password terakhir kali sebelum submit
        validatePasswords();

        // 1. Validasi Kolom Wajib Diisi & Format
        if (nrpField.getText().isEmpty() || namaField.getText().isEmpty() ||
                emailField.getText().isEmpty() || passwordField.getText().isEmpty() ||
                prodiComboBox.getSelectionModel().isEmpty() || roleComboBox.getSelectionModel().isEmpty()) {

            showAlert(AlertType.WARNING, "Input Tidak Lengkap", "Mohon lengkapi semua kolom wajib (NRP, Nama, Email, Password, Program Studi, Role).");
            return;
        }

        // Validasi Password Mismatch dari label
        if (passwordMismatchLabel.isVisible()) {
            showAlert(AlertType.ERROR, "Kesalahan Pendaftaran", "Password dan Konfirmasi Password tidak cocok.");
            return;
        }

        // Validasi Format Email
        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(AlertType.ERROR, "Format Email Salah", "Format alamat email tidak valid.");
            return;
        }

        // Ambil Data dari Form
        String nrp = nrpField.getText();
        String nama = namaField.getText();
        String email = emailField.getText();
        String password = passwordField.getText(); // Nanti perlu di-hash!
        String selectedProdiNama = prodiComboBox.getSelectionModel().getSelectedItem();
        String selectedProgramNama = programComboBox.getSelectionModel().getSelectedItem(); // Bisa null
        String selectedRole = roleComboBox.getSelectionModel().getSelectedItem();

        // --- Logika Penyimpanan ke Database ---
        // Anda perlu mengambil id_prodi dan id_program dari nama yang dipilih
        // Ini membutuhkan query tambahan ke DB atau menyimpan ID bersama nama di ComboBox
        String idProdi = getProdiIdByName(selectedProdiNama);
        String idProgram = (selectedProgramNama != null && !selectedProgramNama.isEmpty()) ? getProgramIdByName(selectedProgramNama) : null;


        // Contoh penyimpanan data (ANDA PERLU MENGIMPLEMENTASIKAN INI DENGAN BENAR)
        String DB_URL = "jdbc:postgresql://localhost:5432/namadatabaseanda"; // Ganti dengan URL DB Anda
        String USER = "userdbanda"; // Ganti dengan user DB Anda
        String PASS = "passdbanda"; // Ganti dengan password DB Anda

        String SQL_INSERT_MAHASISWA = "INSERT INTO mahasiswa (nrp, nama, email, id_program, id_prodi, role) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_MAHASISWA)) {

            pstmt.setString(1, nrp);
            pstmt.setString(2, nama);
            pstmt.setString(3, email);
            pstmt.setString(4, idProgram); // id_program bisa null
            pstmt.setString(5, idProdi);
            pstmt.setString(6, selectedRole);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                showAlert(AlertType.INFORMATION, "Pendaftaran Berhasil", "Akun Anda berhasil didaftarkan!");
                // Arahkan ke halaman login atau dashboard
                // Contoh: navigateToLoginScene();
            } else {
                showAlert(AlertType.ERROR, "Pendaftaran Gagal", "Terjadi kesalahan saat mendaftarkan akun.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Validasi duplikasi NRP atau Email dari database
            if (e.getMessage().contains("duplicate key value violates unique constraint \"mahasiswa_nrp_pkey\"")) {
                showAlert(AlertType.ERROR, "Pendaftaran Gagal", "NRP '" + nrp + "' sudah terdaftar. Mohon gunakan NRP lain.");
            } else if (e.getMessage().contains("duplicate key value violates unique constraint \"mahasiswa_email_key\"")) {
                showAlert(AlertType.ERROR, "Pendaftaran Gagal", "Email '" + email + "' sudah terdaftar. Mohon gunakan email lain.");
            } else {
                showAlert(AlertType.ERROR, "Kesalahan Database", "Terjadi kesalahan database: " + e.getMessage());
            }
        }
    }

    // --- Metode Event Handler Hyperlink "Login" ---
    @FXML
    private void handleLoginLinkAction() {
        // Logika untuk beralih ke halaman Login
        System.out.println("Navigasi ke halaman Login...");
        // Contoh: Load FXML baru dan tampilkan
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginHyperlink.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (
                IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Kesalahan Navigasi", "Gagal memuat halaman login.");
        }
    }

    // --- Helper Method untuk Menampilkan Alert ---
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- Helper Method untuk mendapatkan ID dari nama (perlu query database) ---
    private String getProdiIdByName(String namaProdi) {
        String id = null;
        String DB_URL = "jdbc:postgresql://localhost:5432/namadatabaseanda";
        String USER = "userdbanda";
        String PASS = "passdbanda";
        String SQL = "SELECT id_prodi FROM prodi WHERE nama_prodi = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, namaProdi);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                id = rs.getString("id_prodi");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Kesalahan Database", "Gagal mendapatkan ID Prodi: " + e.getMessage());
        }
        return id;
    }

    private String getProgramIdByName(String namaProgram) {
        String id = null;
        String DB_URL = "jdbc:postgresql://localhost:5432/Project_1_BasisData";
        String USER = "userdbanda";
        String PASS = "passdbanda";
        String SQL = "SELECT id_program FROM program_mhs WHERE nama_program = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, namaProgram);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                id = rs.getString("id_program");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Kesalahan Database", "Gagal mendapatkan ID Program: " + e.getMessage());
        }
        return id;
    }

    // --- PENTING: Implementasikan navigasi ke scene lain (misalnya login) di sini ---
    // private void navigateToLoginScene() {
    //     // Implementasi loading scene login.fxml
    // }
}