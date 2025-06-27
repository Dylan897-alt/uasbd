package com.example.uas_bd;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controller untuk halaman "Klub Saya".
 * Menampilkan daftar semua klub di mana pengguna yang sedang login terdaftar sebagai anggota.
 */
public class MyClubsController {

    @FXML
    private VBox clubsContainer;

    @FXML
    public void initialize() {
        loadJoinedClubsFromDatabase();
    }

    /**
     * Memuat klub yang diikuti oleh pengguna yang sedang login dari database.
     * Menggunakan satu query JOIN yang efisien.
     */
    private void loadJoinedClubsFromDatabase() {
        clubsContainer.getChildren().clear(); // Bersihkan container setiap kali memuat

        // --- PERBAIKAN 1: Menggunakan UserSession untuk mendapatkan NRP dinamis ---
        String loggedInNrp = UserSession.getLoggedInNrp();

        // Validasi: Pastikan pengguna sudah login sebelum melanjutkan
        if (loggedInNrp == null || !UserSession.isLoggedIn()) {
            showError("Sesi Tidak Valid", "Tidak dapat memuat data klub karena sesi pengguna tidak ditemukan. Silakan login kembali.");
            return;
        }

        // --- PERBAIKAN 2: Query database yang lebih efisien ---
        // Menggunakan satu query JOIN untuk mendapatkan semua data klub yang relevan sekaligus.
        // Ini lebih baik daripada 2 query terpisah.
        String query = "SELECT c.id_club, c.nama_club, c.deskripsi, c.tahun_berdiri, c.image_path " +
                "FROM club c " +
                "JOIN keanggotaan k ON c.id_club = k.id_club " +
                "WHERE k.nrp = ? " +
                "ORDER BY c.nama_club ASC";

        // --- PERBAIKAN 3: Manajemen koneksi yang aman dengan try-with-resources ---
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (conn == null) {
                showError("Kesalahan Database", "Gagal terhubung ke database.");
                return;
            }

            pstmt.setString(1, loggedInNrp);
            ResultSet rs = pstmt.executeQuery();

            boolean hasClubs = false;
            while (rs.next()) {
                hasClubs = true;
                int clubID = rs.getInt("id_club");
                String name = rs.getString("nama_club");
                String description = rs.getString("deskripsi");
                int year = rs.getInt("tahun_berdiri");
                String imagePath = rs.getString("image_path");

                HBox clubBox = createClubVisualBox(clubID, name, description, year, imagePath);
                clubsContainer.getChildren().add(clubBox);
            }

            // Jika setelah loop selesai tidak ada klub yang ditemukan, tampilkan pesan.
            if (!hasClubs) {
                showNoClubsMessage();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Kesalahan Database", "Gagal memuat data klub Anda: " + e.getMessage());
        }
    }

    /**
     * Membuat satu HBox visual yang berisi informasi lengkap untuk satu klub.
     * Direfaktor dari ClubsController untuk konsistensi.
     */
    private HBox createClubVisualBox(int clubID, String name, String description, int year, String imagePath) {
        HBox clubBox = new HBox(20); // same spacing as ClubsController
        clubBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 8; -fx-padding: 15; -fx-background-color: white;");
        clubBox.setAlignment(Pos.CENTER_LEFT);

        // Navigation + hover effects
        clubBox.setOnMouseClicked(event -> navigateToClubDetail(clubID, (Node) event.getSource()));
        clubBox.setOnMouseEntered(e -> clubBox.setStyle("-fx-border-color: #0078D7; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 15; -fx-background-color: #f0f8ff; -fx-effect: dropshadow(three-pass-box, rgba(0,120,215,0.3), 10, 0, 0, 0);"));
        clubBox.setOnMouseExited(e -> clubBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 8; -fx-padding: 15; -fx-background-color: white;"));

        // Image
        ImageView imageView = createImageView(imagePath);
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        clubBox.getChildren().add(imageView);

        // Text
        VBox textContainer = new VBox(5);
        textContainer.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 14px;");

        Label yearLabel = new Label("Berdiri sejak: " + year);
        yearLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        textContainer.getChildren().addAll(nameLabel, descLabel, yearLabel);
        clubBox.getChildren().add(textContainer);

        return clubBox;
    }


    /**
     * Logika untuk berpindah dari halaman ini ke halaman detail klub.
     * Memastikan memanggil metode yang benar di ClubDetailController.
     */
    private void navigateToClubDetail(int clubID, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("club-detail.fxml"));
            Parent root = loader.load();

            ClubDetailController controller = loader.getController();
            controller.initializeClubData(clubID); // Memanggil metode yang sudah disempurnakan

            Stage stage = (Stage) sourceNode.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Kesalahan Navigasi", "Gagal memuat halaman detail klub: " + e.getMessage());
        }
    }

    /**
     * Menampilkan pesan bahwa pengguna belum bergabung dengan klub apa pun.
     */
    private void showNoClubsMessage() {
        Label messageLabel = new Label("Anda belum bergabung dengan klub mana pun.");
        messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555;");
        clubsContainer.getChildren().add(messageLabel);
        clubsContainer.setAlignment(Pos.CENTER);
    }

    private ImageView createImageView(String imagePath) {
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
        return new ImageView(new Image(imageStream));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}