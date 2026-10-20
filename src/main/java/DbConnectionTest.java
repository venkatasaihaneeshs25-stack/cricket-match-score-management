
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnectionTest {
    public static void main(String[] args) {
        String host = System.getenv("DB_HOST");
        String name = System.getenv("DB_NAME");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (host == null || name == null || user == null || password == null) {
            System.err.println("Database secrets are missing.");
            System.exit(1);
        }

        String url = "jdbc:mysql://" + host + ":3306/" + name
                   + "?connectTimeout=10000";

        try (Connection connection =
                 DriverManager.getConnection(url, user, password)) {
            System.out.println("SUCCESS: Connected to the cricket database.");
        } catch (SQLException e) {
           System.err.println("Connection failed.");
System.err.println("Error code: " + e.getErrorCode());
System.err.println("SQL state: " + e.getSQLState());
System.err.println("Message: " + e.getMessage());
System.exit(1);
        }
    }
}
