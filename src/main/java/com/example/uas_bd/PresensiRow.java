package com.example.uas_bd;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PresensiRow {
    private final StringProperty nrp;
    private final StringProperty nama;
    private final BooleanProperty hadir;

    public PresensiRow(String nrp, String nama) {
        this.nrp = new SimpleStringProperty(nrp);
        this.nama = new SimpleStringProperty(nama);
        this.hadir = new SimpleBooleanProperty(false);
    }

    public String getNrp() { return nrp.get(); }
    public StringProperty nrpProperty() { return nrp; }

    public String getNama() { return nama.get(); }
    public StringProperty namaProperty() { return nama; }

    public boolean isHadir() { return hadir.get(); }
    public BooleanProperty hadirProperty() { return hadir; }
}
