package com.example.uas_bd;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String name = rs.getString("nama_club");
                String description = rs.getString("deskripsi");
                int year = rs.getInt("tahun_berdiri");
                String imagePath = rs.getString("image_path");

                HBox clubBox = new HBox(10);
                clubBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 10;");
                clubBox.setAlignment(Pos.CENTER_LEFT);

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
            System.err.println("Failed to load clubs: " + e.getMessage());
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
                throw new RuntimeException("Fallback image not found: image-not-found.png");
            }
        }

        ImageView imageView = new ImageView(new Image(imageStream));
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        return imageView;
    }
}
