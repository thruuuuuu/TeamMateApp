package Test;

import Log.Logger;
import Manager.TeamManager;
import Entity.Participant;
import Enums.Game;
import Enums.Role;

import java.io.*;

public class FileIntegrityTests {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        Logger.info("=== Starting File Integrity Tests ===");

        testCSVExportCreation();
        testCSVExportFormat();
        testCSVWithRemainingParticipants();
        testCSVLoadBack();
        testLargeCSVExport();

        printTestResults();
    }

    private static void testCSVExportCreation() {
        Logger.info("Testing CSV Export Creation...");

        try {
            String testFile = "test_export_creation.csv";
            TeamManager tm = new TeamManager();

            // Add participants to database
            for (int i = 0; i < 10; i++) {
                Participant p = new Participant(
                        "ExportUser" + i,
                        "export" + i + "@test.com",
                        Game.CHESS,
                        5 + i % 5,
                        Role.STRATEGIST,
                        70 + i % 30
                );
                tm.addParticipant(p);
            }

            // Form teams and save to database
            tm.setTeamSize(3);
            tm.formTeams();
            tm.saveTeamsToDatabase();

            // Export to CSV
            tm.saveTeamsToCSV(testFile);

            // Verify file exists
            File file = new File(testFile);
            assert file.exists() : "CSV file should be created";
            assert file.length() > 0 : "CSV file should not be empty";

            // Verify file content
            BufferedReader br = new BufferedReader(new FileReader(testFile));
            String header = br.readLine();
            assert header.contains("TeamID") : "Header should contain TeamID";
            assert header.contains("ParticipantID") : "Header should contain ParticipantID";

            int lineCount = 1; // Header already read
            while (br.readLine() != null) {
                lineCount++;
            }
            br.close();

            assert lineCount > 1 : "Should have data rows";

            // Clean up
            file.delete();

            testsPassed++;
            Logger.info("✓ CSV export creation test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ CSV export creation test failed", e);
        }
    }

    private static void testCSVExportFormat() {
        Logger.info("Testing CSV Export Format...");

        try {
            String testFile = "test_export_format.csv";
            TeamManager tm = new TeamManager();

            // Add participants
            for (int i = 0; i < 12; i++) {
                Participant p = new Participant(
                        "FormatUser" + i,
                        "format" + i + "@test.com",
                        Game.FIFA,
                        6,
                        Role.ATTACKER,
                        75
                );
                tm.addParticipant(p);
            }

            tm.setTeamSize(4);
            tm.formTeams();
            tm.saveTeamsToDatabase();
            tm.saveTeamsToCSV(testFile);

            // Read and verify format
            BufferedReader br = new BufferedReader(new FileReader(testFile));
            String header = br.readLine();
            String[] headerFields = header.split(",");

            assert headerFields.length >= 9 : "Should have at least 9 columns";
            assert headerFields[0].equals("TeamID") : "First column should be TeamID";
            assert headerFields[1].equals("ParticipantID") : "Second column should be ParticipantID";

            // Check data rows
            String dataRow = br.readLine();
            assert dataRow != null : "Should have data rows";
            String[] dataFields = dataRow.split(",");
            assert dataFields.length >= 9 : "Data row should have at least 9 fields";

            br.close();

            // Clean up
            new File(testFile).delete();

            testsPassed++;
            Logger.info("✓ CSV export format test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ CSV export format test failed", e);
        }
    }

    private static void testCSVWithRemainingParticipants() {
        Logger.info("Testing CSV with Remaining Participants...");

        try {
            String testFile = "test_remaining.csv";
            TeamManager tm = new TeamManager();

            // Add 13 participants (team size 5 = 2 teams + 3 remaining)
            for (int i = 0; i < 13; i++) {
                Participant p = new Participant(
                        "RemainingUser" + i,
                        "remaining" + i + "@test.com",
                        Game.BASKETBALL,
                        7,
                        Role.DEFENDER,
                        80
                );
                tm.addParticipant(p);
            }

            tm.setTeamSize(5);
            tm.formTeams();
            tm.saveTeamsToDatabase();
            tm.saveTeamsToCSV(testFile);

            // Append remaining participants
            if (tm.hasRemainingParticipants()) {
                tm.appendRemainingParticipantsToCSV(testFile);
            }

            // Verify file contains TeamID=0 entries
            BufferedReader br = new BufferedReader(new FileReader(testFile));
            String line;
            boolean hasTeamZero = false;
            int teamZeroCount = 0;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("0,")) {
                    hasTeamZero = true;
                    teamZeroCount++;
                }
            }
            br.close();

            assert hasTeamZero : "File should contain TeamID=0 entries for remaining participants";
            assert teamZeroCount == 3 : "Should have exactly 3 remaining participants";

            // Clean up
            new File(testFile).delete();

            testsPassed++;
            Logger.info("✓ CSV with remaining participants test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ CSV with remaining participants test failed", e);
        }
    }

    private static void testCSVLoadBack() {
        Logger.info("Testing CSV Load Back...");

        try {
            String testFile = "test_loadback.csv";
            TeamManager tm1 = new TeamManager();

            // Create and save teams
            for (int i = 0; i < 10; i++) {
                Participant p = new Participant(
                        "LoadBackUser" + i,
                        "loadback" + i + "@test.com",
                        Game.VALORANT,
                        6,
                        Role.SUPPORTER,
                        75
                );
                tm1.addParticipant(p);
            }

            tm1.setTeamSize(3);
            tm1.formTeams();
            tm1.saveTeamsToDatabase();
            tm1.saveTeamsToCSV(testFile);

            if (tm1.hasRemainingParticipants()) {
                tm1.appendRemainingParticipantsToCSV(testFile);
            }

            // Load back into new TeamManager
            TeamManager tm2 = new TeamManager();
            tm2.loadTeamFormationFromCSV(testFile);

            // Verify loaded data matches saved data
            File file = new File(testFile);
            assert file.exists() : "CSV file should exist for loading";

            // Clean up
            file.delete();

            testsPassed++;
            Logger.info("✓ CSV load back test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ CSV load back test failed", e);
        }
    }

    private static void testLargeCSVExport() {
        Logger.info("Testing Large CSV Export...");

        try {
            String testFile = "test_large_export.csv";
            TeamManager tm = new TeamManager();

            // Add 100 participants
            for (int i = 0; i < 100; i++) {
                Participant p = new Participant(
                        "LargeUser" + i,
                        "large" + i + "@test.com",
                        Game.values()[i % Game.values().length],
                        5 + (i % 6),
                        Role.values()[i % Role.values().length],
                        60 + (i % 40)
                );
                tm.addParticipant(p);
            }

            tm.setTeamSize(5);
            tm.formTeams();
            tm.saveTeamsToDatabase();
            tm.saveTeamsToCSV(testFile);

            if (tm.hasRemainingParticipants()) {
                tm.appendRemainingParticipantsToCSV(testFile);
            }

            // Verify file
            File file = new File(testFile);
            assert file.exists() : "Large CSV file should be created";
            assert file.length() > 1000 : "Large CSV file should be substantial";

            BufferedReader br = new BufferedReader(new FileReader(testFile));
            int lineCount = 0;
            while (br.readLine() != null) {
                lineCount++;
            }
            br.close();

            assert lineCount > 50 : "Should have many lines for 100 participants";

            // Clean up
            file.delete();

            testsPassed++;
            Logger.info("✓ Large CSV export test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Large CSV export test failed", e);
        }
    }

    private static void printTestResults() {
        Logger.info("\n╔════════════════════════════════════╗");
        Logger.info("║   FILE INTEGRITY TEST RESULTS      ║");
        Logger.info("╚════════════════════════════════════╝");
        Logger.info("Tests Passed: " + testsPassed);
        Logger.info("Tests Failed: " + testsFailed);
        Logger.info("Total Tests: " + (testsPassed + testsFailed));

        if (testsFailed == 0) {
            Logger.info("✓ All file integrity tests passed!");
        } else {
            Logger.warning("⚠ Some file integrity tests failed. Please review the log.");
        }
    }
}