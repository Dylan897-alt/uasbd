module com.example.uas_bd {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens com.example.uas_bd to javafx.fxml;
    exports com.example.uas_bd;
}