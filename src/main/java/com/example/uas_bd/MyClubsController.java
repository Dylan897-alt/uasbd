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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
        Connection conn = DatabaseConnector.connect();
        if (conn == null) {
            showAlert("Database Error", "Failed to connect to the database. Please check your settings.");
            return;
        }
        try {
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
                int clubID = rs2.getInt("id_club");
                String name = rs2.getString("nama_club");
                String description = rs2.getString("deskripsi");
                int year = rs2.getInt("tahun_berdiri");
                String imagePath = rs2.getString("image_path");

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
                        e.printStackTrace();
                        showAlert("Navigation Error", "Failed to load club detail page.");
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

            if (!hasClubs) {
                showNoClubsMessage();
            }

        } catch (SQLException e) {
            System.out.println("Failed to load clubs: " + e.getMessage());
            showAlert("Database Error", "Could not load clubs from the database:\n" + e.getMessage());
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
                showAlert("Image Error", "Fallback image is missing");

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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
