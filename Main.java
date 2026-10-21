
import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String URL =
        "jdbc:mysql://sql.freedb.tech:3306/freedb_Yza4PyvW";
    private static final String USER = "u_rs4QKT";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String password = System.getenv("DB_PASSWORD");

        if (password == null || password.isBlank()) {
            System.out.println("Configure DB_PASSWORD before running.");
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
                System.out.println("7. Add Player");
                System.out.println("8. Update Player");
                System.out.println("9. Delete Player");
                System.out.println("10. Add Match");
                System.out.println("11. Transaction Demo");
                System.out.println("12. Call Stored Procedure");
                System.out.println("0. Exit");
                System.out.print("Choice: ");

                int choice;
                try {
                    choice = Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Enter a valid number.");
                    continue;
                }

                try {
                    switch (choice) {
                        case 1 -> viewTable(con, "Teams");
                        case 2 -> viewTable(con, "Players");
                        case 3 -> viewTable(con, "Matches");
                        case 4 -> addTeam(con, sc);
                        case 5 -> updateTeam(con, sc);
                        case 6 -> deleteTeam(con, sc);
                        case 7 -> addPlayer(con, sc);
                        case 8 -> updatePlayer(con, sc);
                        case 9 -> deletePlayer(con, sc);
                        case 10 -> addMatch(con, sc);
                        case 11 -> transactionDemo(con, sc);
                        case 12 -> callStoredProcedure(con);
                        case 0 -> {
                            System.out.println("Goodbye!");
                            return;
                        }
                        default -> System.out.println("Invalid choice.");
                    }
                } catch (SQLException e) {
                    System.out.println("Database operation failed: "
                                       + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid input: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed: "
                               + e.getMessage());
        } finally {
            sc.close();
        }
    }

    private static void viewTable(Connection con, String table)
            throws SQLException {
        if (!table.equals("Teams") && !table.equals("Players")
                && !table.equals("Matches")) {
            throw new IllegalArgumentException("Unknown table.");
        }

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
        System.out.print("Team name: ");
        String name = sc.nextLine();
        System.out.print("Country: ");
        String country = sc.nextLine();

        String sql =
            "INSERT INTO Teams (team_name, country) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, country);
            System.out.println("Rows inserted: " + ps.executeUpdate());
        }
    }

    private static void updateTeam(Connection con, Scanner sc)
            throws SQLException {
        System.out.print("Team ID: ");
        int id = Integer.parseInt(sc.nextLine());
        System.out.print("New team name: ");
        String name = sc.nextLine();
        System.out.print("New country: ");
        String country = sc.nextLine();

        String sql =
            "UPDATE Teams SET team_name = ?, country = ? WHERE team_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, country);
            ps.setInt(3, id);
            System.out.println("Rows updated: " + ps.executeUpdate());
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
        }
    }

    private static void addPlayer(Connection con, Scanner sc)
            throws SQLException {
        System.out.print("Player name: ");
        String name = sc.nextLine();
        System.out.print("Role: ");
        String role = sc.nextLine();
        System.out.print("Team ID: ");
        int teamId = Integer.parseInt(sc.nextLine());

        String sql =
            "INSERT INTO Players (player_name, role, team_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, role);
            ps.setInt(3, teamId);
            System.out.println("Rows inserted: " + ps.executeUpdate());
        }
    }

    private static void updatePlayer(Connection con, Scanner sc)
            throws SQLException {
        System.out.print("Player ID: ");
        int id = Integer.parseInt(sc.nextLine());
        System.out.print("New player name: ");
        String name = sc.nextLine();
        System.out.print("New role: ");
        String role = sc.nextLine();
        System.out.print("New team ID: ");
        int teamId = Integer.parseInt(sc.nextLine());

        String sql =
            "UPDATE Players SET player_name = ?, role = ?, team_id = ? WHERE player_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, role);
            ps.setInt(3, teamId);
            ps.setInt(4, id);
            System.out.println("Rows updated: " + ps.executeUpdate());
        }
    }

    private static void deletePlayer(Connection con, Scanner sc)
            throws SQLException {
        System.out.print("Player ID to delete: ");
        int id = Integer.parseInt(sc.nextLine());

        String sql = "DELETE FROM Players WHERE player_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            System.out.println("Rows deleted: " + ps.executeUpdate());
        }
    }

    private static void addMatch(Connection con, Scanner sc)
            throws SQLException {
        System.out.print("Team 1 ID: ");
        int team1 = Integer.parseInt(sc.nextLine());
        System.out.print("Team 2 ID: ");
        int team2 = Integer.parseInt(sc.nextLine());
        if (team1 == team2) {
            System.out.println("Choose two different teams.");
            return;
        }

        System.out.print("Match date (YYYY-MM-DD): ");
        Date date = Date.valueOf(sc.nextLine());
        System.out.print("Venue: ");
        String venue = sc.nextLine();
        System.out.print("Format: ");
        String format = sc.nextLine();

        String sql =
            "INSERT INTO Matches (team1_id, team2_id, match_date, venue, `format`) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, team1);
            ps.setInt(2, team2);
            ps.setDate(3, date);
            ps.setString(4, venue);
            ps.setString(5, format);
            System.out.println("Rows inserted: " + ps.executeUpdate());
        }
    }

    private static void transactionDemo(Connection con, Scanner sc)
            throws SQLException {
        System.out.print("Team ID to update: ");
        int id = Integer.parseInt(sc.nextLine());
        System.out.print("New country: ");
        String country = sc.nextLine();

        boolean oldAutoCommit = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            String sql = "UPDATE Teams SET country = ? WHERE team_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, country);
                ps.setInt(2, id);
                int rows = ps.executeUpdate();

                if (rows == 0) {
                    con.rollback();
                    System.out.println("No matching team. Rolled back.");
                } else {
                    con.commit();
                    System.out.println("Transaction committed. Rows: " + rows);
                }
            }
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    private static void callStoredProcedure(Connection con)
            throws SQLException {
        // Create the procedure in phpMyAdmin before choosing menu option 12.
        try (CallableStatement cs =
                 con.prepareCall("{CALL GetAllTeams()}")) {
            boolean hasResults = cs.execute();
            if (hasResults) {
                try (ResultSet rs = cs.getResultSet()) {
                    while (rs.next()) {
                        System.out.println(
                            rs.getInt("team_id") + " | " +
                            rs.getString("team_name") + " | " +
                            rs.getString("country"));
                    }
                }
            }
        }
    }
    
private static void getAllTeams(Connection con)
        throws SQLException {
    String sql = "{CALL GetAllTeams()}";

    try (CallableStatement cs = con.prepareCall(sql);
         ResultSet rs = cs.executeQuery()) {

        System.out.println("ID\tTeam Name\tCountry");

        while (rs.next()) {
            System.out.println(
                rs.getInt("team_id") + "\t" +
                rs.getString("team_name") + "\t" +
                rs.getString("country")
            );
        }
    }
}

}
