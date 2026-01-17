import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:sqlite:hospital.db";

    @SuppressWarnings("CallToPrintStackTrace")
    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            System.out.println("SQLite database connected successfully!");
        } catch (SQLException e) {
            System.out.println("Failed to connect to SQLite database.");
            e.printStackTrace();
        }
        return conn;
    }
}
