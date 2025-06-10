module com.example.uas_bd {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.uas_bd to javafx.fxml;
    exports com.example.uas_bd;
}