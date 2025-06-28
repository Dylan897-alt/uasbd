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

public class MyClubsController {
    @FXML
    private VBox clubsContainer;

    @FXML
    public void initialize() {
        loadJoinedClubsFromDatabase();
    }

    private void loadJoinedClubsFromDatabase() {
        clubsContainer.getChildren().clear();
        String loggedInNrp = UserSession.getLoggedInNrp();

        if (loggedInNrp == null || !UserSession.isLoggedIn()) {
            showError("Sesi Tidak Valid", "Tidak dapat memuat data klub karena sesi pengguna tidak ditemukan. Silakan login kembali.");
            return;
        }

        String query = "SELECT c.id_club, c.nama_club, c.deskripsi, c.tahun_berdiri, c.image_path " +
                "FROM club c " +
                "JOIN keanggotaan k ON c.id_club = k.id_club " +
                "WHERE k.nrp = ? " +
                "ORDER BY c.nama_club ASC";

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

            if (!hasClubs) {
                showNoClubsMessage();
            }

        } catch (SQLException e) {
            showError("Kesalahan Database", "Gagal memuat data klub Anda: " + e.getMessage());
        }
    }

    private HBox createClubVisualBox(int clubID, String name, String description, int year, String imagePath) {
        HBox clubBox = new HBox(20);
        clubBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 8; -fx-padding: 15; -fx-background-color: white;");
        clubBox.setAlignment(Pos.CENTER_LEFT);

        clubBox.setOnMouseClicked(event -> navigateToClubDetail(clubID, (Node) event.getSource()));
        clubBox.setOnMouseEntered(e -> clubBox.setStyle("-fx-border-color: #0078D7; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 15; -fx-background-color: #f0f8ff; -fx-effect: dropshadow(three-pass-box, rgba(0,120,215,0.3), 10, 0, 0, 0);"));
        clubBox.setOnMouseExited(e -> clubBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 8; -fx-padding: 15; -fx-background-color: white;"));

        ImageView imageView = createImageView(imagePath);
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        clubBox.getChildren().add(imageView);

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

    private void navigateToClubDetail(int clubID, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("club-detail.fxml"));
            Parent root = loader.load();

            ClubDetailController controller = loader.getController();
            controller.initializeClubData(clubID);

            Stage stage = (Stage) sourceNode.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Kesalahan Navigasi", "Gagal memuat halaman detail klub: " + e.getMessage());
        }
    }

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