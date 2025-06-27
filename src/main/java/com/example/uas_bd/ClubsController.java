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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Controller untuk halaman yang menampilkan daftar semua klub yang tersedia.
 * Halaman ini berfungsi sebagai "galeri" atau "katalog" klub.
 */
public class ClubsController {

    @FXML
    private VBox clubsContainer;

    @FXML
    public void initialize() {
        loadClubsFromDatabase();
    }

    /**
     * Memuat data klub dari database dan secara dinamis membuat elemen UI untuk setiap klub.
     */
    private void loadClubsFromDatabase() {
        String query = "SELECT id_club, nama_club, deskripsi, tahun_berdiri, image_path FROM club ORDER BY nama_club ASC";

        // --- PENYEMPURNAAN KUNCI ---
        // Menggunakan try-with-resources untuk Connection, Statement, dan ResultSet.
        // Ini memastikan semua sumber daya database (conn, stmt, rs) secara otomatis
        // ditutup setelah blok try selesai, mencegah kebocoran sumber daya (resource leak).
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Memeriksa apakah koneksi berhasil didapatkan
            if (conn == null) {
                showError("Kesalahan Database", "Gagal terhubung ke database. Periksa pengaturan koneksi Anda.");
                return;
            }

            // Loop melalui setiap baris hasil dari database
            while (rs.next()) {
                // Mengambil data dari setiap kolom
                int clubID = rs.getInt("id_club");
                String name = rs.getString("nama_club");
                String description = rs.getString("deskripsi");
                int year = rs.getInt("tahun_berdiri");
                String imagePath = rs.getString("image_path"); // Diasumsikan ada kolom image_path di tabel club

                // Membuat satu baris visual untuk klub
                HBox clubBox = createClubVisualBox(clubID, name, description, year, imagePath);

                // Menambahkan baris klub yang sudah jadi ke dalam container utama
                clubsContainer.getChildren().add(clubBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Kesalahan Database", "Terjadi kesalahan saat memuat data klub: " + e.getMessage());
        }
    }

    /**
     * Membuat satu HBox visual yang berisi informasi lengkap untuk satu klub.
     * @return HBox yang siap ditampilkan.
     */
    private HBox createClubVisualBox(int clubID, String name, String description, int year, String imagePath) {
        HBox clubBox = new HBox(20); // Memberi jarak 20px antar elemen
        clubBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 8; -fx-padding: 15; -fx-background-color: white;");
        clubBox.setAlignment(Pos.CENTER_LEFT);

        // Event handler saat sebuah klub di-klik: navigasi ke halaman detail
        clubBox.setOnMouseClicked(event -> navigateToClubDetail(clubID, (Node) event.getSource()));

        // Efek visual saat mouse hover untuk memberikan feedback ke pengguna
        clubBox.setOnMouseEntered(e -> clubBox.setStyle("-fx-border-color: #0078D7; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 15; -fx-background-color: #f0f8ff; -fx-effect: dropshadow(three-pass-box, rgba(0,120,215,0.3), 10, 0, 0, 0);"));
        clubBox.setOnMouseExited(e -> clubBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 8; -fx-padding: 15; -fx-background-color: white;"));

        // Membuat dan menambahkan gambar/logo klub
        clubBox.getChildren().add(createImageView(imagePath));

        // Membuat container untuk teks (nama, deskripsi, tahun)
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
     */
    private void navigateToClubDetail(int clubID, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ClubDetail.fxml")); // Pastikan nama file FXML benar
            Parent root = loader.load();

            // Mengambil controller dari halaman detail untuk mengirimkan ID klub
            ClubDetailController controller = loader.getController();
            controller.initializeClubData(clubID); // Metode baru untuk mengirim ID

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
     * Membuat ImageView dari path gambar. Sangat tangguh karena memiliki gambar fallback.
     * @param imagePath Path relatif gambar dari folder /resources/images/
     * @return ImageView yang siap digunakan.
     */
    private ImageView createImageView(String imagePath) {
        InputStream imageStream = null;

        // Coba muat gambar spesifik klub
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            imageStream = getClass().getResourceAsStream("/images/" + imagePath);
        }

        // Jika gambar klub tidak ada, gunakan gambar fallback
        if (imageStream == null) {
            imageStream = getClass().getResourceAsStream("/images/image-not-found.png");
            if (imageStream == null) {
                // Jika gambar fallback pun tidak ada, ini adalah error kritis
                throw new RuntimeException("Krisis! Gambar fallback 'image-not-found.png' tidak ditemukan di resources/images.");
            }
        }

        ImageView imageView = new ImageView(new Image(imageStream));
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    /**
     * Helper method untuk menampilkan pesan error.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
