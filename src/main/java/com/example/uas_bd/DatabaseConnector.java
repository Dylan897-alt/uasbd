package com.example.uas_bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String URL = "jdbc:postgresql://localhost:5432/uas_bd";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Dylan030506";
    //

//    private static final String URL = "jdbc:postgresql://localhost:5432/Project_1_BasisData";
//    private static final String USER = "postgres";
//    private static final String PASSWORD = "Untukkuliah123";

    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return null;
        }
    }
}
