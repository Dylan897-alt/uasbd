package com.example.uas_bd;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;

/**
 * Controller untuk satu kartu kegiatan (ActivityCard.fxml).
 * Mengelola tampilan dan aksi dari satu item kegiatan di dalam daftar.
 */
public class ActivityCardController {

    // Elemen-elemen FXML yang di-inject dari ActivityCard.fxml
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
    private Button detailButton;
    @FXML
    private Button daftarButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    // Variabel untuk menyimpan data dan referensi
    private Kegiatan kegiatan;
    private KegiatanListController parentController;

    /**
     * Metode utama untuk mengisi data ke dalam kartu.
     * Dipanggil dari KegiatanListController saat kartu dibuat.
     * @param kegiatan Objek Kegiatan yang berisi semua data.
     * @param userRole Role dari pengguna yang sedang login ('Anggota' atau 'Pengurus').
     * @param parentController Referensi ke controller utama yang memuat kartu ini.
     */
    public void setKegiatanData(Kegiatan kegiatan, String userRole, KegiatanListController parentController) {
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
            // Aksi untuk Anggota
            detailButton.setVisible(true);
            daftarButton.setVisible(true);
            editButton.setVisible(false);
            deleteButton.setVisible(false);

            // Menghubungkan aksi tombol ke metode yang ada di controller utama (parent)
            detailButton.setOnAction(event -> parentController.handleLihatDetailAnggota(kegiatan));
            daftarButton.setOnAction(event -> parentController.handleDaftarKegiatan(kegiatan, this));

        } else if ("Pengurus".equals(userRole)) {
            // Aksi untuk Pengurus
            detailButton.setVisible(false);
            daftarButton.setVisible(false);
            editButton.setVisible(true);
            deleteButton.setVisible(true);

            // Menghubungkan aksi tombol ke metode yang ada di controller utama (parent)
            editButton.setOnAction(event -> parentController.handleEditKegiatan(kegiatan));
            deleteButton.setOnAction(event -> parentController.handleHapusKegiatan(kegiatan));
        } else {
            // Jika role tidak dikenali, sembunyikan semua tombol
            actionButtonContainer.setVisible(false);
        }
    }

    /**
     * Metode ini mengubah tampilan kartu setelah pengguna berhasil mendaftar.
     * Ia akan mengganti tombol-tombol dengan label "Sudah Terdaftar".
     * Metode ini dipanggil dari KegiatanListController.
     */
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