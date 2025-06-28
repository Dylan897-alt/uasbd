package com.example.uas_bd;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Controller untuk halaman "Daftar Semua Kegiatan".
 * Menampilkan semua kegiatan yang ada dan memungkinkan pengguna (Anggota) untuk mendaftar.
 */
public class KegiatanListController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> jenisKegiatanFilterComboBox;
    @FXML
    private ComboBox<String> clubFilterComboBox;
    @FXML
    private Button resetFilterButton;
    @FXML
    private FlowPane kegiatanFlowPane;

    private ObservableList<Kegiatan> masterKegiatanList = FXCollections.observableArrayList();
    private ObservableList<Kegiatan> filteredKegiatanList = FXCollections.observableArrayList();
    private Set<Integer> registeredActivityIds = new HashSet<>();

    @FXML
    public void initialize() {
        String currentUserRole = UserSession.getLoggedInRole();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterKegiatanData());
        jenisKegiatanFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterKegiatanData());
        clubFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterKegiatanData());
        resetFilterButton.setOnAction(event -> resetFilters());

        loadFilterOptions();
        loadKegiatanData();
    }

    private void loadKegiatanData() {
        masterKegiatanList.clear();
        registeredActivityIds.clear();
        String loggedInNrp = UserSession.getLoggedInNrp();
        if (loggedInNrp == null) return;

        String registrationQuery = "SELECT id_kegiatan FROM registrasi WHERE nrp = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(registrationQuery)) {
            pstmt.setString(1, loggedInNrp);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                registeredActivityIds.add(rs.getInt("id_kegiatan"));
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat status pendaftaran Anda.\n\n" + getErrorMessage(e));
        }

        String kegiatanQuery = "SELECT kc.id_kegiatan, kc.nama_kegiatan, c.nama_club, kc.tanggal, " +
                "kc.waktu_mulai, kc.waktu_selesai, kc.lokasi, jk.nama_jenis " +
                "FROM kegiatan_club kc " +
                "JOIN club c ON kc.id_club = c.id_club " +
                "JOIN jenis_kegiatan jk ON kc.id_jenis = jk.id_jenis";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(kegiatanQuery);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                masterKegiatanList.add(new Kegiatan(
                        rs.getInt("id_kegiatan"),
                        rs.getString("nama_kegiatan"),
                        rs.getString("nama_club"),
                        rs.getObject("tanggal", LocalDate.class),
                        rs.getObject("waktu_mulai", LocalTime.class),
                        rs.getObject("waktu_selesai", LocalTime.class),
                        rs.getString("lokasi"),
                        rs.getString("nama_jenis")
                ));
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat data kegiatan.\n\n" + getErrorMessage(e));
        }

        filterKegiatanData();
    }

    private void populateKegiatanCards(ObservableList<Kegiatan> kegiatanToDisplay) {
        kegiatanFlowPane.getChildren().clear();

        if (kegiatanToDisplay.isEmpty()) {
            kegiatanFlowPane.getChildren().add(new Label("Tidak ada kegiatan yang ditemukan."));
        } else {
            String currentUserRole = UserSession.getLoggedInRole();
            for (Kegiatan kegiatan : kegiatanToDisplay) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("ActivityCard.fxml"));
                    Node cardNode = loader.load();
                    ActivityCardController cardController = loader.getController();
                    boolean isRegistered = registeredActivityIds.contains(kegiatan.getIdKegiatan());
                    cardController.setKegiatanData(kegiatan, currentUserRole, this, isRegistered);
                    kegiatanFlowPane.getChildren().add(cardNode);
                } catch (IOException e) {
                    showAlert(AlertType.ERROR, "Kesalahan UI", "Gagal memuat kartu kegiatan.\n\n" + getErrorMessage(e));
                }
            }
        }
    }

    public void handleDaftarKegiatan(Kegiatan kegiatan, ActivityCardController cardController) {
        String loggedInNrp = UserSession.getLoggedInNrp();
        if (loggedInNrp == null) return;

        Alert confirmAlert = new Alert(AlertType.CONFIRMATION, "Apakah Anda yakin ingin mendaftar ke kegiatan: " + kegiatan.getNamaKegiatan() + "?", ButtonType.OK, ButtonType.CANCEL);
        confirmAlert.setTitle("Konfirmasi Pendaftaran");
        confirmAlert.setHeaderText(null);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                registerToActivityInDb(loggedInNrp, kegiatan.getIdKegiatan(), kegiatan.getNamaKegiatan(), cardController);
            }
        });
    }

    private void registerToActivityInDb(String nrp, int idKegiatan, String namaKegiatan, ActivityCardController cardController) {
        String query = "INSERT INTO registrasi (nrp, id_kegiatan, tanggal_registrasi, status_registrasi) VALUES (?, ?, CURRENT_TIMESTAMP, ?)";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nrp);
            pstmt.setInt(2, idKegiatan);
            pstmt.setString(3, "Terdaftar");

            if (pstmt.executeUpdate() > 0) {
                showAlert(AlertType.INFORMATION, "Berhasil", "Anda berhasil terdaftar di kegiatan: " + namaKegiatan);
                cardController.updateToRegisteredState();
                registeredActivityIds.add(idKegiatan);
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                showAlert(AlertType.WARNING, "Gagal", "Anda sudah pernah terdaftar di kegiatan ini.");
                cardController.updateToRegisteredState();
            } else {
                showAlert(AlertType.ERROR, "Kesalahan Database", "Gagal mendaftar: " + getErrorMessage(e));
            }
        }
    }

    private void loadFilterOptions() {
        ObservableList<String> jenisList = FXCollections.observableArrayList();
        jenisList.add("Semua Jenis");
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nama_jenis FROM jenis_kegiatan ORDER BY nama_jenis ASC")) {
            while (rs.next()) {
                jenisList.add(rs.getString("nama_jenis"));
            }
            jenisKegiatanFilterComboBox.setItems(jenisList);
            jenisKegiatanFilterComboBox.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat filter jenis kegiatan.\n\n" + getErrorMessage(e));
        }

        ObservableList<String> clubList = FXCollections.observableArrayList();
        clubList.add("Semua Club");
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nama_club FROM club ORDER BY nama_club ASC")) {
            while (rs.next()) {
                clubList.add(rs.getString("nama_club"));
            }
            clubFilterComboBox.setItems(clubList);
            clubFilterComboBox.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat filter club.\n\n" + getErrorMessage(e));
        }
    }

    private void filterKegiatanData() {
        filteredKegiatanList.clear();
        String searchText = searchField.getText().toLowerCase();
        String selectedJenis = jenisKegiatanFilterComboBox.getSelectionModel().getSelectedItem();
        String selectedClub = clubFilterComboBox.getSelectionModel().getSelectedItem();

        for (Kegiatan kegiatan : masterKegiatanList) {
            boolean matchesSearch = searchText.isEmpty() || kegiatan.getNamaKegiatan().toLowerCase().contains(searchText);
            boolean matchesJenis = selectedJenis == null || selectedJenis.equals("Semua Jenis") || kegiatan.getJenisKegiatan().equals(selectedJenis);
            boolean matchesClub = selectedClub == null || selectedClub.equals("Semua Club") || kegiatan.getClubPenyelenggara().equals(selectedClub);

            if (matchesSearch && matchesJenis && matchesClub) {
                filteredKegiatanList.add(kegiatan);
            }
        }
        populateKegiatanCards(filteredKegiatanList);
    }

    private void resetFilters() {
        searchField.clear();
        jenisKegiatanFilterComboBox.getSelectionModel().selectFirst();
        clubFilterComboBox.getSelectionModel().selectFirst();
    }

    public void handleEditKegiatan(Kegiatan kegiatan) {
        // Implementasi navigasi ke halaman edit untuk Pengurus
        System.out.println("Mengedit: " + kegiatan.getNamaKegiatan());
    }

    public void handleHapusKegiatan(Kegiatan kegiatan) {
        // Implementasi logika hapus untuk Pengurus
        System.out.println("Menghapus: " + kegiatan.getNamaKegiatan());
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getErrorMessage(Exception e) {
        return (e.getMessage() != null && !e.getMessage().isBlank())
                ? e.getMessage()
                : "Terjadi kesalahan tak dikenal.";
    }
}