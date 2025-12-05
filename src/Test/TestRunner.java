package Test;

import Log.Logger;
import Database.DatabaseConnection;

public class TestRunner {
    private static int totalTestsPassed = 0;
    private static int totalTestsFailed = 0;
    private static long startTime;

    public static void main(String[] args) {
        printHeader();

        // Test database connection first
        if (!testDatabaseConnection()) {
            Logger.error("Database connection failed. Cannot proceed with tests.");
            System.out.println("\n✗ CRITICAL ERROR: Database connection failed!");
            System.out.println("Please ensure MySQL is running and credentials are correct.");
            System.exit(1);
        }

        startTime = System.currentTimeMillis();

        try {
            // Phase 1: Unit Tests
            runTestPhase("UNIT TESTS", () -> UnitTests.main(null));

            // Phase 2: Concurrency Tests
            runTestPhase("CONCURRENCY TESTS", () -> ConcurrencyTests.main(null));

            // Phase 3: File Integrity Tests
            runTestPhase("FILE INTEGRITY TESTS", () -> FileIntegrityTests.main(null));

            // Phase 4: User Acceptance Tests
            runTestPhase("USER ACCEPTANCE TESTS", () -> UserAcceptanceTestsJava.main(null));

        } catch (Exception e) {
            Logger.error("Critical error during test execution", e);
            System.out.println("\n✗ CRITICAL ERROR: Test execution failed!");
            e.printStackTrace();
        }

        printFinalResults();
    }

    private static void printHeader() {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║                                                        ║");
        System.out.println("║       TeamMate System - Comprehensive Test Suite       ║");
        System.out.println("║                                                        ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");

        Logger.info("╔════════════════════════════════════════════════════════╗");
        Logger.info("║       TeamMate System - Comprehensive Test Suite       ║");
        Logger.info("╚════════════════════════════════════════════════════════╝\n");
    }

    private static boolean testDatabaseConnection() {
        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│   TESTING DATABASE CONNECTION          │");
        System.out.println("└─────────────────────────────────────────┘");

        Logger.info("Testing database connection...");

        boolean connected = DatabaseConnection.testConnection();

        if (connected) {
            System.out.println("✓ Database connection successful!");
            Logger.info("✓ Database connection successful!");
        } else {
            System.out.println("✗ Database connection failed!");
            Logger.error("✗ Database connection failed!");
        }

        return connected;
    }

    private static void runTestPhase(String phaseName, Runnable testRunner) {
        System.out.println("\n\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║   PHASE: " + String.format("%-45s", phaseName) + "║");
        System.out.println("╚════════════════════════════════════════════════════════╝");

        Logger.info("┌─────────────────────────────────────────┐");
        Logger.info("│   PHASE: " + phaseName);
        Logger.info("└─────────────────────────────────────────┘");

        try {
            testRunner.run();
            System.out.println("\n✓ Phase completed successfully");
        } catch (Exception e) {
            System.out.println("\n✗ Phase failed with error: " + e.getMessage());
            Logger.error("Phase " + phaseName + " failed", e);
        }
    }

    private static void printFinalResults() {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double seconds = duration / 1000.0;

        System.out.println("\n\n");
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║                                                        ║");
        System.out.println("║           TEST SUITE EXECUTION COMPLETE                ║");
        System.out.println("║                                                        ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");

        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│        EXECUTION SUMMARY                │");
        System.out.println("└─────────────────────────────────────────┘");
        System.out.println("  Total Execution Time: " + String.format("%.2f", seconds) + " seconds (" + duration + "ms)");
        System.out.println("  Average Time per Phase: " + String.format("%.2f", seconds / 4) + " seconds");

        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│        TEST PHASES COMPLETED            │");
        System.out.println("└─────────────────────────────────────────┘");
        System.out.println("  ✓ Phase 1: Unit Tests");
        System.out.println("  ✓ Phase 2: Concurrency Tests");
        System.out.println("  ✓ Phase 3: File Integrity Tests");
        System.out.println("  ✓ Phase 4: User Acceptance Tests");

        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│        DETAILED LOGS                    │");
        System.out.println("└─────────────────────────────────────────┘");
        System.out.println("  Log File: logs/teammate_system.log");
        System.out.println("  Review the log file for detailed test outcomes and any failures.");

        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│        RECOMMENDATIONS                  │");
        System.out.println("└─────────────────────────────────────────┘");
        System.out.println("  1. Check logs/teammate_system.log for detailed results");
        System.out.println("  2. Review any failed tests before deployment");
        System.out.println("  3. Ensure all database operations completed successfully");
        System.out.println("  4. Verify CSV export/import functionality");

        System.out.println("\n════════════════════════════════════════════════════════");

        // Log final results
        Logger.info("╔════════════════════════════════════════════════════════╗");
        Logger.info("║           TEST SUITE EXECUTION COMPLETE                 ║");
        Logger.info("╚════════════════════════════════════════════════════════╝");
        Logger.info("Total execution time: " + duration + "ms (" + String.format("%.2f", seconds) + " seconds)");
        Logger.info("\nAll test phases completed. Review above logs for individual test results.");
        Logger.info("For failed tests, search for '✗' in this log file.");

        System.out.println("\n✓ Test Suite Complete - Check logs for detailed results\n");
    }
}