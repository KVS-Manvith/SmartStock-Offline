package com.inventory.ui;

import com.inventory.util.DBConnection;

public class TestConnection {
    public static void main(String[] args) {
        if (DBConnection.getConnection() != null) {
            System.out.println("Database Connected Successfully!");
        }
    }
}
