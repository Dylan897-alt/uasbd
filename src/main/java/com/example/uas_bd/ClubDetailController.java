package com.example.uas_bd;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class ClubDetailController {

    @FXML
    private ImageView clubImageView;
    @FXML
    private Label clubNameLabel;
    @FXML
    private Label clubDescriptionLabel;
    @FXML
    private Label clubYearLabel;
    @FXML
    private VBox actionContainer; // Container untuk tombol "Daftar" atau status "Sudah Terdaftar"

    private int clubId;
    private String clubName; // Simpan nama klub untuk digunakan di notifikasi

    public void initializeClubData(int clubId) {
        this.clubId = clubId;
        loadClubDetails();
    }

    private void loadClubDetails() {
        String query = "SELECT nama_club, deskripsi, tahun_berdiri, image_path FROM club WHERE id_club = ?";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (conn == null) {
                showError("Kesalahan Database", "Gagal terhubung ke database.");
                return;
            }

            pstmt.setInt(1, this.clubId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                this.clubName = rs.getString("nama_club");
                String description = rs.getString("deskripsi");
                int year = rs.getInt("tahun_berdiri");
                String imagePath = rs.getString("image_path");

                clubNameLabel.setText(this.clubName);
                clubDescriptionLabel.setText(description);
                clubYearLabel.setText("Berdiri sejak: " + year);
                clubImageView.setImage(loadImage(imagePath));

                checkMembershipStatus();
            } else {
                showError("Klub Tidak Ditemukan", "Klub dengan ID " + this.clubId + " tidak ditemukan.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Kesalahan Database", "Gagal memuat detail klub: " + e.getMessage());
        }
    }

    private void checkMembershipStatus() {
        String loggedInNrp = UserSession.getLoggedInNrp();
        if (loggedInNrp == null || !UserSession.isLoggedIn()) {
            showError("Sesi Tidak Valid", "Tidak dapat memverifikasi pengguna. Silakan login kembali.");
            return;
        }

        String query = "SELECT 1 FROM keanggotaan WHERE nrp = ? AND id_club = ?";
        boolean isMember = false;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, loggedInNrp);
            pstmt.setInt(2, this.clubId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    isMember = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Kesalahan Database", "Gagal memeriksa status keanggotaan: " + e.getMessage());
            return; // Keluar jika terjadi error
        }

        updateActionUI(isMember);
    }

    private void updateActionUI(boolean isMember) {
        actionContainer.getChildren().clear();
        if (isMember) {
            Label alreadyJoinedLabel = new Label("Anda sudah terdaftar");
            alreadyJoinedLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: green;");
            actionContainer.getChildren().add(alreadyJoinedLabel);
        } else {
            Button joinButton = new Button("Gabung Klub Ini");
            joinButton.setStyle("-fx-font-size: 14px; -fx-background-color: #0078D7; -fx-text-fill: white; -fx-font-weight: bold;");
            joinButton.setOnAction(event -> handleJoinClubAction());
            actionContainer.getChildren().add(joinButton);
        }
    }

    private void handleJoinClubAction() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Konfirmasi Pendaftaran");
        confirmation.setHeaderText("Mendaftar ke " + this.clubName);
        confirmation.setContentText("Apakah Anda yakin ingin bergabung dengan klub ini?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            registerUserToClub();
        }
    }

    private void registerUserToClub() {
        String loggedInNrp = UserSession.getLoggedInNrp();
        if (loggedInNrp == null) {
            showError("Error", "Sesi pengguna tidak ditemukan.");
            return;
        }

        String query = "INSERT INTO keanggotaan (nrp, id_club, peran, tanggal_gabung, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, loggedInNrp);
            pstmt.setInt(2, this.clubId);
            pstmt.setString(3, "Anggota"); // Peran default
            pstmt.setDate(4, java.sql.Date.valueOf(LocalDate.now())); // Tanggal hari ini
            pstmt.setString(5, "Aktif"); // Status default

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Pendaftaran Berhasil!", "Selamat! Anda telah berhasil terdaftar di " + this.clubName + ".");
                // Perbarui UI untuk menampilkan status "Sudah terdaftar"
                updateActionUI(true);
            }

        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // Handle unique constraint violation
                showError("Pendaftaran Gagal", "Anda sudah terdaftar di klub ini.");
                updateActionUI(true); // Perbarui UI juga jika ternyata sudah terdaftar
            } else {
                e.printStackTrace();
                showError("Kesalahan Registrasi", "Terjadi kesalahan database: " + e.getMessage());
            }
        }
    }

    private Image loadImage(String imagePath) {
        InputStream imageStream = null;
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            imageStream = getClass().getResourceAsStream("/images/" + imagePath);
        }
        if (imageStream == null) {
            imageStream = getClass().getResourceAsStream("/images/image-not-found.png");
            if (imageStream == null) {
                throw new RuntimeException("Krisis! Gambar fallback 'image-not-found.png' tidak ditemukan.");
            }
        }
        return new Image(imageStream);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }
}