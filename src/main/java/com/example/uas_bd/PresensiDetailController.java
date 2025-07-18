package com.example.uas_bd;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PresensiDetailController {
    @FXML
    private TableView<PresensiRow> tableView;
    @FXML private TableColumn<PresensiRow, String> nrpColumn, namaColumn;
    @FXML private TableColumn<PresensiRow, Boolean> hadirColumn;

    private int kegiatanId;

    @FXML
    public void initialize() {
        tableView.setEditable(true);

        nrpColumn.setCellValueFactory(cell -> cell.getValue().nrpProperty());
        namaColumn.setCellValueFactory(cell -> cell.getValue().namaProperty());

        hadirColumn.setCellValueFactory(cell -> cell.getValue().hadirProperty());
        hadirColumn.setCellFactory(CheckBoxTableCell.forTableColumn(hadirColumn));
        hadirColumn.setEditable(true);
    }

    public void setKegiatanId(int id) {
        this.kegiatanId = id;
        loadRegistrasi();
    }

    private void loadRegistrasi() {
        ObservableList<PresensiRow> rows = FXCollections.observableArrayList();
        String query = """
            SELECT r.nrp, m.nama 
            FROM registrasi r
            JOIN mahasiswa m ON r.nrp = m.nrp
            WHERE r.id_kegiatan = ?
            """;
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, kegiatanId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(new PresensiRow(rs.getString("nrp"), rs.getString("nama")));
            }
            tableView.setItems(rows);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Kesalahan Database", "Gagal memuat daftar registrasi.\n\n" + getErrorMessage(e));
        }
    }

    @FXML
    private void handleSubmit() {
        boolean savedAny = false;
        for (PresensiRow row : tableView.getItems()) {
            if (row.isHadir()) {
                savePresensi(row.getNrp());
                savedAny = true;
            }
        }

        if (savedAny) {
            new Alert(Alert.AlertType.INFORMATION, "Presensi berhasil disimpan.").showAndWait();
        } else {
            new Alert(Alert.AlertType.WARNING, "Tidak ada peserta yang dicentang hadir.").showAndWait();
        }
    }

    private void savePresensi(String nrp) {
        String getRegIdQuery = "SELECT id_registrasi FROM registrasi WHERE nrp = ? AND id_kegiatan = ?";
        String insertQuery = """
            INSERT INTO presensi (status_kehadiran, nrp, id_kegiatan, id_registrasi)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement regStmt = conn.prepareStatement(getRegIdQuery)) {

            regStmt.setString(1, nrp);
            regStmt.setInt(2, kegiatanId);
            ResultSet rs = regStmt.executeQuery();

            if (rs.next()) {
                int idRegistrasi = rs.getInt("id_registrasi");

                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, "Hadir");
                    insertStmt.setString(2, nrp);
                    insertStmt.setInt(3, kegiatanId);
                    insertStmt.setInt(4, idRegistrasi);

                    insertStmt.executeUpdate();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Kesalahan Database", "Gagal menyimpan presensi.\n\n" + getErrorMessage(e));
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Data Tidak Ditemukan", "Registrasi tidak ditemukan untuk:\nNRP: " + nrp + "\nKegiatan ID: " + kegiatanId);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Kesalahan Database", "Gagal mengambil ID registrasi.\n\n" + getErrorMessage(e));
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
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