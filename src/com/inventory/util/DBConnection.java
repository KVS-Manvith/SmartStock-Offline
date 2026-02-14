package com.inventory.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/inventory_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "inventory_user";
    private static final String DEFAULT_PASSWORD = "inventory123";

    private static final String URL = getEnvOrDefault("INVENTORY_DB_URL", DEFAULT_URL);
    private static final String USER = getEnvOrDefault("INVENTORY_DB_USER", DEFAULT_USER);
    private static final String PASSWORD = getEnvOrDefault("INVENTORY_DB_PASSWORD", DEFAULT_PASSWORD);

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "MySQL JDBC driver not found. Ensure lib/mysql-connector-j-9.6.0.jar is on the classpath.",
                    e
            );
        }
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to connect to MySQL using URL=" + URL + ", USER=" + USER + ". "
                            + "Update DBConnection defaults or set INVENTORY_DB_URL/USER/PASSWORD environment variables.",
                    e
            );
        }
    }
}
