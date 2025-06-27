package com.example.uas_bd;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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

// PENTING: Import BCrypt dihilangkan karena tidak digunakan
// import org.mindrot.jbcrypt.BCrypt; // BARIS INI DIHILANGKAN

public class LoginController {

    @FXML
    private ImageView loginImageView;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordTextField;
    @FXML
    private Label loginTitleLabel;
    @FXML
    private Button signInBtn;
    @FXML
    private Label statusPassLabel;
    @FXML
    private Label statusSignInLabel;
    @FXML
    private Hyperlink signUpLink;
    @FXML
    private Label cautionLabel;
    @FXML
    private ComboBox<String> roleComboBox;
//    private static final String DB_URL = "jdbc:postgresql://localhost:5432/uas_bd";
//    private static final String DB_USER = "postgres";
//    private static final String DB_PASS = "Dylan030506";

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/HR BD A";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "POSTGRESSQL";
//    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Project_1_BasisData";
//    private static final String DB_USER = "postgres";
//    private static final String DB_PASS = "Untukkuliah123";

    @FXML
    public void initialize() {
        statusPassLabel.setVisible(false);
        statusSignInLabel.setVisible(false);

        ObservableList<String> roles = FXCollections.observableArrayList("Anggota", "Pengurus");
        roleComboBox.setItems(roles);
        roleComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleSignInButtonAction() {
        statusPassLabel.setVisible(false);
        statusSignInLabel.setVisible(false);

        String username = usernameTextField.getText();
        String password = passwordTextField.getText(); // Password dalam bentuk teks biasa
        String selectedRole = roleComboBox.getSelectionModel().getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || selectedRole == null) {
            showAlert(AlertType.WARNING, "Login Gagal", "Mohon lengkapi username, password, dan pilih role.");
            return;
        }

        // PERINGATAN SANGAT PENTING: Query ini mengambil password dalam teks biasa
        String query = "SELECT password, role FROM mahasiswa WHERE nrp = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password"); // Mengambil password dalam teks biasa
                String storedRole = rs.getString("role");

                // PERINGATAN SANGAT PENTING: Perbandingan password dalam teks biasa
                if (password.equals(storedPassword)) {
                    if (selectedRole.equals(storedRole)) {
                        // Ambil juga nama dan email jika diperlukan di sesi
                        String queryForDetails = "SELECT nama, email FROM mahasiswa WHERE nrp = ?";
                        try (PreparedStatement pstmtDetails = conn.prepareStatement(queryForDetails)) {
                            pstmtDetails.setString(1, username);
                            ResultSet rsDetails = pstmtDetails.executeQuery();
                            if (rsDetails.next()) {
                                String nama = rsDetails.getString("nama");
                                String email = rsDetails.getString("email");

                                // --- PENTING: ISI DATA SESI DI SINI ---
                                UserSession.createSession(username, nama, email, storedRole);
                                // ------------------------------------

                                showAlert(AlertType.INFORMATION, "Login Berhasil", "Selamat datang, " + nama + "!");
                                navigateToDashboard(storedRole);
                            }
                        }
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

    @FXML
    private void handleSignUpLinkAction() {
        System.out.println("Navigasi ke halaman Sign Up...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("signUp.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) signUpLink.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Kesalahan Navigasi", "Gagal memuat halaman Sign Up.");
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void navigateToDashboard(String role) {
        try {
            String fxmlPath = "";
            if ("Anggota".equals(role)) {
//                fxmlPath = "/path/to/AnggotaDashboard.fxml";
                fxmlPath = "clubs.fxml";
            } else if ("Pengurus".equals(role)) {
                fxmlPath = "/path/to/PengurusDashboard.fxml";
            }

            if (!fxmlPath.isEmpty()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                Stage stage = (Stage) signInBtn.getScene().getWindow();
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