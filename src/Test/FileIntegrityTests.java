package Test;

import Log.Logger;
import Manager.TeamManager;
import Entity.Participant;

import java.io.*;

public class FileIntegrityTests {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        Logger.info("=== Starting File Integrity Tests ===");

        testCSVCreation();
        testCSVLoading();
        testCSVCorruption();
        testMissingFile();
        testInvalidData();
        testEmptyFile();
        testSpecialCharacters();

        printTestResults();
    }

    private static void testCSVCreation() {
        Logger.info("Testing CSV Creation...");

        try {
            String testFile = "test_creation.csv";
            TeamManager tm = new TeamManager();

            // Add participants
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
                tm.addParticipant(p);
            }

            tm.setTeamSize(3);
            tm.formTeams();
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
            Logger.info("✓ CSV creation test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ CSV creation test failed", e);
        }
    }

    private static void testCSVLoading() {
        Logger.info("Testing CSV Loading...");

        try {
            String testFile = "test_loading.csv";

            // Create test CSV
            PrintWriter pw = new PrintWriter(testFile);
            pw.println("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            pw.println("P001,Alice,alice@test.com,Chess,7,Strategist,95,Leader");
            pw.println("P002,Bob,bob@test.com,FIFA,8,Attacker,80,Balanced");
            pw.println("P003,Charlie,charlie@test.com,Basketball,6,Defender,65,Thinker");
            pw.close();

            // Load CSV
            TeamManager tm = new TeamManager();
            tm.loadParticipantsFromCSV(testFile);

            // Verify loaded data
            assert tm.participantExists("P001") : "P001 should exist";
            assert tm.participantExists("P002") : "P002 should exist";
            assert tm.participantExists("P003") : "P003 should exist";

            Participant p1 = tm.getParticipantById("P001");
            assert p1.getName().equals("Alice") : "Name should be Alice";
            assert p1.getSkillLevel() == 7 : "Skill level should be 7";

            // Clean up
            new File(testFile).delete();

            testsPassed++;
            Logger.info("✓ CSV loading test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ CSV loading test failed", e);
        }
    }

    private static void testCSVCorruption() {
        Logger.info("Testing CSV Corruption Handling...");

        try {
            String testFile = "test_corrupted.csv";

            // Create corrupted CSV (missing columns)
            PrintWriter pw = new PrintWriter(testFile);
            pw.println("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            pw.println("P001,Alice,alice@test.com,Chess"); // Incomplete row
            pw.println("P002,Bob,bob@test.com,FIFA,8,Attacker,80,Balanced"); // Good row
            pw.println("P003"); // Very incomplete
            pw.close();

            // Try to load - should handle errors gracefully
            TeamManager tm = new TeamManager();
            tm.loadParticipantsFromCSV(testFile);

            // Should load only valid rows
            assert tm.participantExists("P002") : "Valid row should be loaded";

            // Clean up
            new File(testFile).delete();

            testsPassed++;
            Logger.info("✓ CSV corruption handling test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ CSV corruption handling test failed", e);
        }
    }

    private static void testMissingFile() {
        Logger.info("Testing Missing File Handling...");

        try {
            TeamManager tm = new TeamManager();

            // Try to load non-existent file
            tm.loadParticipantsFromCSV("nonexistent_file.csv");

            // Should handle gracefully without crashing
            testsPassed++;
            Logger.info("✓ Missing file handling test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Missing file handling test failed", e);
        }
    }

    private static void testInvalidData() {
        Logger.info("Testing Invalid Data Handling...");

        try {
            String testFile = "test_invalid.csv";

            // Create CSV with invalid data
            PrintWriter pw = new PrintWriter(testFile);
            pw.println("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            pw.println("P001,Alice,alice@test.com,Chess,ABC,Strategist,95,Leader"); // Invalid skill
            pw.println("P002,Bob,bob@test.com,FIFA,8,Attacker,XYZ,Balanced"); // Invalid personality score
            pw.close();

            // Try to load - should handle errors
            TeamManager tm = new TeamManager();
            tm.loadParticipantsFromCSV(testFile);

            // Clean up
            new File(testFile).delete();

            testsPassed++;
            Logger.info("✓ Invalid data handling test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Invalid data handling test failed", e);
        }
    }

    private static void testEmptyFile() {
        Logger.info("Testing Empty File Handling...");

        try {
            String testFile = "test_empty.csv";

            // Create empty CSV
            PrintWriter pw = new PrintWriter(testFile);
            pw.println("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            pw.close();

            // Load empty file
            TeamManager tm = new TeamManager();
            tm.loadParticipantsFromCSV(testFile);

            // Clean up
            new File(testFile).delete();

            testsPassed++;
            Logger.info("✓ Empty file handling test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Empty file handling test failed", e);
        }
    }

    private static void testSpecialCharacters() {
        Logger.info("Testing Special Characters in Data...");

        try {
            String testFile = "test_special.csv";

            // Create CSV with special characters
            PrintWriter pw = new PrintWriter(testFile);
            pw.println("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            pw.println("P001,O'Brien,obrien@test.com,Chess,7,Strategist,95,Leader");
            pw.println("P002,José García,jose@test.com,FIFA,8,Attacker,80,Balanced");
            pw.close();

            TeamManager tm = new TeamManager();
            tm.loadParticipantsFromCSV(testFile);

            // Verify special characters preserved
            assert tm.participantExists("P001") : "P001 should exist";
            assert tm.participantExists("P002") : "P002 should exist";

            // Clean up
            new File(testFile).delete();

            testsPassed++;
            Logger.info("✓ Special characters test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Special characters test failed", e);
        }
    }


    private static void printTestResults() {
        Logger.info("=== File Integrity Test Results ===");
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
