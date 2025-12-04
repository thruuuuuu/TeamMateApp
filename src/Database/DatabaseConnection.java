package Database;

import Log.Logger;
import java.sql.*;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/teammate_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // XAMPP default has no password

    // Remove static connection - get fresh connection each time
    private DatabaseConnection() {}

    /**
     * Get database connection (creates new connection each time)
     * This prevents stale connection issues
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Logger.debug("Database connection established");
        } catch (ClassNotFoundException e) {
            Logger.error("MySQL JDBC Driver not found", e);
            System.err.println("ERROR: MySQL JDBC Driver not found. Please add mysql-connector-java to your classpath.");
        } catch (SQLException e) {
            Logger.error("Failed to connect to database", e);
            System.err.println("ERROR: Failed to connect to database. Please check your MySQL server and credentials.");
            System.err.println("Details: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                Logger.info("Database connection test successful");
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Database connection test failed", e);
        }
        return false;
    }

    /**
     * Close database connection (now just for compatibility)
     */
    public static void closeConnection() {
        Logger.info("Connection cleanup - connections are now auto-closed with try-resources");
    }

    /**
     * Execute a query that returns results
     * WARNING: Caller must close ResultSet and Connection
     */
    @Deprecated
    public static ResultSet executeQuery(String query) throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    /**
     * Execute an update query (INSERT, UPDATE, DELETE)
     */
    public static int executeUpdate(String query) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(query);
        }
    }

    /**
     * Execute batch updates
     */
    public static int[] executeBatch(String[] queries) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            for (String query : queries) {
                stmt.addBatch(query);
            }
            return stmt.executeBatch();
        }
    }
}