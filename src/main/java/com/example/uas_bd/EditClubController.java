package com.example.uas_bd;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.*;

public class EditClubController {

    @FXML
    private VBox clubListContainer;
    @FXML
    private VBox mainContainer;
    @FXML
    private TextField nameField, yearField, imageField;
    @FXML
    private TextArea descField;
    @FXML
    private ComboBox<CategoryItem> categoryComboBox;

    private String nrp;
    private int editingClubId = -1;

    public static class CategoryItem{
        int id;
        String name;

        CategoryItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public int getId() {
            return id;
        }
    }

    @FXML
    public void initialize() {
        this.nrp = "c14240058";
//        this.nrp = UserSession.getLoggedInNrp();
        loadCategories();
        loadClubs();
    }

    private void loadClubs() {
        clubListContainer.getChildren().clear();
        try (Connection conn = DatabaseConnector.connect()) {
            String sql = """
    SELECT c.*
    FROM club c
    JOIN keanggotaan k ON c.id_club = k.id_club
    WHERE k.nrp = ? AND k.peran = 'pengurus'
""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nrp);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id_club");
                String name = rs.getString("nama_club");
                String desc = rs.getString("deskripsi");
                int year = rs.getInt("tahun_berdiri");
                int idKategori = rs.getInt("id_kategori");
                String image = rs.getString("image_path");

                Label info = new Label(name + " (" + year + ")");
                Button editBtn = new Button("Edit");
                Button deleteBtn = new Button("Delete");

                editBtn.setOnAction(event -> populateForm(id, name, desc, year, image, idKategori));
                deleteBtn.setOnAction(event -> {
                    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmation.setTitle("Konfirmasi Hapus");
                    confirmation.setHeaderText(null);
                    confirmation.setContentText("Apakah kamu yakin ingin menghapus " + name + "?");
                    confirmation.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            deleteClub(id);
                        }
                    });
                });

                HBox row = new HBox(10, info, editBtn, deleteBtn);
                row.setStyle("-fx-border-color: lightgray; -fx-padding: 5;");
                clubListContainer.getChildren().add(row);
            }
        } catch (SQLException e) {
            showError("Error Database", e.getMessage());
        }
    }

    private void populateForm(int id, String name, String desc, int year, String image, int idKategori) {
        editingClubId = id;
        nameField.setText(name);
        descField.setText(desc);
        yearField.setText(String.valueOf(year));
        imageField.setText(image);
        categoryComboBox.setValue(findCategoryItemById(idKategori));
    }

    @FXML
    private void handleCreate() {
        String name = nameField.getText();
        String desc = descField.getText();
        String yearText = yearField.getText();
        String image = imageField.getText();
        CategoryItem selectedCategory = categoryComboBox.getValue();

        if (name.isBlank()) {
            showError("Error Validasi", "Nama klub tidak boleh kosong.");
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException e) {
            showError("Error Validasi", "Tahun harus diisi angka.");
            return;
        }

        if (selectedCategory == null) {
            showError("Error Validasi", "Kamu belum memilih kategori.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Konfirmasi Buat");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Apakah kamu yakin membuat klub ini?");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConnector.connect()) {
                    // Insert the club and return the id_club
                    String clubSql = """
                    INSERT INTO club (nama_club, deskripsi, tahun_berdiri, image_path, id_kategori)
                    VALUES (?, ?, ?, ?, ?)
                    RETURNING id_club
                """;
                    PreparedStatement stmt = conn.prepareStatement(clubSql);
                    stmt.setString(1, name);
                    stmt.setString(2, desc);
                    stmt.setInt(3, year);
                    stmt.setString(4, image);
                    stmt.setInt(5, selectedCategory.getId());

                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        int clubId = rs.getInt("id_club");

                        String anggotaSql = """
                        INSERT INTO keanggotaan (nrp, id_club, tanggal_gabung, status, peran)
                        VALUES (?, ?, CURRENT_DATE, 'Aktif', 'pengurus')
                    """;
                        PreparedStatement anggotaStmt = conn.prepareStatement(anggotaSql);
                        anggotaStmt.setString(1, nrp);
                        anggotaStmt.setInt(2, clubId);
                        anggotaStmt.executeUpdate();
                    }

                    clearForm();
                    showInfo("Sukses!", "Berhasil membuat " + name);
                    loadClubs();
                } catch (SQLException e) {
                    showError("Error Insert", e.getMessage());
                }
            }
        });
    }


    @FXML
    private void handleUpdate() {
        if (editingClubId == -1){
            showError("Error Validasi", "Kamu belum memilih klub.");
            return;
        }

        CategoryItem selectedCategory = categoryComboBox.getValue();

        if(nameField.getText().isBlank()){
            showError("Error Validasi", "Nama klub tidak boleh kosong.");
            return;
        }

        String yearText = yearField.getText();
        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException e) {
            showError("Error Validasi", "Tahun harus diisi angka.");
            return;
        }

        if (selectedCategory == null) {
            showError("Error Validasi", "Kamu belum memilih kategori.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Konfirmasi Update");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Apakah kamu yakin update klub ini?");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConnector.connect()) {
                    PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE club SET nama_club=?, deskripsi=?, tahun_berdiri=?, image_path=?, id_kategori=? WHERE id_club=?"
                    );
                    stmt.setString(1, nameField.getText());
                    stmt.setString(2, descField.getText());
                    stmt.setInt(3, Integer.parseInt(yearField.getText()));
                    stmt.setString(4, imageField.getText());
                    stmt.setInt(5, selectedCategory.getId());
                    stmt.setInt(6, editingClubId);
                    stmt.executeUpdate();
                    clearForm();
                    showInfo("Sukses!", "Berhasil update!");
                    loadClubs();
                } catch (SQLException e) {
                    showError("Error Update", e.getMessage());
                }
            }
        });
    }

    private void deleteClub(int id) {
        try (Connection conn = DatabaseConnector.connect()){
            PreparedStatement stmt1 = conn.prepareStatement("SELECT nama_club FROM club WHERE id_club = ?");
            stmt1.setInt(1, id);
            ResultSet rs = stmt1.executeQuery();
            if(rs.next()){
                String name = rs.getString("nama_club");
                PreparedStatement stmt2 = conn.prepareStatement("DELETE FROM club WHERE id_club = ?");
                stmt2.setInt(1, id);
                stmt2.executeUpdate();
                showInfo("Sukses!", "Berhasil menghapus " + name);
                loadClubs();
            } else{
                showError("Klub Tidak Ditemukan", "Club ID " + id + " tidak ditemukan.");
            }
        } catch (SQLException e) {
            showError("Error Delete", e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        nameField.clear();
        yearField.clear();
        descField.clear();
        imageField.clear();
        editingClubId = -1;
    }

    private void loadCategories() {
        try (Connection conn = DatabaseConnector.connect()) {
            String sql = "SELECT id_kategori, nama_kategori FROM kategori";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_kategori");
                String name = rs.getString("nama_kategori");
                categoryComboBox.getItems().add(new CategoryItem(id, name));
            }
        } catch (SQLException e) {
            showError("Error Loading Kategori", e.getMessage());
        }
    }

    private CategoryItem findCategoryItemById(int id) {
        for (CategoryItem item : categoryComboBox.getItems()) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String title, String msg){
        Alert information = new Alert(Alert.AlertType.INFORMATION);
        information.setTitle(title);
        information.setHeaderText(null);
        information.setContentText(msg);
        information.showAndWait();
    }
}

