package com.myapi;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.SQLException;

public class Database {

    private static HikariDataSource dataSource;

    static {
        // ---------------------------------------------------------
        // 🛡️ ROBUST LOADING STRATEGY
        // ---------------------------------------------------------
        Dotenv dotenv = null;
        try {
            // Try to load .env file (Localhost)
            dotenv = Dotenv.configure().ignoreIfMissing().load();
        } catch (Exception e) {
            // If it fails (Production), just ignore and continue
            System.out.println("⚠️ .env file not found. Using System Environment Variables.");
        }

        HikariConfig config = new HikariConfig();

        // Use a helper method to get variables safely from EITHER source
        String host = getEnv(dotenv, "DB_HOST");
        String port = getEnv(dotenv, "DB_PORT");
        String name = getEnv(dotenv, "DB_NAME");
        String user = getEnv(dotenv, "DB_USER");
        String pass = getEnv(dotenv, "DB_PASS");

        // Debugging: Print host to logs so we know it worked (Masking password)
        System.out.println("🔌 Connecting to Database Host: " + host);

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + name;
        
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(pass);

        // Performance Settings
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(30000);

        dataSource = new HikariDataSource(config);
        System.out.println("🚀 Database Connected Successfully!");
    }

    // Helper method: Tries .env first, then falls back to System Env (Render)
    private static String getEnv(Dotenv dotenv, String key) {
        String value = null;
        // 1. Try .env file
        if (dotenv != null) {
            value = dotenv.get(key);
        }
        // 2. If null, try System Environment (Render)
        if (value == null) {
            value = System.getenv(key);
        }
        // 3. If still null, default to empty string to prevent crashes
        return (value == null) ? "" : value;
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}