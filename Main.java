```java
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
            System.out.println("Configure the DB_PASSWORD environment variable.");
            sc.close();
            return;
        }

        try (Connection con =
                 DriverManager.getConnection(URL, USER, password)) {
            System.out.println("Connected to Cricket Score Database!");

            boolean running = true;

            while (running) {
                System.out.println("\n=== CRICKET MATCH SCORE MANAGEMENT ===");
                System.out.println("1. View Teams");
                System.out.println("2. View Players");
                System.out.println("3. View Matches");
                System.out.println("4. Add Team");
                System.out.println("5. Update Team");
                System.out.println("6. Delete Team");
                System.out.println("7. Transaction Demo");
                System.out.println("8. Get All Teams (Stored Procedure)");
                System.out.println("9. Update Match Score");
                System.out.println("10. View Scorecards");
                System.out.println("0. Exit");
                System.out.print("Enter choice: ");

                int choice;
                try {
                    choice = Integer.parseInt(sc.nextLine().trim());
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
                        case 7 -> transactionDemo(con, sc);
                        case 8 -> getAllTeams(con);
                        case 9 -> updateMatchScore(con, sc);
                        case 10 -> viewScorecards(con);
                        case 0 -> {
                            running = false;
                            System.out.println("Goodbye!");
                        }
                        default -> System.out.println("Invalid choice.");
                    }
                } catch (SQLException e) {
                    System.out.println("Operation failed: " + e.getMessage());
                } catch (NumberFormatException e) {
                    System.out.println("Enter valid numeric values.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            System.out.println("SQL state: " + e.getSQLState());
            System.out.println("Error code: " + e.getErrorCode());
        } finally {
            sc.close();
        }
    }

    private static void viewTable(Connection con, String table)
            throws SQLException {
        if (!table.equals("Teams") &&
            !table.equals("Players") &&
            !table.equals("Matches")) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        String sql = "SELECT * FROM " + table;

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
        String name = sc.nextLine().trim();

        System.out.print("Country: ");
        String country = sc.nextLine().trim();

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
        int id = Integer.parseInt(sc.nextLine().trim());

        System.out.print("New team name: ");
        String name = sc.nextLine().trim();

        System.out.print("New country: ");
        String country = sc.nextLine().trim();

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
        int id = Integer.parseInt(sc.nextLine().trim());

        String sql = "DELETE FROM Teams WHERE team_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            System.out.println("Rows deleted: " + ps.executeUpdate());
        }
    }

    private static void getAllTeams(Connection con)
            throws SQLException {
        try (CallableStatement cs =
                 con.prepareCall("{CALL GetAllTeams()}");
             ResultSet rs = cs.executeQuery()) {
            System.out.println("\n--- ALL TEAMS ---");
            System.out.println("ID\tTeam Name\tCountry");

            while (rs.next()) {
                System.out.println(
                    rs.getInt("team_id") + "\t" +
                    rs.getString("team_name") + "\t" +
                    rs.getString("country"));
            }
        }
    }

    private static void transactionDemo(Connection con, Scanner sc)
            throws SQLException {
        System.out.print("Team ID to update: ");
        int id = Integer.parseInt(sc.nextLine().trim());

        System.out.print("New country: ");
        String country = sc.nextLine().trim();

        boolean oldAutoCommit = con.getAutoCommit();

        try {
            con.setAutoCommit(false);

            String sql =
                "UPDATE Teams SET country = ? WHERE team_id = ?";

            int rows;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, country);
                ps.setInt(2, id);
                rows = ps.executeUpdate();
            }

            if (rows == 0) {
                con.rollback();
                System.out.println(
                    "No matching team. Transaction rolled back.");
            } else {
                con.commit();
                System.out.println(
                    "Transaction committed. Rows updated: " + rows);
            }
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    private static void updateMatchScore(Connection con, Scanner sc)
            throws SQLException {
        System.out.print("Enter Match ID (1-5): ");
        int matchId = Integer.parseInt(sc.nextLine().trim());

        int team1Id;
        int team2Id;

        String check =
            "SELECT m.team1_id, m.team2_id, " +
            "t1.team_name AS team1_name, " +
            "t2.team_name AS team2_name " +
            "FROM Matches m " +
            "JOIN Teams t1 ON m.team1_id = t1.team_id " +
            "JOIN Teams t2 ON m.team2_id = t2.team_id " +
            "WHERE m.match_id = ?";

        try (PreparedStatement ps = con.prepareStatement(check)) {
            ps.setInt(1, matchId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Match not found.");
                    return;
                }

                team1Id = rs.getInt("team1_id");
                team2Id = rs.getInt("team2_id");

                System.out.println(
                    "1. " + rs.getString("team1_name"));
                System.out.println(
                    "2. " + rs.getString("team2_name"));
            }
        }

        System.out.print("Team 1 runs: ");
        int runs1 = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Team 1 wickets (0-10): ");
        int wickets1 = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Team 1 overs: ");
        double overs1 = Double.parseDouble(sc.nextLine().trim());

        System.out.print("Team 2 runs: ");
        int runs2 = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Team 2 wickets (0-10): ");
        int wickets2 = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Team 2 overs: ");
        double overs2 = Double.parseDouble(sc.nextLine().trim());

        if (runs1 < 0 || runs2 < 0 ||
            wickets1 < 0 || wickets1 > 10 ||
            wickets2 < 0 || wickets2 > 10 ||
            overs1 < 0 || overs2 < 0 ||
            overs1 > 999.9 || overs2 > 999.9) {
            System.out.println("Invalid score details.");
            return;
        }

        System.out.println("Winner:");
        System.out.println("1. Team 1");
        System.out.println("2. Team 2");
        System.out.println("0. Tie / No result");
        System.out.print("Enter choice: ");

        int winnerChoice = Integer.parseInt(sc.nextLine().trim());

        if (winnerChoice < 0 || winnerChoice > 2) {
            System.out.println("Invalid winner choice.");
            return;
        }

        if (winnerChoice == 1 && runs1 < runs2 ||
            winnerChoice == 2 && runs2 < runs1) {
            System.out.println(
                "The selected winner has fewer runs. Check the score.");
            return;
        }

        if (winnerChoice == 1 && runs1 == runs2 ||
            winnerChoice == 2 && runs1 == runs2) {
            System.out.println(
                "Scores are tied. Choose Tie / No result.");
            return;
        }

        String sql =
            "UPDATE Matches SET team1_runs = ?, team1_wickets = ?, " +
            "team1_overs = ?, team2_runs = ?, team2_wickets = ?, " +
            "team2_overs = ?, winner_team_id = ? WHERE match_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, runs1);
            ps.setInt(2, wickets1);
            ps.setDouble(3, overs1);
            ps.setInt(4, runs2);
            ps.setInt(5, wickets2);
            ps.setDouble(6, overs2);

            if (winnerChoice == 0)
                ps.setNull(7, Types.INTEGER);
            else if (winnerChoice == 1)
                ps.setInt(7, team1Id);
            else
                ps.setInt(7, team2Id);

            ps.setInt(8, matchId);

            if (ps.executeUpdate() > 0)
                System.out.println("Match score updated successfully!");
            else
                System.out.println("No match was updated.");
        }
    }

    private static void viewScorecards(Connection con)
            throws SQLException {
        String sql =
            "SELECT m.match_id, m.match_date, m.venue, m.format, " +
            "t1.team_name AS team1_name, " +
            "m.team1_runs, m.team1_wickets, m.team1_overs, " +
            "t2.team_name AS team2_name, " +
            "m.team2_runs, m.team2_wickets, m.team2_overs, " +
            "tw.team_name AS winner_name " +
            "FROM Matches m " +
            "JOIN Teams t1 ON m.team1_id = t1.team_id " +
            "JOIN Teams t2 ON m.team2_id = t2.team_id " +
            "LEFT JOIN Teams tw ON m.winner_team_id = tw.team_id " +
            "ORDER BY m.match_id";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.println("\n==============================");
                System.out.println("Match ID: " + rs.getInt("match_id"));
                System.out.println("Date: " + rs.getDate("match_date"));
                System.out.println("Venue: " + rs.getString("venue"));
                System.out.println("Format: " + rs.getString("format"));

                System.out.println(
                    rs.getString("team1_name") + ": " +
                    rs.getInt("team1_runs") + "/" +
                    rs.getInt("team1_wickets") + " in " +
                    rs.getBigDecimal("team1_overs") + " overs");

                System.out.println(
                    rs.getString("team2_name") + ": " +
                    rs.getInt("team2_runs") + "/" +
                    rs.getInt("team2_wickets") + " in " +
                    rs.getBigDecimal("team2_overs") + " overs");

                String winner = rs.getString("winner_name");

                if (winner == null) {
                    System.out.println("Result: Not decided / Tie / No result");
                } else {
                    System.out.println("Winner: " + winner);
                }
            }

            if (!found)
                System.out.println("No matches found.");
        }
    }
}
```
