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
import java.util.ArrayList;

public class MyClubsController {

    @FXML
    private VBox clubsContainer;

    @FXML
    public void initialize() {
        loadClubsFromDatabase();
    }

    private void loadClubsFromDatabase() {
        String nrp = "c14240058";

        try (Connection conn = DatabaseConnector.connect()) {
            String query = "SELECT id_club FROM keanggotaan WHERE nrp = ?";
            PreparedStatement ps1 = conn.prepareStatement(query);
            ps1.setString(1, nrp);
            ResultSet rs1 = ps1.executeQuery();

            ArrayList<Integer> clubIDs = new ArrayList<>();
            while (rs1.next()) {
                clubIDs.add(rs1.getInt("id_club"));
            }

            if (clubIDs.isEmpty()) {
                showNoClubsMessage();
                return;
            }

            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < clubIDs.size(); i++) {
                placeholders.append("?");
                if (i < clubIDs.size() - 1) {
                    placeholders.append(",");
                }
            }

            String query2 = "SELECT * FROM club WHERE id_club IN (" + placeholders + ")";
            PreparedStatement ps2 = conn.prepareStatement(query2);

            for (int i = 0; i < clubIDs.size(); i++) {
                ps2.setInt(i + 1, clubIDs.get(i));
            }

            ResultSet rs2 = ps2.executeQuery();

            boolean hasClubs = false;

            while (rs2.next()) {
                hasClubs = true;

                String name = rs2.getString("nama_club");
                String description = rs2.getString("deskripsi");
                int year = rs2.getInt("tahun_berdiri");
                String imagePath = rs2.getString("image_path");

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

            if (!hasClubs) {
                showNoClubsMessage();
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

    private void showNoClubsMessage() {
        VBox card = new VBox(5);
        card.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 10;");

        Label messageLabel = new Label("You haven't joined a club.");
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
        messageLabel.setWrapText(true);

        card.getChildren().add(messageLabel);
        clubsContainer.getChildren().add(card);
    }
}
