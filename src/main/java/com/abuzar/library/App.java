package com.abuzar.library;

import java.sql.Connection;

public class App {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Connected to library_db!");
            System.out.println(" Library Management System - Backend Ready");
        } catch (Exception e) {
            System.err.println(" Connection failed:");
            e.printStackTrace();
        }
    }
}