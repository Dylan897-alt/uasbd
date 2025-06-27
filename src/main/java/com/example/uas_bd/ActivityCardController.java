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
    private Button daftarButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    private Kegiatan kegiatan;
    private KegiatanListController parentController;

    /**
     * Metode utama untuk mengisi data ke dalam kartu.
     * Menerima parameter boolean 'isRegistered' untuk menentukan tampilan awal.
     * @param kegiatan Objek Kegiatan yang berisi semua data.
     * @param userRole Role dari pengguna yang sedang login ('Anggota' atau 'Pengurus').
     * @param parentController Referensi ke controller utama yang memuat kartu ini.
     * @param isRegistered Status apakah pengguna sudah terdaftar di kegiatan ini.
     */
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

            // Logika kunci: Tentukan tampilan berdasarkan status pendaftaran
            if (isRegistered) {
                // Jika sudah terdaftar saat halaman dimuat, langsung ubah tampilannya
                updateToRegisteredState();
            } else {
                // Jika belum, tampilkan tombol "Daftar" dan hubungkan aksinya
                daftarButton.setVisible(true);
                daftarButton.setOnAction(event -> parentController.handleDaftarKegiatan(kegiatan, this));
            }

        } else if ("Pengurus".equals(userRole)) {
            // Aksi untuk Pengurus
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

    /**
     * Metode ini mengubah tampilan kartu setelah pengguna berhasil mendaftar
     * atau saat halaman dimuat dan pengguna sudah terdaftar.
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