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
import java.sql.*;

public class ClubsController {

    @FXML
    private VBox clubsContainer;

    @FXML
    public void initialize() {
        loadClubsFromDatabase();
    }

    private void loadClubsFromDatabase() {
        String query = "SELECT * FROM club";

        Connection conn = DatabaseConnector.connect();
        if (conn == null) {
            showError("Error Database", "Gagal connect ke database. Cek settingnya.");
            return;
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int clubID = rs.getInt("id_club");
                String name = rs.getString("nama_club");
                String description = rs.getString("deskripsi");
                int year = rs.getInt("tahun_berdiri");
                String imagePath = rs.getString("image_path");

                HBox clubBox = new HBox(10);
                clubBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 10;");
                clubBox.setAlignment(Pos.CENTER_LEFT);

                clubBox.setOnMouseClicked(event -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("club-detail.fxml"));
                        Parent root = loader.load();
                        ClubDetailController controller = loader.getController();
                        controller.setClubID(clubID);
                        controller.loadClubData();

                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        Scene scene = new Scene(root);
                        stage.setScene(scene);
                        stage.show();
                    } catch (IOException e) {
                        showError("Error Navigasi", e.getMessage());
                    }
                });

                clubBox.setOnMouseEntered(e -> {
                    clubBox.setStyle("-fx-border-color: lightblue; -fx-border-width: 2; -fx-border-radius: 5; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, lightblue, 10, 0.3, 0, 0);");
                });

                clubBox.setOnMouseExited(e -> {
                    clubBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 10;");
                });

                clubBox.getChildren().add(createImageView(imagePath));

                VBox textContainer = new VBox(5);
                Label nameLabel = new Label(name);
                nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                Label descLabel = new Label(description);
                descLabel.setWrapText(true);

                Label yearLabel = new Label("Founded: " + year);

                textContainer.getChildren().addAll(nameLabel, descLabel, yearLabel);
                clubBox.getChildren().add(textContainer);

                clubsContainer.getChildren().add(clubBox);
            }

        } catch (SQLException e) {
            showError("Error Database", e.getMessage());
        }
    }

    private ImageView createImageView(String imagePath) {
        InputStream imageStream = null;

        if (imagePath != null && !imagePath.trim().isEmpty()) {
            imageStream = getClass().getResourceAsStream("/images/" + imagePath);
            if (imageStream == null) {
                System.err.println("Image not found in resources: " + imagePath);
            }
        }

        if (imageStream == null) {
            imageStream = getClass().getResourceAsStream("/images/image-not-found.png");
            if (imageStream == null) {
                showError("Error Image", "Fallback image tidak ditemukan.");
                throw new RuntimeException("Fallback image not found: image-not-found.png");
            }
        }

        ImageView imageView = new ImageView(new Image(imageStream));
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
