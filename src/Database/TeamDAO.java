package Database;

import Entity.Participant;
import Entity.Team;
import Log.Logger;
import Main.FormationStatistics;

import java.sql.*;
import java.util.*;

public class TeamDAO {

    /**
     * Save formed teams to database
     */
    public static boolean saveTeamsToDatabase(List<Team> teams, int teamSize, String organizerId) {
        if (teams == null || teams.isEmpty()) {
            Logger.warning("No teams to save to database");
            return false;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Clear existing team assignments
            clearAllTeamAssignments(conn);

            // Insert teams and members
            for (Team team : teams) {
                int teamId = insertTeam(conn, team, teamSize, organizerId);

                if (teamId > 0) {
                    // Insert team members
                    for (Participant p : team.getMembers()) {
                        insertTeamMember(conn, teamId, p.getId());
                    }
                }
            }

            conn.commit();
            Logger.info("Successfully saved " + teams.size() + " teams to database");
            return true;

        } catch (SQLException e) {
            Logger.error("Error saving teams to database", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    Logger.error("Error rolling back transaction", ex);
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    Logger.error("Error resetting auto-commit", e);
                }
            }
        }
    }

    /**
     * Insert a team and return its generated ID
     */
    private static int insertTeam(Connection conn, Team team, int teamSize, String organizerId) throws SQLException {
        String query = "INSERT INTO teams (team_size, avg_skill_level, role_diversity, created_by) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, teamSize);
            pstmt.setDouble(2, team.getAverageSkill());
            pstmt.setLong(3, team.getRoleDiversity());
            pstmt.setString(4, organizerId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Insert a team member
     */
    private static void insertTeamMember(Connection conn, int teamId, String participantId) throws SQLException {
        String query = "INSERT INTO team_members (team_id, participant_id) VALUES (?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, teamId);
            pstmt.setString(2, participantId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Clear all existing team assignments
     */
    public static void clearAllTeamAssignments(Connection conn) throws SQLException {
        String deleteMembers = "DELETE FROM team_members";
        String deleteTeams = "DELETE FROM teams";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(deleteMembers);
            stmt.executeUpdate(deleteTeams);
            Logger.info("Cleared all existing team assignments");
        }
    }

    /**
     * Get all teams from database
     */
    public static List<Team> getAllTeamsFromDatabase() {
        List<Team> teams = new ArrayList<>();
        String query = "SELECT DISTINCT t.team_id FROM teams t ORDER BY t.team_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int teamId = rs.getInt("team_id");
                Team team = getTeamById(teamId);
                if (team != null) {
                    teams.add(team);
                }
            }

            Logger.info("Retrieved " + teams.size() + " teams from database");
        } catch (SQLException e) {
            Logger.error("Error retrieving teams from database", e);
        }

        return teams;
    }

    /**
     * Get a specific team by ID with all its members
     */
    public static Team getTeamById(int teamId) {
        Team team = new Team(teamId);

        String query = "SELECT p.* FROM participants p " +
                "JOIN team_members tm ON p.id = tm.participant_id " +
                "WHERE tm.team_id = ? " +
                "ORDER BY p.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, teamId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Participant p = new Participant(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("preferred_game"),
                            rs.getInt("skill_level"),
                            rs.getString("preferred_role"),
                            rs.getInt("personality_score")
                    );
                    team.addMember(p);
                }
            }
        } catch (SQLException e) {
            Logger.error("Error retrieving team by ID", e);
            return null;
        }

        return team;
    }

    /**
     * Get team assignment for a specific participant
     */
    public static Integer getParticipantTeamId(String participantId) {
        String query = "SELECT team_id FROM team_members WHERE participant_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, participantId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("team_id");
                }
            }
        } catch (SQLException e) {
            Logger.error("Error getting participant team ID", e);
        }

        return null;
    }

    /**
     * Save team formation history
     */
    public static int saveTeamFormation(FormationStatistics stats, String formationName, String organizerId) {
        String query = "INSERT INTO team_formations (formation_name, team_size, total_participants, " +
                "teams_formed, participants_assigned, participants_remaining, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, formationName);
            pstmt.setInt(2, stats.getTeamSize());
            pstmt.setInt(3, stats.getTotalParticipants());
            pstmt.setInt(4, stats.getTeamsFormed());
            pstmt.setInt(5, stats.getParticipantsAssigned());
            pstmt.setInt(6, stats.getParticipantsRemaining());
            pstmt.setString(7, organizerId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int formationId = generatedKeys.getInt(1);
                        Logger.info("Saved team formation history with ID: " + formationId);
                        return formationId;
                    }
                }
            }
        } catch (SQLException e) {
            Logger.error("Error saving team formation history", e);
        }

        return -1;
    }

    /**
     * Link teams to a formation
     */
    public static void linkTeamsToFormation(int formationId, List<Team> teams) {
        String query = "INSERT INTO formation_teams (formation_id, team_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            conn.setAutoCommit(false);

            for (Team team : teams) {
                pstmt.setInt(1, formationId);
                pstmt.setInt(2, team.getTeamId());
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit();

            Logger.info("Linked " + teams.size() + " teams to formation " + formationId);

        } catch (SQLException e) {
            Logger.error("Error linking teams to formation", e);
        }
    }

    /**
     * Get team formation history
     */
    public static List<Map<String, Object>> getTeamFormationHistory(String organizerId) {
        List<Map<String, Object>> history = new ArrayList<>();
        String query = "SELECT * FROM team_formations WHERE created_by = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, organizerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> formation = new HashMap<>();
                    formation.put("formation_id", rs.getInt("formation_id"));
                    formation.put("formation_name", rs.getString("formation_name"));
                    formation.put("team_size", rs.getInt("team_size"));
                    formation.put("total_participants", rs.getInt("total_participants"));
                    formation.put("teams_formed", rs.getInt("teams_formed"));
                    formation.put("participants_assigned", rs.getInt("participants_assigned"));
                    formation.put("participants_remaining", rs.getInt("participants_remaining"));
                    formation.put("created_at", rs.getTimestamp("created_at"));

                    history.add(formation);
                }
            }
        } catch (SQLException e) {
            Logger.error("Error retrieving team formation history", e);
        }

        return history;
    }
}