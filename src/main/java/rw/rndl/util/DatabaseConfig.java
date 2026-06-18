
package rw.rndl.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    // Separate databases
    private static final String APP_DB_URL = "jdbc:sqlite:library.db";
    private static final String SIMULATOR_DB_URL = "jdbc:sqlite:simulator.db";

    static {
        // Explicitly load SQLite JDBC driver
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Error loading SQLite JDBC driver: " + e.getMessage());
        }
    }

    public static Connection getAppConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(APP_DB_URL);
        enableForeignKeys(conn);
        return conn;
    }

    public static Connection getSimulatorConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(SIMULATOR_DB_URL);
        enableForeignKeys(conn);
        return conn;
    }

    private static void enableForeignKeys(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
    }

    public static void initializeDatabaseForApp() throws SQLException {
        String createBooksTable = "CREATE TABLE IF NOT EXISTS books (" +
                "isbn TEXT PRIMARY KEY," +
                "title TEXT NOT NULL," +
                "author TEXT NOT NULL," +
                "publication_year INTEGER NOT NULL," +
                "available INTEGER NOT NULL DEFAULT 1" +
                ")";

        String createMembersTable = "CREATE TABLE IF NOT EXISTS members (" +
                "member_id TEXT PRIMARY KEY," +
                "name TEXT NOT NULL" +
                ")";

        String createBorrowingRecordsTable = "CREATE TABLE IF NOT EXISTS borrowing_records (" +
                "record_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "member_id TEXT NOT NULL," +
                "isbn TEXT NOT NULL," +
                "borrow_date DATE NOT NULL," +
                "return_date DATE," +
                "FOREIGN KEY (member_id) REFERENCES members(member_id)," +
                "FOREIGN KEY (isbn) REFERENCES books(isbn)" +
                ")";

        try (Connection conn = getAppConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createBooksTable);
            stmt.execute(createMembersTable);
            stmt.execute(createBorrowingRecordsTable);
        }
    }

    public static void initializeDatabaseForSimulator() throws SQLException {
        String dropBorrowingRecords = "DROP TABLE IF EXISTS borrowing_records";
        String dropBooks = "DROP TABLE IF EXISTS books";
        String dropMembers = "DROP TABLE IF EXISTS members";

        String createBooksTable = "CREATE TABLE books (" +
                "isbn TEXT PRIMARY KEY," +
                "title TEXT NOT NULL," +
                "author TEXT NOT NULL," +
                "publication_year INTEGER NOT NULL," +
                "available INTEGER NOT NULL DEFAULT 1" +
                ")";

        String createMembersTable = "CREATE TABLE members (" +
                "member_id TEXT PRIMARY KEY," +
                "name TEXT NOT NULL" +
                ")";

        String createBorrowingRecordsTable = "CREATE TABLE borrowing_records (" +
                "record_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "member_id TEXT NOT NULL," +
                "isbn TEXT NOT NULL," +
                "borrow_date DATE NOT NULL," +
                "return_date DATE," +
                "FOREIGN KEY (member_id) REFERENCES members(member_id)," +
                "FOREIGN KEY (isbn) REFERENCES books(isbn)" +
                ")";

        try (Connection conn = getSimulatorConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(dropBorrowingRecords);
            stmt.execute(dropBooks);
            stmt.execute(dropMembers);

            stmt.execute(createBooksTable);
            stmt.execute(createMembersTable);
            stmt.execute(createBorrowingRecordsTable);
        }
    }
}

