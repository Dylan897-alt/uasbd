package com.example.uas_bd; // Ganti dengan package Anda

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Kegiatan {
    private final SimpleIntegerProperty idKegiatan;
    private final SimpleStringProperty namaKegiatan;
    private final SimpleStringProperty clubPenyelenggara; // Nama klub
    private final SimpleObjectProperty<LocalDate> tanggal;
    private final SimpleStringProperty waktuMulai; // Format HH:mm
    private final SimpleStringProperty waktuSelesai; // Format HH:mm
    private final SimpleStringProperty lokasi;
    private final SimpleStringProperty jenisKegiatan; // Nama jenis kegiatan

    // Konstruktor
    public Kegiatan(int idKegiatan, String namaKegiatan, String clubPenyelenggara, LocalDate tanggal,
                    LocalTime waktuMulai, LocalTime waktuSelesai, String lokasi, String jenisKegiatan) {
        this.idKegiatan = new SimpleIntegerProperty(idKegiatan);
        this.namaKegiatan = new SimpleStringProperty(namaKegiatan);
        this.clubPenyelenggara = new SimpleStringProperty(clubPenyelenggara);
        this.tanggal = new SimpleObjectProperty<>(tanggal);
        this.waktuMulai = new SimpleStringProperty(waktuMulai.format(DateTimeFormatter.ofPattern("HH:mm")));
        this.waktuSelesai = new SimpleStringProperty(waktuSelesai.format(DateTimeFormatter.ofPattern("HH:mm")));
        this.lokasi = new SimpleStringProperty(lokasi);
        this.jenisKegiatan = new SimpleStringProperty(jenisKegiatan);
    }

    // Getter untuk properti (penting untuk TableView, meskipun kita pakai FlowPane, ini good practice)
    public IntegerProperty idKegiatanProperty() {
        return idKegiatan;
    }
    public StringProperty namaKegiatanProperty() {
        return namaKegiatan;
    }
    public StringProperty clubPenyelenggaraProperty() {
        return clubPenyelenggara; }
    public ObjectProperty<LocalDate> tanggalProperty() {
        return tanggal;
    }
    public StringProperty waktuMulaiProperty() {
        return waktuMulai;
    }
    public StringProperty waktuSelesaiProperty() {
        return waktuSelesai;
    }
    public StringProperty lokasiProperty() {
        return lokasi;
    }
    public StringProperty jenisKegiatanProperty() {
        return jenisKegiatan;
    }

    // Getter biasa (digunakan oleh ActivityCardController)
    public int getIdKegiatan() {
        return idKegiatan.get();
    }
    public String getNamaKegiatan() {
        return namaKegiatan.get();
    }
    public String getClubPenyelenggara() {
        return clubPenyelenggara.get();
    }
    public LocalDate getTanggal() {
        return tanggal.get();
    }
    public String getWaktuMulai() {
        return waktuMulai.get();
    }
    public String getWaktuSelesai() {
        return waktuSelesai.get();
    }
    public String getLokasi() {
        return lokasi.get();
    }
    public String getJenisKegiatan() {
        return jenisKegiatan.get();
    }
}