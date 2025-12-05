package com.gdn.training.member;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseSetup {

    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "root";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                Statement stmt = conn.createStatement()) {

            createDatabase(stmt, "member_db");
            createDatabase(stmt, "cart_db");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createDatabase(Statement stmt, String dbName) {
        try {
            stmt.executeUpdate("CREATE DATABASE " + dbName);
            System.out.println("Database " + dbName + " created successfully.");
        } catch (Exception e) {
            if (e.getMessage().contains("already exists")) {
                System.out.println("Database " + dbName + " already exists.");
            } else {
                System.out.println("Failed to create database " + dbName + ": " + e.getMessage());
            }
        }
    }
}
