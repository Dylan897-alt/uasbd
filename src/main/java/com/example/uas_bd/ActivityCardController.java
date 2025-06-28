package com.example.uas_bd;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;

public class ActivityCardController {
    @FXML
    private Label namaKegiatanLabel;
    @FXML
    private Label clubPenyelenggaraLabel;
    @FXML
    private Label tanggalLabel;
    @FXML
    private Label waktuLabel;
    @FXML
    private Label lokasiLabel;
    @FXML
    private Label jenisKegiatanLabel;
    @FXML
    private HBox actionButtonContainer; // Container untuk tombol-tombol
    @FXML
    private Button daftarButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    private Kegiatan kegiatan;
    private KegiatanListController parentController;

    public void setKegiatanData(Kegiatan kegiatan, String userRole, KegiatanListController parentController, boolean isRegistered) {
        this.kegiatan = kegiatan;
        this.parentController = parentController;

        // Mengisi data text ke label-label
        namaKegiatanLabel.setText(kegiatan.getNamaKegiatan());
        clubPenyelenggaraLabel.setText("Oleh: " + kegiatan.getClubPenyelenggara());
        tanggalLabel.setText(kegiatan.getTanggal().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        waktuLabel.setText(kegiatan.getWaktuMulai() + " - " + kegiatan.getWaktuSelesai());
        lokasiLabel.setText(kegiatan.getLokasi());
        jenisKegiatanLabel.setText(kegiatan.getJenisKegiatan());

        // Logika untuk menampilkan tombol yang sesuai berdasarkan peran pengguna
        if ("Anggota".equals(userRole)) {
            // Sembunyikan tombol-tombol yang tidak relevan untuk Anggota
            editButton.setVisible(false);
            deleteButton.setVisible(false);

            if (isRegistered) {
                updateToRegisteredState();
            } else {
                daftarButton.setVisible(true);
                daftarButton.setOnAction(event -> parentController.handleDaftarKegiatan(kegiatan, this));
            }

        } else if ("Pengurus".equals(userRole)) {
            daftarButton.setVisible(false);
            editButton.setVisible(true);
            deleteButton.setVisible(true);

            editButton.setOnAction(event -> parentController.handleEditKegiatan(kegiatan));
            deleteButton.setOnAction(event -> parentController.handleHapusKegiatan(kegiatan));
        } else {
            // Jika role tidak dikenali, sembunyikan semua tombol
            actionButtonContainer.setVisible(false);
        }
    }

    public void updateToRegisteredState() {
        // Hapus semua tombol yang ada di dalam container
        actionButtonContainer.getChildren().clear();

        // Buat label baru untuk status "Sudah Terdaftar"
        Label registeredLabel = new Label("✔ Sudah Terdaftar");
        registeredLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: green;");

        // Masukkan label baru ke dalam container
        actionButtonContainer.getChildren().add(registeredLabel);
    }
}