package Test;

import Log.Logger;

public class TestRunner {
    public static void main(String[] args) {
        Logger.info("╔════════════════════════════════════════════════════════╗");
        Logger.info("║       TeamMate System - Comprehensive Test Suite       ║");
        Logger.info("╚════════════════════════════════════════════════════════╝\n");

        long startTime = System.currentTimeMillis();

        try {
            // 1. Unit Tests
            Logger.info("┌─────────────────────────────────────────┐");
            Logger.info("│   PHASE 1: UNIT TESTS                   │");
            Logger.info("└─────────────────────────────────────────┘");
            UnitTests.main(null);

            // 2. Concurrency Tests
            Logger.info("┌─────────────────────────────────────────┐");
            Logger.info("│   PHASE 2: CONCURRENCY TESTS            │");
            Logger.info("└─────────────────────────────────────────┘");
            ConcurrencyTests.main(null);

            // 3. File Integrity Tests
            Logger.info("┌─────────────────────────────────────────┐");
            Logger.info("│   PHASE 3: FILE INTEGRITY TESTS         │");
            Logger.info("└─────────────────────────────────────────┘");
            FileIntegrityTests.main(null);

            // 4. User Acceptance Tests
            Logger.info("┌─────────────────────────────────────────┐");
            Logger.info("│   PHASE 4: USER ACCEPTANCE TESTS        │");
            Logger.info("└─────────────────────────────────────────┘");
            UserAcceptanceTestJava.main(null);

        } catch (Exception e) {
            Logger.error("Critical error during test execution", e);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        Logger.info("╔════════════════════════════════════════════════════════╗");
        Logger.info("║           TEST SUITE EXECUTION COMPLETE                 ║");
        Logger.info("╚════════════════════════════════════════════════════════╝");
        Logger.info("Total execution time: " + duration + "ms (" + (duration / 1000.0) + " seconds)");
        Logger.info("\nDetailed test results have been logged to: logs/teammate_system.log");
        Logger.info("\nReview the log file for detailed test outcomes and any failures.");
    }
}
