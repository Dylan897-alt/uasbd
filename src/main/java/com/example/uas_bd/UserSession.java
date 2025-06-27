package com.example.uas_bd;

public class UserSession {
    private static String loggedInNrp;
    private static String loggedInNama;
    private static String loggedInEmail;
    private static String loggedInRole; // Anggota atau Pengurus
    // Anda bisa tambahkan data lain yang mungkin dibutuhkan, seperti id_prodi, id_program, dll.

    public static void createSession(String nrp, String nama, String email, String role) {
        UserSession.loggedInNrp = nrp;
        UserSession.loggedInNama = nama;
        UserSession.loggedInEmail = email;
        UserSession.loggedInRole = role;
    }

    public static void clearSession() {
        UserSession.loggedInNrp = null;
        UserSession.loggedInNama = null;
        UserSession.loggedInEmail = null;
        UserSession.loggedInRole = null;
    }

    public static String getLoggedInNrp() {
        return loggedInNrp;
    }

    public static String getLoggedInNama() {
        return loggedInNama;
    }

    public static String getLoggedInEmail() {
        return loggedInEmail;
    }

    public static String getLoggedInRole() {
        return loggedInRole;
    }

    public static boolean isLoggedIn() {
        return loggedInNrp != null;
    }
}
