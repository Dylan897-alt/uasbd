package com.example.uas_bd;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class ProfileController {

    @FXML
    private AnchorPane profilePane;

    @FXML
    public void initialize() {
        // Jalankan sesuatu saat tampilan profile dimuat
        System.out.println("Profile loaded.");
    }

    // Tambahkan method lainnya untuk update UI, ambil data user, dll
}
