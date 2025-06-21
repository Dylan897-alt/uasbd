package com.example.uas_bd;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// Pastikan Anda sudah menambahkan library hashing seperti jbcrypt jika menggunakan BCrypt
// import org.mindrot.jbcrypt.BCrypt;

public class LoginController {

    // --- Deklarasi Elemen FXML (sesuai fx:id di FXML) ---
    @FXML
    private ImageView loginImageView; // Jika ingin memanipulasi gambar
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordTextField;
    @FXML
    private Label loginTitleLabel; // Jika ingin memanipulasi judul
    @FXML
    private Button signInBtn;
    @FXML
    private Label statusPassLabel; // Untuk pesan error password
    @FXML
    private Label statusSignInLabel; // Untuk pesan error username
    @FXML
    private Hyperlink signUpLink;
    @FXML
    private Label cautionLabel; // Jika ingin memanipulasi label "Don't have account?"
    @FXML
    private ComboBox<String> roleComboBox;

    // --- Database Configuration (Ganti dengan detail DB Anda) ---
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Project_1_BasisData";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "Untukkuliah123";

    // --- Metode Inisialisasi ---
    @FXML
    public void initialize() {
        // Sembunyikan label status/error saat pertama kali dimuat
        statusPassLabel.setVisible(false);
        statusSignInLabel.setVisible(false);

        // Isi ComboBox untuk Role (sesuai dengan yang ada di tabel mahasiswa)
        ObservableList<String> roles = FXCollections.observableArrayList("Anggota", "Pengurus");
        roleComboBox.setItems(roles);
        roleComboBox.getSelectionModel().selectFirst(); // Pilih 'Anggota' sebagai default
    }

    // --- Metode Event Handler untuk Tombol "Sign in" ---
    @FXML
    private void handleSignInButtonAction() {
        // Sembunyikan pesan error sebelumnya
        statusPassLabel.setVisible(false);
        statusSignInLabel.setVisible(false);

        String username = usernameTextField.getText(); // Kita asumsikan username adalah NRP
        String password = passwordTextField.getText();
        String selectedRole = roleComboBox.getSelectionModel().getSelectedItem();

        // 1. Validasi Input Kosong
        if (username.isEmpty() || password.isEmpty() || selectedRole == null) {
            showAlert(AlertType.WARNING, "Login Gagal", "Mohon lengkapi username, password, dan pilih role.");
            return;
        }

        // 2. Autentikasi ke Database
        // Catatan PENTING: Untuk keamanan, password di database harus di-hash (misal BCrypt).
        // Anda perlu membandingkan hash password yang dimasukkan dengan hash di DB.
        String query = "SELECT password_hash, role FROM mahasiswa WHERE nrp = ?"; // Asumsi ada kolom password_hash
        // Jika Anda menyimpan password_hash di tabel terpisah (misal users), sesuaikan query
        // query = "SELECT u.password_hash, u.role FROM users u JOIN mahasiswa m ON u.nrp = m.nrp WHERE m.nrp = ?";


        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password_hash");
                String storedRole = rs.getString("role");

                // Verifikasi Password (menggunakan BCrypt sebagai contoh)
                // if (BCrypt.checkpw(password, storedHashedPassword)) { // Jika menggunakan BCrypt
                if (password.equals(storedHashedPassword)) { // !!! SANGAT TIDAK AMAN untuk password mentah !!!
                    // GANTI DENGAN PEMBANDING HASH!
                    if (selectedRole.equals(storedRole)) {
                        showAlert(AlertType.INFORMATION, "Login Berhasil", "Selamat datang, " + username + "!");
                        // Navigasi ke dashboard sesuai role
                        navigateToDashboard(selectedRole);
                    } else {
                        statusSignInLabel.setText("Role tidak sesuai.");
                        statusSignInLabel.setVisible(true);
                    }
                } else {
                    statusPassLabel.setText("Password salah!");
                    statusPassLabel.setVisible(true);
                }
            } else {
                statusSignInLabel.setText("Username tidak terdaftar.");
                statusSignInLabel.setVisible(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Kesalahan Database", "Terjadi kesalahan saat login: " + e.getMessage());
        }
    }

    // --- Metode Event Handler untuk Hyperlink "Sign up" ---
    @FXML
    private void handleSignUpLinkAction() {
        System.out.println("Navigasi ke halaman Sign Up...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("signUp.fxml")); // Ganti path
            Parent root = loader.load();
            Stage stage = (Stage) signUpLink.getScene().getWindow(); // Mendapatkan Stage dari elemen yang diklik
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Kesalahan Navigasi", "Gagal memuat halaman Sign Up.");
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

    // --- Metode Navigasi ke Dashboard ---
    private void navigateToDashboard(String role) {
        try {
            String fxmlPath = "";
            if ("Anggota".equals(role)) {
                fxmlPath = "/path/to/AnggotaDashboard.fxml"; // Ganti path FXML dashboard anggota
            } else if ("Pengurus".equals(role)) {
                fxmlPath = "/path/to/PengurusDashboard.fxml"; // Ganti path FXML dashboard pengurus
            }

            if (!fxmlPath.isEmpty()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                Stage stage = (Stage) signInBtn.getScene().getWindow(); // Dapatkan Stage dari button
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } else {
                showAlert(AlertType.ERROR, "Navigasi", "Tidak ada dashboard yang ditentukan untuk role ini.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Kesalahan Navigasi", "Gagal memuat dashboard: " + e.getMessage());
        }
    }
}