package Test;

import Log.Logger;
import Manager.TeamManager;
import Entity.Participant;
import Enums.Game;
import Enums.Role;
import Main.FormationStatistics;

import java.io.*;

public class UserAcceptanceTestJava {
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

        printTestResults();
    }

    private static void testCompleteOrganizerWorkflow() {
        Logger.info("Testing Complete Organizer Workflow...");

        try {
            // UAT-001: Organizer uploads participants, forms teams, and saves
            TeamManager tm = new TeamManager();

            // Step 1: Create sample CSV
            String csvFile = "uat_participants.csv";
            createSampleCSV(csvFile, 15);

            // Step 2: Upload CSV
            Logger.info("Step 1: Uploading participants CSV");
            tm.loadParticipantsFromCSV(csvFile);
            assert tm.participantExists("P000") : "Participants should be loaded";

            // Step 3: Set team size
            Logger.info("Step 2: Setting team size");
            tm.setTeamSize(5);

            // Step 4: Form teams
            Logger.info("Step 3: Forming teams");
            FormationStatistics stats = tm.formTeams();
            assert stats != null : "Statistics should be returned";

            // Step 5: Save teams
            Logger.info("Step 4: Saving teams");
            String outputFile = "uat_teams.csv";
            tm.saveTeamsToCSV(outputFile);

            File teamFile = new File(outputFile);
            assert teamFile.exists() : "Team file should be created";

            // Step 6: Save remaining participants
            Logger.info("Step 5: Saving remaining participants");
            if (tm.hasRemainingParticipants()) {
                tm.appendRemainingParticipantsToCSV(outputFile);
            }

            // Clean up
            new File(csvFile).delete();
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
                    "John Doe",
                    "john@test.com",
                    Game.CHESS,
                    7,
                    Role.STRATEGIST,
                    85
            );
            tm.addParticipant(p);
            String participantId = p.getId();

            // Step 2: Verify registration
            Logger.info("Step 2: Verifying registration");
            assert tm.participantExists(participantId) : "Participant should exist";

            // Step 3: Check participant info
            Logger.info("Step 3: Checking participant info");
            Participant retrieved = tm.getParticipantById(participantId);
            assert retrieved.getName().equals("John Doe") : "Name should match";
            assert retrieved.getEmail().equals("john@test.com") : "Email should match";

            // Step 4: Update email
            Logger.info("Step 4: Updating email");
            tm.updateParticipantEmail(participantId, "newemail@test.com");
            Participant updated = tm.getParticipantById(participantId);
            assert updated.getEmail().equals("newemail@test.com") : "Email should be updated";

            // Step 5: Update skill level
            Logger.info("Step 5: Updating skill level");
            tm.updateParticipantSkill(participantId, 9);
            updated = tm.getParticipantById(participantId);
            assert updated.getSkillLevel() == 9 : "Skill should be updated";

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

            // Add diverse participants
            tm.addParticipant(new Participant("P001", "Leader1", "l1@test.com", "Chess", 8, "Strategist", 95));
            tm.addParticipant(new Participant("P002", "Balanced1", "b1@test.com", "FIFA", 7, "Attacker", 80));
            tm.addParticipant(new Participant("P003", "Thinker1", "t1@test.com", "Basketball", 6, "Defender", 65));
            tm.addParticipant(new Participant("P004", "Leader2", "l2@test.com", "CS:GO", 9, "Supporter", 92));
            tm.addParticipant(new Participant("P005", "Balanced2", "b2@test.com", "DOTA 2", 7, "Coordinator", 75));
            tm.addParticipant(new Participant("P006", "Thinker2", "t2@test.com", "Valorant", 5, "Strategist", 60));
            tm.addParticipant(new Participant("P007", "Leader3", "l3@test.com", "Chess", 8, "Attacker", 90));
            tm.addParticipant(new Participant("P008", "Balanced3", "b3@test.com", "FIFA", 6, "Defender", 78));
            tm.addParticipant(new Participant("P009", "Thinker3", "t3@test.com", "Basketball", 7, "Supporter", 62));

            // Form teams of 3
            tm.setTeamSize(3);
            FormationStatistics stats = tm.formTeams();

            assert stats != null : "Statistics should be generated";
            Logger.info("Formed " + stats + " teams");

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
            // UAT-004: Save and load a team formation
            TeamManager tm1 = new TeamManager();

            // Create and save formation
            for (int i = 0; i < 10; i++) {
                Participant p = new Participant(
                        "P" + String.format("%03d", i),
                        "User" + i,
                        "user" + i + "@test.com",
                        "Chess",
                        5 + i % 5,
                        "Strategist",
                        70 + i % 30
                );
                tm1.addParticipant(p);
            }

            tm1.setTeamSize(3);
            tm1.formTeams();

            String formationFile = "uat_formation.csv";
            tm1.saveTeamsToCSV(formationFile);
            if (tm1.hasRemainingParticipants()) {
                tm1.appendRemainingParticipantsToCSV(formationFile);
            }

            // Load formation
            TeamManager tm2 = new TeamManager();
            tm2.loadTeamFormationFromCSV(formationFile);

            // Verify loaded data
            assert tm2.participantExists("P000") : "Participants should be loaded";
            assert tm2.participantExists("P009") : "All participants should be loaded";

            // Clean up
            new File(formationFile).delete();

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
                        "P" + String.format("%03d", i),
                        "User" + i,
                        "user" + i + "@test.com",
                        "Chess",
                        5,
                        "Strategist",
                        80
                );
                tm.addParticipant(p);
            }

            tm.setTeamSize(5);
            FormationStatistics stats = tm.formTeams();

            // Should have 2 teams and 3 remaining
            assert tm.hasRemainingParticipants() : "Should have remaining participants";

            // Save with remaining
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

            assert hasTeamZero : "File should contain TeamID=0 entries";

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
            // UAT-006: Participant updates multiple profile fields
            TeamManager tm = new TeamManager();

            Participant p = new Participant(
                    "Test User",
                    "test@test.com",
                    Game.CHESS,
                    5,
                    Role.STRATEGIST,
                    80
            );
            tm.addParticipant(p);
            String id = p.getId();

            // Update email
            tm.updateParticipantEmail(id, "newemail@test.com");
            Participant updated = tm.getParticipantById(id);
            assert updated.getEmail().equals("newemail@test.com") : "Email should be updated";

            // Update skill
            tm.updateParticipantSkill(id, 8);
            updated = tm.getParticipantById(id);
            assert updated.getSkillLevel() == 8 : "Skill should be updated";

            // Update game
            tm.updateParticipantGame(id, Game.FIFA);
            updated = tm.getParticipantById(id);
            assert updated.getGame() == Game.FIFA : "Game should be updated";

            // Update role
            tm.updateParticipantRole(id, Role.ATTACKER);
            updated = tm.getParticipantById(id);
            assert updated.getRole() == Role.ATTACKER : "Role should be updated";

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

            // Try to form teams with no participants
            FormationStatistics stats = tm.formTeams();
            // Should handle gracefully

            // Try invalid team size
            boolean exceptionThrown = false;
            try {
                tm.setTeamSize(15);
            } catch (Exception e) {
                exceptionThrown = true;
            }
            assert exceptionThrown : "Should throw exception for invalid team size";

            // Try to get non-existent participant
            exceptionThrown = false;
            try {
                tm.getParticipantById("INVALID");
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

    private static void createSampleCSV(String filename, int count) throws IOException {
        PrintWriter pw = new PrintWriter(filename);
        pw.println("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
        for (int i = 0; i < count; i++) {
            pw.println(String.format("P%03d,User%d,user%d@test.com,Chess,%d,Strategist,%d,Balanced",
                    i, i, i, 5 + (i % 5), 70 + (i % 20)));
        }
        pw.close();
    }

    private static void printTestResults() {
        Logger.info("=== User Acceptance Test Results ===");
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
