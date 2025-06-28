package com.example.uas_bd;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;

public class NavbarPengurusController {

    public void onDashboardClick(ActionEvent event) {
        navigate(event, "pengurusDashboard.fxml");
    }

    public void onEditClubsClick(ActionEvent event) {
        navigate(event, "edit-club.fxml");
    }

    public void onKegiatanClick(ActionEvent event) {
        navigate(event, "kegiatan-baru.fxml");
    }

    public void onManageClubClick(ActionEvent event) {
        navigate(event, "manage-presensi.fxml");
    }

    public void onLogoutClick(ActionEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Konfirmasi Logout");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Apakah kamu ingin log out?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                navigate(event, "login.fxml");
            }
        });
    }

    private void navigate(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
