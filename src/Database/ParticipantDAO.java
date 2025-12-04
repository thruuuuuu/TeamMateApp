package Database;

import Entity.Participant;
import Enums.Game;
import Enums.Role;
import Log.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipantDAO {

    /**
     * Insert a new participant into the database
     */
    public static boolean insertParticipant(Participant participant, String password) {
        String query = "INSERT INTO participants (id, name, email, password, preferred_game, " +
                "skill_level, preferred_role, personality_score, personality_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, participant.getId());
            pstmt.setString(2, participant.getName());
            pstmt.setString(3, participant.getEmail());
            pstmt.setString(4, password);
            pstmt.setString(5, participant.getGame().getDisplayName());
            pstmt.setInt(6, participant.getSkillLevel());
            pstmt.setString(7, participant.getRole().getDisplayName());
            pstmt.setInt(8, participant.getPersonalityScore());
            pstmt.setString(9, participant.getPersonalityType().getDisplayName());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                Logger.info("Participant inserted into database: " + participant.getId());
                return true;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                Logger.warning("Participant ID or email already exists: " + participant.getId());
                System.out.println("✗ Error: Participant ID or email already exists");
            } else {
                Logger.error("Error inserting participant", e);
            }
        }

        return false;
    }

    /**
     * Get participant by ID
     */
    public static Participant getParticipantById(String participantId) {
        String query = "SELECT * FROM participants WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, participantId.toUpperCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Participant(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("preferred_game"),
                            rs.getInt("skill_level"),
                            rs.getString("preferred_role"),
                            rs.getInt("personality_score")
                    );
                }
            }
        } catch (SQLException e) {
            Logger.error("Error retrieving participant", e);
        }

        return null;
    }

    /**
     * Get all participants from database
     */
    public static List<Participant> getAllParticipants() {
        List<Participant> participants = new ArrayList<>();
        String query = "SELECT * FROM participants ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

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
                participants.add(p);
            }

            Logger.info("Retrieved " + participants.size() + " participants from database");
        } catch (SQLException e) {
            Logger.error("Error retrieving all participants", e);
        }

        return participants;
    }

    /**
     * Update participant email
     */
    public static boolean updateParticipantEmail(String participantId, String newEmail) {
        String query = "UPDATE participants SET email = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, newEmail);
            pstmt.setString(2, participantId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                Logger.info("Updated email for participant: " + participantId);
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Error updating participant email", e);
        }

        return false;
    }

    /**
     * Update participant skill level
     */
    public static boolean updateParticipantSkill(String participantId, int newSkill) {
        String query = "UPDATE participants SET skill_level = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, newSkill);
            pstmt.setString(2, participantId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                Logger.info("Updated skill level for participant: " + participantId);
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Error updating participant skill", e);
        }

        return false;
    }

    /**
     * Update participant game preference
     */
    public static boolean updateParticipantGame(String participantId, Game newGame) {
        String query = "UPDATE participants SET preferred_game = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, newGame.getDisplayName());
            pstmt.setString(2, participantId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                Logger.info("Updated game preference for participant: " + participantId);
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Error updating participant game", e);
        }

        return false;
    }

    /**
     * Update participant role preference
     */
    public static boolean updateParticipantRole(String participantId, Role newRole) {
        String query = "UPDATE participants SET preferred_role = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, newRole.getDisplayName());
            pstmt.setString(2, participantId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                Logger.info("Updated role preference for participant: " + participantId);
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Error updating participant role", e);
        }

        return false;
    }

    /**
     * Delete participant by ID
     */
    public static boolean deleteParticipant(String participantId) {
        String query = "DELETE FROM participants WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, participantId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                Logger.info("Deleted participant: " + participantId);
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Error deleting participant", e);
        }

        return false;
    }

    /**
     * Bulk insert participants from CSV validation
     */
    public static int bulkInsertParticipants(List<Participant> participants) {
        String query = "INSERT INTO participants (id, name, email, password, preferred_game, " +
                "skill_level, preferred_role, personality_score, personality_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "name = VALUES(name), email = VALUES(email), preferred_game = VALUES(preferred_game), " +
                "skill_level = VALUES(skill_level), preferred_role = VALUES(preferred_role), " +
                "personality_score = VALUES(personality_score), personality_type = VALUES(personality_type)";

        int insertedCount = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            conn.setAutoCommit(false);

            for (Participant p : participants) {
                pstmt.setString(1, p.getId());
                pstmt.setString(2, p.getName());
                pstmt.setString(3, p.getEmail());
                pstmt.setString(4, AuthenticationService.generateParticipantPassword(p.getId()));
                pstmt.setString(5, p.getGame().getDisplayName());
                pstmt.setInt(6, p.getSkillLevel());
                pstmt.setString(7, p.getRole().getDisplayName());
                pstmt.setInt(8, p.getPersonalityScore());
                pstmt.setString(9, p.getPersonalityType().getDisplayName());

                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            conn.commit();

            for (int result : results) {
                if (result > 0) {
                    insertedCount++;
                }
            }

            Logger.info("Bulk inserted " + insertedCount + " participants into database");

        } catch (SQLException e) {
            Logger.error("Error during bulk insert", e);
        }

        return insertedCount;
    }

    /**
     * Get participants not assigned to any team
     */
    public static List<Participant> getUnassignedParticipants() {
        List<Participant> participants = new ArrayList<>();
        String query = "SELECT p.* FROM participants p " +
                "LEFT JOIN team_members tm ON p.id = tm.participant_id " +
                "WHERE tm.participant_id IS NULL " +
                "ORDER BY p.id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

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
                participants.add(p);
            }

            Logger.info("Retrieved " + participants.size() + " unassigned participants");
        } catch (SQLException e) {
            Logger.error("Error retrieving unassigned participants", e);
        }

        return participants;
    }
}