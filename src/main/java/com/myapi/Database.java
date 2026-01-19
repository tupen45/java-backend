package com.myapi;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv; // Import the library
import java.sql.Connection;
import java.sql.SQLException;

public class Database {

    private static HikariDataSource dataSource;

    static {
        // 1. Load the .env file
        Dotenv dotenv = Dotenv.load();

        HikariConfig config = new HikariConfig();

        // 2. Read credentials securely
        String host = dotenv.get("DB_HOST");
        String port = dotenv.get("DB_PORT");
        String name = dotenv.get("DB_NAME");
        String user = dotenv.get("DB_USER");
        String pass = dotenv.get("DB_PASS");

        // 3. Build the URL dynamically
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
        System.out.println("🔒 Database Connected securely using .env (" + host + ")");
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}