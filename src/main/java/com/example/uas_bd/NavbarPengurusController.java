package com.example.uas_bd;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavbarPengurusController {

    public void onDashboardClick(ActionEvent event) {
        navigate(event, "pengurus-dashboard.fxml");
    }

    public void onEditClubsClick(ActionEvent event) {
        navigate(event, "edit-club.fxml");
    }

    public void onKegiatanClick(ActionEvent event) {
        navigate(event, "kegiatan-baru.fxml");
    }

    public void onLogoutClick(ActionEvent event) {
        UserSession.clearSession(); // hapus sesi login
        navigate(event, "login.fxml");
    }

    private void navigate(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
