package Test;

import Entity.Participant;
import Entity.Team;
import Enums.Game;
import Enums.Role;
import Enums.PersonalityType;
import Exceptions.*;
import Log.Logger;
import Manager.TeamManager;
import Database.ParticipantDAO;
import Database.AuthenticationService;

public class UnitTests {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        Logger.info("=== Starting Unit Tests ===");

        testParticipantCreation();
        testPersonalityClassification();
        testTeamCreation();
        testEnumFunctionality();
        testExceptionHandling();
        testTeamManagerBasics();
        testDatabaseOperations();
        testPasswordGeneration();

        printTestResults();
    }

    private static void testParticipantCreation() {
        Logger.info("Testing Participant Creation...");

        try {
            // Test 1: Create participant with all fields
            Participant p1 = new Participant("P001", "John Doe", "john@test.com",
                    "Chess", 7, "Strategist", 95);

            assert p1.getId().equals("P001") : "ID mismatch";
            assert p1.getName().equals("John Doe") : "Name mismatch";
            assert p1.getEmail().equals("john@test.com") : "Email mismatch";
            assert p1.getGame() == Game.CHESS : "Game mismatch";
            assert p1.getSkillLevel() == 7 : "Skill level mismatch";
            assert p1.getRole() == Role.STRATEGIST : "Role mismatch";
            assert p1.getPersonalityScore() == 95 : "Personality score mismatch";
            assert p1.getPersonalityType() == PersonalityType.LEADER : "Personality type mismatch";

            testsPassed++;
            Logger.info("✓ Participant creation test passed");

        } catch (AssertionError | Exception e) {
            testsFailed++;
            Logger.error("✗ Participant creation test failed", (Exception) e);
        }

        try {
            // Test 2: Create participant without ID (auto-generate)
            Participant p2 = new Participant("Jane Smith", "jane@test.com",
                    Game.FIFA, 8, Role.ATTACKER, 80);

            assert p2.getId() != null : "ID should be auto-generated";
            assert p2.getId().startsWith("P") : "ID should start with P";
            assert p2.getPersonalityType() == PersonalityType.BALANCED : "Should be Balanced";

            testsPassed++;
            Logger.info("✓ Auto-generated ID test passed");

        } catch (AssertionError | Exception e) {
            testsFailed++;
            Logger.error("✗ Auto-generated ID test failed", (Exception) e);
        }
    }

    private static void testPersonalityClassification() {
        Logger.info("Testing Personality Classification...");

        try {
            // Leader: 90-100
            Participant leader = new Participant("P001", "Leader", "l@test.com",
                    "Chess", 5, "Strategist", 95);
            assert leader.getPersonalityType() == PersonalityType.LEADER : "Should be Leader";

            // Balanced: 70-89
            Participant balanced = new Participant("P002", "Balanced", "b@test.com",
                    "FIFA", 5, "Attacker", 80);
            assert balanced.getPersonalityType() == PersonalityType.BALANCED : "Should be Balanced";

            // Thinker: 50-69
            Participant thinker = new Participant("P003", "Thinker", "t@test.com",
                    "Basketball", 5, "Defender", 60);
            assert thinker.getPersonalityType() == PersonalityType.THINKER : "Should be Thinker";

            // Edge cases
            Participant edge1 = new Participant("P004", "Edge90", "e1@test.com",
                    "Chess", 5, "Supporter", 90);
            assert edge1.getPersonalityType() == PersonalityType.LEADER : "90 should be Leader";

            Participant edge2 = new Participant("P005", "Edge70", "e2@test.com",
                    "FIFA", 5, "Coordinator", 70);
            assert edge2.getPersonalityType() == PersonalityType.BALANCED : "70 should be Balanced";

            testsPassed++;
            Logger.info("✓ Personality classification test passed");

        } catch (AssertionError | Exception e) {
            testsFailed++;
            Logger.error("✗ Personality classification test failed", (Exception) e);
        }
    }

    private static void testTeamCreation() {
        Logger.info("Testing Team Creation...");

        try {
            Team team = new Team(1);

            Participant p1 = new Participant("P001", "Member1", "m1@test.com",
                    "Chess", 7, "Strategist", 95);
            Participant p2 = new Participant("P002", "Member2", "m2@test.com",
                    "FIFA", 8, "Attacker", 80);
            Participant p3 = new Participant("P003", "Member3", "m3@test.com",
                    "Basketball", 6, "Defender", 70);

            team.addMember(p1);
            team.addMember(p2);
            team.addMember(p3);

            assert team.getTeamId() == 1 : "Team ID mismatch";
            assert team.getSize() == 3 : "Team size mismatch";
            assert team.getMembers().size() == 3 : "Members count mismatch";

            double expectedAvg = (7 + 8 + 6) / 3.0;
            assert Math.abs(team.getAverageSkill() - expectedAvg) < 0.01 : "Average skill calculation error";

            assert team.getRoleDiversity() == 3 : "Role diversity should be 3";

            testsPassed++;
            Logger.info("✓ Team creation test passed");

        } catch (AssertionError | Exception e) {
            testsFailed++;
            Logger.error("✗ Team creation test failed", (Exception) e);
        }
    }

    private static void testEnumFunctionality() {
        Logger.info("Testing Enum Functionality...");

        try {
            // Test Game enum
            assert Game.fromInt(1) == Game.CHESS : "Game fromInt(1) should be CHESS";
            assert Game.fromInt(2) == Game.FIFA : "Game fromInt(2) should be FIFA";
            assert Game.CHESS.getDisplayName().equals("Chess") : "Chess display name mismatch";
            assert Game.BASKETBALL.getDisplayName().equals("Basketball") : "Basketball display name mismatch";

            // Test Role enum
            assert Role.fromInt(1) == Role.STRATEGIST : "Role fromInt(1) should be STRATEGIST";
            assert Role.fromInt(2) == Role.ATTACKER : "Role fromInt(2) should be ATTACKER";
            assert Role.STRATEGIST.getDisplayName().equals("Strategist") : "Strategist display name mismatch";

            // Test PersonalityType classification
            assert PersonalityType.classify(95) == PersonalityType.LEADER : "95 should classify as Leader";
            assert PersonalityType.classify(80) == PersonalityType.BALANCED : "80 should classify as Balanced";
            assert PersonalityType.classify(60) == PersonalityType.THINKER : "60 should classify as Thinker";

            testsPassed++;
            Logger.info("✓ Enum functionality test passed");

        } catch (AssertionError | Exception e) {
            testsFailed++;
            Logger.error("✗ Enum functionality test failed", (Exception) e);
        }
    }

    private static void testExceptionHandling() {
        Logger.info("Testing Exception Handling...");

        try {
            TeamManager tm = new TeamManager();

            // Test InvalidTeamSizeException
            boolean exceptionThrown = false;
            try {
                tm.setTeamSize(2); // Should throw exception
            } catch (InvalidTeamSizeException e) {
                exceptionThrown = true;
            }
            assert exceptionThrown : "Should throw InvalidTeamSizeException for size < 3";

            exceptionThrown = false;
            try {
                tm.setTeamSize(11); // Should throw exception
            } catch (InvalidTeamSizeException e) {
                exceptionThrown = true;
            }
            assert exceptionThrown : "Should throw InvalidTeamSizeException for size > 10";

            // Test ParticipantNotFoundException
            exceptionThrown = false;
            try {
                tm.getParticipantById("NONEXISTENT999");
            } catch (ParticipantNotFoundException e) {
                exceptionThrown = true;
            }
            assert exceptionThrown : "Should throw ParticipantNotFoundException";

            testsPassed++;
            Logger.info("✓ Exception handling test passed");

        } catch (AssertionError | Exception e) {
            testsFailed++;
            Logger.error("✗ Exception handling test failed", (Exception) e);
        }
    }

    private static void testTeamManagerBasics() {
        Logger.info("Testing TeamManager Basics...");

        try {
            TeamManager tm = new TeamManager();

            // Add participant
            Participant p1 = new Participant("Test User", "testmanager@test.com",
                    Game.CHESS, 7, Role.STRATEGIST, 95);
            tm.addParticipant(p1);

            assert tm.participantExists(p1.getId()) : "Participant should exist in database";

            Participant retrieved = tm.getParticipantById(p1.getId());
            assert retrieved.getName().equals("Test User") : "Retrieved participant name mismatch";

            testsPassed++;
            Logger.info("✓ TeamManager basics test passed");

        } catch (AssertionError | Exception e) {
            testsFailed++;
            Logger.error("✗ TeamManager basics test failed", (Exception) e);
        }
    }

    private static void testDatabaseOperations() {
        Logger.info("Testing Database Operations...");

        try {
            // Test participant insertion
            Participant p = new Participant("DB Test User", "dbtest@test.com",
                    Game.VALORANT, 8, Role.ATTACKER, 85);

            String password = AuthenticationService.generateParticipantPassword(p.getId());
            boolean inserted = ParticipantDAO.insertParticipant(p, password);

            assert inserted : "Participant should be inserted into database";

            // Test retrieval
            Participant retrieved = ParticipantDAO.getParticipantById(p.getId());
            assert retrieved != null : "Should retrieve participant from database";
            assert retrieved.getName().equals("DB Test User") : "Retrieved name should match";

            // Test update
            boolean updated = ParticipantDAO.updateParticipantEmail(p.getId(), "newemail@test.com");
            assert updated : "Email should be updated";

            Participant afterUpdate = ParticipantDAO.getParticipantById(p.getId());
            assert afterUpdate.getEmail().equals("newemail@test.com") : "Email should be updated in DB";

            // Clean up
            ParticipantDAO.deleteParticipant(p.getId());

            testsPassed++;
            Logger.info("✓ Database operations test passed");

        } catch (AssertionError | Exception e) {
            testsFailed++;
            Logger.error("✗ Database operations test failed", (Exception) e);
        }
    }

    private static void testPasswordGeneration() {
        Logger.info("Testing Password Generation...");

        try {
            String participantId = "P12345";
            String password = AuthenticationService.generateParticipantPassword(participantId);

            assert password != null : "Password should not be null";
            assert password.equals("P12345-123") : "Password format should be ID-123";
            assert password.contains(participantId) : "Password should contain participant ID";

            testsPassed++;
            Logger.info("✓ Password generation test passed");

        } catch (AssertionError | Exception e) {
            testsFailed++;
            Logger.error("✗ Password generation test failed", (Exception) e);
        }
    }

    private static void printTestResults() {
        Logger.info("\n╔════════════════════════════════════╗");
        Logger.info("║       UNIT TEST RESULTS            ║");
        Logger.info("╚════════════════════════════════════╝");
        Logger.info("Tests Passed: " + testsPassed);
        Logger.info("Tests Failed: " + testsFailed);
        Logger.info("Total Tests: " + (testsPassed + testsFailed));
        Logger.info("Success Rate: " + String.format("%.2f%%",
                (testsPassed * 100.0) / (testsPassed + testsFailed)));

        if (testsFailed == 0) {
            Logger.info("✓ All unit tests passed!");
        } else {
            Logger.warning("⚠ Some tests failed. Please review the log.");
        }
    }
}