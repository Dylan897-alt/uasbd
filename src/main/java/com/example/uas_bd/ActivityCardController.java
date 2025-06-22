package com.example.uas_bd; // Ganti dengan package Anda

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.time.format.DateTimeFormatter;

public class ActivityCardController {

    @FXML
    private Label namaKegiatanLabel;
    @FXML
    private Label clubPenyelenggaraLabel;
    @FXML
    private Label tanggalLabel;
    @FXML
    private Label waktuLabel; // Gabungan waktu mulai dan selesai
    @FXML
    private Label lokasiLabel;
    @FXML
    private Label jenisKegiatanLabel;
    @FXML
    private Button detailButton;
    @FXML
    private Button daftarButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    private Kegiatan kegiatan; // Objek Kegiatan yang akan ditampilkan di kartu ini
    private KegiatanListController parentController; // Referensi ke controller halaman utama

    // Metode untuk mengisi data kartu
    public void setKegiatanData(Kegiatan kegiatan, String userRole, KegiatanListController parentController) {
        this.kegiatan = kegiatan;
        this.parentController = parentController;

        namaKegiatanLabel.setText(kegiatan.getNamaKegiatan());
        clubPenyelenggaraLabel.setText("Oleh: " + kegiatan.getClubPenyelenggara());
        // Format tanggal agar lebih mudah dibaca
        tanggalLabel.setText(kegiatan.getTanggal().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        waktuLabel.setText(kegiatan.getWaktuMulai() + " - " + kegiatan.getWaktuSelesai());
        lokasiLabel.setText(kegiatan.getLokasi());
        jenisKegiatanLabel.setText(kegiatan.getJenisKegiatan());

        // Atur visibilitas tombol berdasarkan role
        if ("Anggota".equals(userRole)) {
            detailButton.setVisible(true);
            daftarButton.setVisible(true);
            editButton.setVisible(false);
            deleteButton.setVisible(false);
            // Hubungkan aksi tombol ke metode di parentController
            detailButton.setOnAction(event -> parentController.handleLihatDetailAnggota(kegiatan));
            daftarButton.setOnAction(event -> parentController.handleDaftarKegiatan(kegiatan));
        } else if ("Pengurus".equals(userRole)) {
            detailButton.setVisible(false); // Pengurus mungkin langsung edit
            daftarButton.setVisible(false);
            editButton.setVisible(true);
            deleteButton.setVisible(true);
            // Hubungkan aksi tombol ke metode di parentController
            editButton.setOnAction(event -> parentController.handleEditKegiatan(kegiatan));
            deleteButton.setOnAction(event -> parentController.handleHapusKegiatan(kegiatan));
        } else {
            // Role tidak dikenal atau tidak ada aksi
            detailButton.setVisible(false);
            daftarButton.setVisible(false);
            editButton.setVisible(false);
            deleteButton.setVisible(false);
        }
    }
}