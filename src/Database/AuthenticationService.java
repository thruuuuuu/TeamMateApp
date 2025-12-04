package Database;

import Entity.Organizer;
import Entity.Participant;
import Log.Logger;

import java.sql.*;

public class AuthenticationService {

    /**
     * Authenticate participant using ID and password (Name + ID)
     */
    public static Participant authenticateParticipant(String participantId, String password) {
        String query = "SELECT * FROM participants WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, participantId.toUpperCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");

                    // Verify password
                    if (storedPassword.equals(password)) {
                        Logger.info("Participant authenticated successfully: " + participantId);

                        return new Participant(
                                rs.getString("id"),
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getString("preferred_game"),
                                rs.getInt("skill_level"),
                                rs.getString("preferred_role"),
                                rs.getInt("personality_score")
                        );
                    } else {
                        Logger.warning("Invalid password for participant: " + participantId);
                    }
                } else {
                    Logger.warning("Participant not found: " + participantId);
                }
            }
        } catch (SQLException e) {
            Logger.error("Error authenticating participant", e);
        }

        return null;
    }

    /**
     * Authenticate organizer using ID and password
     */
    public static Organizer authenticateOrganizer(String organizerId, String password) {
        String query = "SELECT * FROM organizers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, organizerId.toUpperCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");

                    // Verify password
                    if (storedPassword.equals(password)) {
                        Logger.info("Organizer authenticated successfully: " + organizerId);

                        return new Organizer(
                                rs.getString("id"),
                                rs.getString("name"),
                                rs.getString("email"),
                                storedPassword
                        );
                    } else {
                        Logger.warning("Invalid password for organizer: " + organizerId);
                    }
                } else {
                    Logger.warning("Organizer not found: " + organizerId);
                }
            }
        } catch (SQLException e) {
            Logger.error("Error authenticating organizer", e);
        }

        return null;
    }

    /**
     * Register a new organizer
     */
    public static boolean registerOrganizer(Organizer organizer) {
        String query = "INSERT INTO organizers (id, name, email, password) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, organizer.getId());
            pstmt.setString(2, organizer.getName());
            pstmt.setString(3, organizer.getEmail());
            pstmt.setString(4, organizer.getPassword());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                Logger.info("Organizer registered successfully: " + organizer.getId());
                return true;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                Logger.warning("Organizer email already exists: " + organizer.getEmail());
                System.out.println("✗ Error: Email already registered");
            } else {
                Logger.error("Error registering organizer", e);
            }
        }

        return false;
    }

    /**
     * Generate password for participant (Name + ID)
     */
    public static String generateParticipantPassword(String id) {
        return  id+ "-123";
    }

    /**
     * Check if participant ID exists
     */
    public static boolean participantExists(String participantId) {
        String query = "SELECT COUNT(*) FROM participants WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, participantId.toUpperCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            Logger.error("Error checking participant existence", e);
        }

        return false;
    }

    /**
     * Check if organizer ID exists
     */
    public static boolean organizerExists(String organizerId) {
        String query = "SELECT COUNT(*) FROM organizers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, organizerId.toUpperCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            Logger.error("Error checking organizer existence", e);
        }

        return false;
    }
}