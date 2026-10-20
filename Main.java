
import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String URL =
        "jdbc:mysql://sql.freedb.tech:3306/freedb_Yza4PvyW";
    private static final String USER = "u_rs4QKT";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Set your database password in the DB_PASSWORD
        // environment variable before running this program.
        String password = System.getenv("DB_PASSWORD");

        if (password == null || password.isEmpty()) {
            System.out.println(
                "Please configure the DB_PASSWORD environment variable.");
            return;
        }

        try (Connection con =
                 DriverManager.getConnection(URL, USER, password)) {

            System.out.println("Connected to Cricket Score Database!");

            while (true) {
                System.out.println("\n=== CRICKET MATCH SCORE MANAGEMENT ===");
                System.out.println("1. View Teams");
                System.out.println("2. View Players");
                System.out.println("3. View Matches");
                System.out.println("4. Add Team");
                System.out.println("5. Update Team");
                System.out.println("6. Delete Team");
                System.out.println("7. Transaction Demo");
                System.out.println("0. Exit");
                System.out.print("Enter choice: ");

                int choice;
                try {
                    choice = Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Enter a valid number.");
                    continue;
                }

                switch (choice) {
                    case 1 -> viewTable(con, "Teams");
                    case 2 -> viewTable(con, "Players");
                    case 3 -> viewTable(con, "Matches");
                    case 4 -> addTeam(con, sc);
                    case 5 -> updateTeam(con, sc);
                    case 6 -> deleteTeam(con, sc);
                    case 7 -> transactionDemo(con);
                    case 0 -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection/error: " +
                               e.getMessage());
        } finally {
            sc.close();
        }
    }

    private static void viewTable(Connection con, String table)
            throws SQLException {
        String sql = "SELECT * FROM " + table + " LIMIT 100";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();

            for (int i = 1; i <= columns; i++)
                System.out.print(md.getColumnName(i) + "\t");
            System.out.println();

            while (rs.next()) {
                for (int i = 1; i <= columns; i++)
                    System.out.print(rs.getString(i) + "\t");
                System.out.println();
            }
        }
    }

    private static void addTeam(Connection con, Scanner sc)
            throws SQLException {
        System.out.print("New team name: ");
        String name = sc.nextLine();

        String sql = "INSERT INTO Teams (team_name) VALUES (?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            System.out.println("Rows inserted: " + ps.executeUpdate());
        } catch (SQLException e) {
            System.out.println("Check the Teams column names: " +
                               e.getMessage());
        }
    }

    private static void updateTeam(Connection con, Scanner sc)
            throws SQLException {
        System.out.print("Team ID: ");
        int id = Integer.parseInt(sc.nextLine());
        System.out.print("New team name: ");
        String name = sc.nextLine();

        String sql =
            "UPDATE Teams SET team_name = ? WHERE team_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, id);
            System.out.println("Rows updated: " + ps.executeUpdate());
        } catch (SQLException e) {
            System.out.println("Check the Teams column names: " +
                               e.getMessage());
        }
    }

    private static void deleteTeam(Connection con, Scanner sc)
            throws SQLException {
        System.out.print("Team ID to delete: ");
        int id = Integer.parseInt(sc.nextLine());

        String sql = "DELETE FROM Teams WHERE team_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            System.out.println("Rows deleted: " + ps.executeUpdate());
        } catch (SQLException e) {
            System.out.println("Delete failed (the team may be referenced): "
                               + e.getMessage());
        }
    }

    private static void transactionDemo(Connection con)
            throws SQLException {
        boolean oldAutoCommit = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            try (Statement st = con.createStatement()) {
                st.executeUpdate(
                    "UPDATE Teams SET team_name = team_name WHERE 1 = 0");
            }
            con.commit();
            System.out.println("Transaction committed successfully.");
        } catch (SQLException e) {
            con.rollback();
            System.out.println("Transaction rolled back: " + e.getMessage());
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }
}
