package kaamverse.db.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import kaamverse.common.DatabaseException;

/**
 * Opens JDBC connections using db.properties.
 * GRASP High Cohesion: the only class that knows how to connect.
 * Use in try-with-resources so each connection is closed.
 */
public final class DBConnection {

    private static final String CONFIG_FILE = "/db.properties";

    private DBConnection() {
    }

    public static Connection open() {
        Properties config = loadConfig();
        try {
            return DriverManager.getConnection(
                    config.getProperty("db.url"),
                    config.getProperty("db.user"),
                    config.getProperty("db.password"));
        } catch (SQLException e) {
            throw new DatabaseException("Could not connect to the database", e);
        }
    }

    private static Properties loadConfig() {
        try (InputStream in = DBConnection.class.getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new DatabaseException("db.properties not found in src/main/resources");
            }
            Properties config = new Properties();
            config.load(in);
            return config;
        } catch (IOException e) {
            throw new DatabaseException("Could not read db.properties", e);
        }
    }
}
