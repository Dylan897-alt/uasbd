package com.example.uas_bd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class NavbarController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Button clubsButton;
    @FXML
    private Button myClubsButton;

    public void onClubsClick(ActionEvent event) throws IOException {
        switchScene(event, "clubs.fxml");
    }

    public void onMyClubsClick(ActionEvent event) throws IOException{
        switchScene(event, "my-clubs.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        root = loader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
