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
// PENTING: Import BCrypt dihilangkan karena tidak digunakan
// import org.mindrot.jbcrypt.BCrypt; // BARIS INI DIHILANGKAN

public class SignUpController {

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
    private Label passwordMismatchLabel;
    @FXML
    private ComboBox<String> prodiComboBox;
    @FXML
    private ComboBox<String> programComboBox;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Button daftarButton;
    @FXML
    private Hyperlink loginHyperlink;

//    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Project_1_BasisData";
//    private static final String DB_USER = "postgres";
//    private static final String DB_PASS = "Untukkuliah123";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/uas_bd";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "Dylan030506";

//    private static final String DB_URL = "jdbc:postgresql://localhost:5432/HR BD A";
//    private static final String DB_USER = "postgres";
//    private static final String DB_PASS = "POSTGRESSQL";

    @FXML
    public void initialize() {
        passwordMismatchLabel.setVisible(false);

        ObservableList<String> roles = FXCollections.observableArrayList("Anggota", "Pengurus");
        roleComboBox.setItems(roles);
        roleComboBox.getSelectionModel().selectFirst();

        loadProdiData();
        loadProgramData();

        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePasswords();
        });
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePasswords();
        });
    }

    private void loadProdiData() {
        ObservableList<String> prodiList = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nama_program FROM program_mhs")) {
            while (rs.next()) {
                programList.add(rs.getString("nama_program"));
            }
            programComboBox.setItems(programList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat data Program Mahasiswa: " + e.getMessage());
        }
    }

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

    @FXML
    private void handleDaftarButtonAction() {
        validatePasswords();

        if (nrpField.getText().isEmpty() || namaField.getText().isEmpty() ||
                emailField.getText().isEmpty() || passwordField.getText().isEmpty() ||
                prodiComboBox.getSelectionModel().isEmpty() || roleComboBox.getSelectionModel().isEmpty()) {
            showAlert(AlertType.WARNING, "Input Tidak Lengkap", "Mohon lengkapi semua kolom wajib (NRP, Nama, Email, Password, Program Studi, Role).");
            return;
        }

        if (passwordMismatchLabel.isVisible()) {
            showAlert(AlertType.ERROR, "Kesalahan Pendaftaran", "Password dan Konfirmasi Password tidak cocok.");
            return;
        }

        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(AlertType.ERROR, "Format Email Salah", "Format alamat email tidak valid.");
            return;
        }

        String nrp = nrpField.getText();
        String nama = namaField.getText();
        String email = emailField.getText();
        String password = passwordField.getText(); // Password dalam bentuk teks biasa
        String selectedProdiNama = prodiComboBox.getSelectionModel().getSelectedItem();
        String selectedProgramNama = programComboBox.getSelectionModel().getSelectedItem();
        String selectedRole = roleComboBox.getSelectionModel().getSelectedItem();

        String idProdi = getProdiIdByName(selectedProdiNama);
        String idProgram = (selectedProgramNama != null && !selectedProgramNama.isEmpty()) ? getProgramIdByName(selectedProgramNama) : null;

        // PERINGATAN SANGAT PENTING: Password disimpan dalam teks biasa
        String SQL_INSERT_MAHASISWA = "INSERT INTO mahasiswa (nrp, nama, email, id_program, id_prodi, role, password) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_MAHASISWA)) {

            pstmt.setString(1, nrp);
            pstmt.setString(2, nama);
            pstmt.setString(3, email);
            pstmt.setString(4, idProgram);
            pstmt.setString(5, idProdi);
            pstmt.setString(6, selectedRole);
            pstmt.setString(7, password); // Menyimpan password dalam teks biasa

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                showAlert(AlertType.INFORMATION, "Pendaftaran Berhasil", "Akun Anda berhasil didaftarkan!");
                // Arahkan ke halaman login atau dashboard
            } else {
                showAlert(AlertType.ERROR, "Pendaftaran Gagal", "Terjadi kesalahan saat mendaftarkan akun.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("duplicate key value violates unique constraint \"mahasiswa_nrp_pkey\"")) {
                showAlert(AlertType.ERROR, "Pendaftaran Gagal", "NRP '" + nrp + "' sudah terdaftar. Mohon gunakan NRP lain.");
            } else if (e.getMessage().contains("duplicate key value violates unique constraint \"mahasiswa_email_key\"")) {
                showAlert(AlertType.ERROR, "Pendaftaran Gagal", "Email '" + email + "' sudah terdaftar. Mohon gunakan email lain.");
            } else {
                showAlert(AlertType.ERROR, "Kesalahan Database", "Terjadi kesalahan database: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleLoginLinkAction() {
        System.out.println("Navigasi ke halaman Login...");
        // Implementasi navigasi ke Login.fxml
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginHyperlink.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Kesalahan Navigasi", "Gagal memuat halaman Login.");
        }
    }

    private String getProdiIdByName(String namaProdi) {
        String id = null;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement("SELECT id_prodi FROM prodi WHERE nama_prodi = ?")) {
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
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement("SELECT id_program FROM program_mhs WHERE nama_program = ?")) {
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

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}