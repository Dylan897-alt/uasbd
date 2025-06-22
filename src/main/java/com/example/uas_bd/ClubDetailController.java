package com.example.uas_bd;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

public class ClubDetailController {
    private int clubID;
    private final String loggedInNrp = "c14240058";

    @FXML
    private ImageView clubImage;
    @FXML
    private HBox actionContainer;
    @FXML
    private VBox infoContainer;
    @FXML
    private VBox clubsContainer;

    public void setClubID(int clubID){
        this.clubID = clubID;
    }

    public void loadClubData(){
        Connection conn = DatabaseConnector.connect();
        if (conn == null) {
            showAlert("Database Error", "Failed to connect to the database. Please check your settings.");
            return;
        }

        try {
            String query = "SELECT * FROM club WHERE id_club = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, clubID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("nama_club");
                String description = rs.getString("deskripsi");
                int year = rs.getInt("tahun_berdiri");
                String imagePath = rs.getString("image_path");

                // Set image
                clubImage.setImage(loadImage(imagePath));

                // Display action button or status
                if (isUserMember(conn)) {
                    Label alreadyJoined = new Label("Sudah terdaftar");
                    alreadyJoined.setStyle("-fx-font-size: 14px; -fx-text-fill: green;");
                    actionContainer.getChildren().add(alreadyJoined);
                } else {
                    Button joinButton = new Button("Daftar");
                    joinButton.setOnAction(e -> {
                        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmation.setTitle("Konfirmasi Pendaftaran");
                        confirmation.setHeaderText(null);
                        confirmation.setContentText("Apakah kamu yakin ingin mendaftar ke klub ini?");

                        confirmation.showAndWait().ifPresent(response -> {
                            if (response.getButtonData().isDefaultButton()) {
                                registerToClub(conn, name);
                            }
                        });
                    });
                    actionContainer.getChildren().add(joinButton);
                }

                Label nameLabel = new Label("Nama Club: " + name);
                nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                Label descLabel = new Label("Deskripsi: " + description);
                descLabel.setWrapText(true);

                Label yearLabel = new Label("Tahun Berdiri: " + year);

                infoContainer.getChildren().addAll(nameLabel, descLabel, yearLabel);
            } else {
                showAlert("Not Found", "Club with ID " + clubID + " was not found.");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error retrieving club:\n" + e.getMessage());
        }
    }

    private boolean isUserMember(Connection conn) throws SQLException {
        String query = "SELECT * FROM keanggotaan WHERE nrp = ? AND id_club = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, loggedInNrp);
        stmt.setInt(2, clubID);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    private void registerToClub(Connection conn, String name) {
        try {
            String insert = "INSERT INTO keanggotaan (nrp, id_club, tanggal_gabung, status, peran) " +
                    "VALUES (?, ?, CURRENT_DATE, 'Aktif', 'anggota')";
            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setString(1, loggedInNrp);
            stmt.setInt(2, clubID);
            stmt.executeUpdate();

            actionContainer.getChildren().clear();
            Label successLabel = new Label("Sudah terdaftar");
            successLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: green;");
            actionContainer.getChildren().add(successLabel);

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Pendaftaran Sukses!");
            success.setHeaderText(null);
            success.setContentText("Berhasil mendaftar ke " + name + " sebagai anggota!");
            success.showAndWait();
        } catch (SQLException e) {
            showAlert("Registration Error", "Failed to register:\n" + e.getMessage());
        }
    }

    private Image loadImage(String imagePath) {
        InputStream imageStream = null;

        if (imagePath != null && !imagePath.trim().isEmpty()) {
            imageStream = getClass().getResourceAsStream("/images/" + imagePath);
            if (imageStream == null) {
                System.err.println("Image not found: " + imagePath);
            }
        }

        if (imageStream == null) {
            imageStream = getClass().getResourceAsStream("/images/image-not-found.png");
            if (imageStream == null) {
                showAlert("Image Error", "Fallback image is missing");
                throw new RuntimeException("Fallback image not found");
            }
        }

        return new Image(imageStream);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    public void initialize() {
        actionContainer.getChildren().clear();
        infoContainer.getChildren().clear();
    }
}
