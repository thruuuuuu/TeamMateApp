package Test;

import Log.Logger;
import Manager.TeamManager;
import Entity.Participant;
import Enums.Game;
import Enums.Role;
import Main.FormationStatistics;
import Database.ParticipantDAO;

import java.io.*;

public class UserAcceptanceTestsJava {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        Logger.info("=== Starting User Acceptance Tests ===");

        testCompleteOrganizerWorkflow();
        testCompleteParticipantWorkflow();
        testTeamFormationScenario();
        testLoadPreviousFormationScenario();
        testRemainingParticipantsScenario();
        testProfileUpdateScenario();
        testErrorRecoveryScenario();
        testDatabasePersistenceScenario();
        testMultipleOrganizerScenario();

        printTestResults();
    }

    private static void testCompleteOrganizerWorkflow() {
        Logger.info("Testing Complete Organizer Workflow...");

        try {
            // UAT-001: Organizer adds participants directly to DB, forms teams, exports to CSV
            TeamManager tm = new TeamManager();
            tm.setCurrentOrganizer("ORG001");

            // Step 1: Add participants directly to database
            Logger.info("Step 1: Adding participants to database");
            for (int i = 0; i < 15; i++) {
                Participant p = new Participant(
                        "OrgWorkflowUser" + i,
                        "orgworkflow" + i + "@test.com",
                        Game.CHESS,
                        5 + (i % 5),
                        Role.STRATEGIST,
                        70 + (i % 20)
                );
                tm.addParticipant(p);
            }

            // Step 2: Set team size
            Logger.info("Step 2: Setting team size");
            tm.setTeamSize(5);

            // Step 3: Form teams (automatically saves to DB)
            Logger.info("Step 3: Forming teams");
            FormationStatistics stats = tm.formTeams();
            assert stats != null : "Statistics should be returned";

            // Step 4: Save to database (done automatically in formTeams)
            Logger.info("Step 4: Teams auto-saved to database");
            tm.saveTeamsToDatabase();

            // Step 5: Export to CSV
            Logger.info("Step 5: Exporting to CSV");
            String outputFile = "uat_organizer_teams.csv";
            tm.saveTeamsToCSV(outputFile);

            File teamFile = new File(outputFile);
            assert teamFile.exists() : "Team file should be created";

            // Step 6: Save remaining participants to CSV
            Logger.info("Step 6: Saving remaining participants to CSV");
            if (tm.hasRemainingParticipants()) {
                tm.appendRemainingParticipantsToCSV(outputFile);
            }

            // Clean up
            new File(outputFile).delete();

            testsPassed++;
            Logger.info("✓ Complete organizer workflow test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Complete organizer workflow test failed", e);
        }
    }

    private static void testCompleteParticipantWorkflow() {
        Logger.info("Testing Complete Participant Workflow...");

        try {
            // UAT-002: Participant registers, checks info, updates profile
            TeamManager tm = new TeamManager();

            // Step 1: Register new participant
            Logger.info("Step 1: Registering new participant");
            Participant p = new Participant(
                    "UAT Participant",
                    "uatparticipant@test.com",
                    Game.FIFA,
                    7,
                    Role.ATTACKER,
                    85
            );
            tm.addParticipant(p);
            String participantId = p.getId();

            // Step 2: Verify registration in database
            Logger.info("Step 2: Verifying registration in database");
            assert tm.participantExists(participantId) : "Participant should exist in database";

            // Step 3: Check participant info from database
            Logger.info("Step 3: Checking participant info from database");
            Participant retrieved = tm.getParticipantById(participantId);
            assert retrieved.getName().equals("UAT Participant") : "Name should match";
            assert retrieved.getEmail().equals("uatparticipant@test.com") : "Email should match";

            // Step 4: Update email in database
            Logger.info("Step 4: Updating email in database");
            tm.updateParticipantEmail(participantId, "newemail@test.com");
            Participant updated = tm.getParticipantById(participantId);
            assert updated.getEmail().equals("newemail@test.com") : "Email should be updated in DB";

            // Step 5: Update skill level in database
            Logger.info("Step 5: Updating skill level in database");
            tm.updateParticipantSkill(participantId, 9);
            updated = tm.getParticipantById(participantId);
            assert updated.getSkillLevel() == 9 : "Skill should be updated in DB";

            // Clean up
            ParticipantDAO.deleteParticipant(participantId);

            testsPassed++;
            Logger.info("✓ Complete participant workflow test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Complete participant workflow test failed", e);
        }
    }

    private static void testTeamFormationScenario() {
        Logger.info("Testing Team Formation Scenario...");

        try {
            // UAT-003: Form balanced teams with diverse roles and personalities
            TeamManager tm = new TeamManager();

            // Add diverse participants to database
            tm.addParticipant(new Participant("Leader1", "leader1@test.com", Game.CHESS, 8, Role.STRATEGIST, 95));
            tm.addParticipant(new Participant("Balanced1", "balanced1@test.com", Game.FIFA, 7, Role.ATTACKER, 80));
            tm.addParticipant(new Participant("Thinker1", "thinker1@test.com", Game.BASKETBALL, 6, Role.DEFENDER, 65));
            tm.addParticipant(new Participant("Leader2", "leader2@test.com", Game.CSGO, 9, Role.SUPPORTER, 92));
            tm.addParticipant(new Participant("Balanced2", "balanced2@test.com", Game.DOTA2, 7, Role.COORDINATOR, 75));
            tm.addParticipant(new Participant("Thinker2", "thinker2@test.com", Game.VALORANT, 5, Role.STRATEGIST, 60));
            tm.addParticipant(new Participant("Leader3", "leader3@test.com", Game.CHESS, 8, Role.ATTACKER, 90));
            tm.addParticipant(new Participant("Balanced3", "balanced3@test.com", Game.FIFA, 6, Role.DEFENDER, 78));
            tm.addParticipant(new Participant("Thinker3", "thinker3@test.com", Game.BASKETBALL, 7, Role.SUPPORTER, 62));

            // Form teams of 3 and auto-save to database
            tm.setTeamSize(3);
            FormationStatistics stats = tm.formTeams();
            tm.saveTeamsToDatabase();

            assert stats != null : "Statistics should be generated";
            assert stats.getTeamsFormed() >= 3 : "Should form at least 3 teams";

            testsPassed++;
            Logger.info("✓ Team formation scenario test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Team formation scenario test failed", e);
        }
    }

    private static void testLoadPreviousFormationScenario() {
        Logger.info("Testing Load Previous Formation Scenario...");

        try {
            // UAT-004: Save teams to CSV and load them back
            TeamManager tm1 = new TeamManager();

            // Create and save formation
            for (int i = 0; i < 10; i++) {
                Participant p = new Participant(
                        "LoadFormUser" + i,
                        "loadform" + i + "@test.com",
                        Game.VALORANT,
                        5 + i % 5,
                        Role.COORDINATOR,
                        70 + i % 30
                );
                tm1.addParticipant(p);
            }

            tm1.setTeamSize(3);
            tm1.formTeams();
            tm1.saveTeamsToDatabase();

            String formationFile = "uat_formation.csv";
            tm1.saveTeamsToCSV(formationFile);
            if (tm1.hasRemainingParticipants()) {
                tm1.appendRemainingParticipantsToCSV(formationFile);
            }

            // Load formation from CSV
            TeamManager tm2 = new TeamManager();
            tm2.loadTeamFormationFromCSV(formationFile);

            // Verify file was read successfully
            File file = new File(formationFile);
            assert file.exists() : "Formation file should exist";

            // Clean up
            file.delete();

            testsPassed++;
            Logger.info("✓ Load previous formation scenario test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Load previous formation scenario test failed", e);
        }
    }

    private static void testRemainingParticipantsScenario() {
        Logger.info("Testing Remaining Participants Scenario...");

        try {
            // UAT-005: Handle remaining participants correctly
            TeamManager tm = new TeamManager();

            // Add 13 participants (team size 5 = 2 teams + 3 remaining)
            for (int i = 0; i < 13; i++) {
                Participant p = new Participant(
                        "RemainingUser" + i,
                        "remaining" + i + "@test.com",
                        Game.CSGO,
                        6,
                        Role.SUPPORTER,
                        80
                );
                tm.addParticipant(p);
            }

            tm.setTeamSize(5);
            FormationStatistics stats = tm.formTeams();
            tm.saveTeamsToDatabase();

            // Should have remaining participants
            assert tm.hasRemainingParticipants() : "Should have remaining participants";
            assert stats.getParticipantsRemaining() == 3 : "Should have 3 remaining participants";

            // Export to CSV with remaining
            String file = "uat_remaining.csv";
            tm.saveTeamsToCSV(file);
            tm.appendRemainingParticipantsToCSV(file);

            // Verify file contains TeamID=0 entries
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            boolean hasTeamZero = false;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("0,")) {
                    hasTeamZero = true;
                    break;
                }
            }
            br.close();

            assert hasTeamZero : "File should contain TeamID=0 entries for remaining participants";

            // Clean up
            new File(file).delete();

            testsPassed++;
            Logger.info("✓ Remaining participants scenario test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Remaining participants scenario test failed", e);
        }
    }

    private static void testProfileUpdateScenario() {
        Logger.info("Testing Profile Update Scenario...");

        try {
            // UAT-006: Participant updates multiple profile fields in database
            TeamManager tm = new TeamManager();

            Participant p = new Participant(
                    "Profile Update User",
                    "profileupdate@test.com",
                    Game.DOTA2,
                    5,
                    Role.STRATEGIST,
                    80
            );
            tm.addParticipant(p);
            String id = p.getId();

            // Update email in database
            tm.updateParticipantEmail(id, "updatedemail@test.com");
            Participant updated = tm.getParticipantById(id);
            assert updated.getEmail().equals("updatedemail@test.com") : "Email should be updated in DB";

            // Update skill in database
            tm.updateParticipantSkill(id, 8);
            updated = tm.getParticipantById(id);
            assert updated.getSkillLevel() == 8 : "Skill should be updated in DB";

            // Update game in database
            tm.updateParticipantGame(id, Game.VALORANT);
            updated = tm.getParticipantById(id);
            assert updated.getGame() == Game.VALORANT : "Game should be updated in DB";

            // Update role in database
            tm.updateParticipantRole(id, Role.ATTACKER);
            updated = tm.getParticipantById(id);
            assert updated.getRole() == Role.ATTACKER : "Role should be updated in DB";

            // Clean up
            ParticipantDAO.deleteParticipant(id);

            testsPassed++;
            Logger.info("✓ Profile update scenario test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Profile update scenario test failed", e);
        }
    }

    private static void testErrorRecoveryScenario() {
        Logger.info("Testing Error Recovery Scenario...");

        try {
            // UAT-007: System handles errors gracefully
            TeamManager tm = new TeamManager();

            // Try invalid team size
            boolean exceptionThrown = false;
            try {
                tm.setTeamSize(15);
            } catch (Exception e) {
                exceptionThrown = true;
            }
            assert exceptionThrown : "Should throw exception for invalid team size";

            // Try to get non-existent participant from database
            exceptionThrown = false;
            try {
                tm.getParticipantById("NONEXISTENT999");
            } catch (Exception e) {
                exceptionThrown = true;
            }
            assert exceptionThrown : "Should throw exception for non-existent participant";

            testsPassed++;
            Logger.info("✓ Error recovery scenario test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Error recovery scenario test failed", e);
        }
    }

    private static void testDatabasePersistenceScenario() {
        Logger.info("Testing Database Persistence Scenario...");

        try {
            // UAT-008: Verify data persists in database across operations
            TeamManager tm1 = new TeamManager();

            // Add participants
            Participant p1 = new Participant("Persist1", "persist1@test.com", Game.BASKETBALL, 7, Role.DEFENDER, 85);
            Participant p2 = new Participant("Persist2", "persist2@test.com", Game.VALORANT, 6, Role.COORDINATOR, 75);

            tm1.addParticipant(p1);
            tm1.addParticipant(p2);

            String id1 = p1.getId();
            String id2 = p2.getId();

            // Create new TeamManager instance (simulating app restart)
            TeamManager tm2 = new TeamManager();

            // Verify participants still exist in database
            assert tm2.participantExists(id1) : "Participant 1 should persist in database";
            assert tm2.participantExists(id2) : "Participant 2 should persist in database";

            Participant retrieved1 = tm2.getParticipantById(id1);
            Participant retrieved2 = tm2.getParticipantById(id2);

            assert retrieved1.getName().equals("Persist1") : "Name should persist";
            assert retrieved2.getName().equals("Persist2") : "Name should persist";

            // Clean up
            ParticipantDAO.deleteParticipant(id1);
            ParticipantDAO.deleteParticipant(id2);

            testsPassed++;
            Logger.info("✓ Database persistence scenario test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Database persistence scenario test failed", e);
        }
    }

    private static void testMultipleOrganizerScenario() {
        Logger.info("Testing Multiple Organizer Scenario...");

        try {
            // UAT-009: Multiple organizers can work with same database
            TeamManager tm1 = new TeamManager();
            tm1.setCurrentOrganizer("ORG001");

            // Organizer 1 adds participants
            for (int i = 0; i < 5; i++) {
                Participant p = new Participant(
                        "Org1User" + i,
                        "org1_" + i + "@test.com",
                        Game.CHESS,
                        7,
                        Role.STRATEGIST,
                        80
                );
                tm1.addParticipant(p);
            }

            // Organizer 2 views same data
            TeamManager tm2 = new TeamManager();
            tm2.setCurrentOrganizer("ORG002");

            // Both should see all participants from database
            assert ParticipantDAO.getAllParticipants().size() >= 5 : "Both organizers should see all participants";

            testsPassed++;
            Logger.info("✓ Multiple organizer scenario test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Multiple organizer scenario test failed", e);
        }
    }

    private static void printTestResults() {
        Logger.info("\n╔════════════════════════════════════╗");
        Logger.info("║  USER ACCEPTANCE TEST RESULTS      ║");
        Logger.info("╚════════════════════════════════════╝");
        Logger.info("Tests Passed: " + testsPassed);
        Logger.info("Tests Failed: " + testsFailed);
        Logger.info("Total Tests: " + (testsPassed + testsFailed));

        if (testsFailed == 0) {
            Logger.info("✓ All user acceptance tests passed!");
            Logger.info("System is ready for production use.");
        } else {
            Logger.warning("⚠ Some user acceptance tests failed.");
            Logger.warning("Please review and fix issues before production deployment.");
        }
    }
}