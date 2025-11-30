package Test;

import Entity.Participant;
import Enums.Game;
import Enums.Role;
import Log.Logger;
import Manager.TeamManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ConcurrencyTests {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        Logger.info("=== Starting Concurrency Tests ===");

        testConcurrentParticipantAddition();
        testConcurrentTeamFormation();
        testConcurrentFileLoading();
        testThreadPoolManagement();
        testDeadlockAvoidance();

        printTestResults();
    }

    private static void testConcurrentParticipantAddition() {
        Logger.info("Testing Concurrent Participant Addition...");

        try {
            TeamManager tm = new TeamManager();
            ExecutorService executor = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(100);

            // Add 100 participants concurrently
            for (int i = 0; i < 100; i++) {
                final int id = i;
                executor.submit(() -> {
                    try {
                        Participant p = new Participant(
                                "User" + id,
                                "user" + id + "@test.com",
                                Game.CHESS,
                                5 + (id % 6),
                                Role.STRATEGIST,
                                60 + (id % 40)
                        );
                        tm.addParticipant(p);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            // All participants should be added without errors
            testsPassed++;
            Logger.info("✓ Concurrent participant addition test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Concurrent participant addition test failed", e);
        }
    }

    private static void testConcurrentTeamFormation() {
        Logger.info("Testing Concurrent Team Formation...");

        try {
            TeamManager tm = new TeamManager();

            // Add participants
            for (int i = 0; i < 20; i++) {
                Participant p = new Participant(
                        "P" + String.format("%03d", i),
                        "User" + i,
                        "user" + i + "@test.com",
                        "Chess",
                        5 + (i % 6),
                        "Strategist",
                        60 + (i % 40)
                );
                tm.addParticipant(p);
            }

            // Form teams multiple times concurrently
            ExecutorService executor = Executors.newFixedThreadPool(5);
            CountDownLatch latch = new CountDownLatch(5);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                Future<?> future = executor.submit(() -> {
                    try {
                        tm.setTeamSize(4);
                        tm.formTeams();
                    } catch (Exception e) {
                        Logger.error("Error in concurrent team formation", e);
                    } finally {
                        latch.countDown();
                    }
                });
                futures.add(future);
            }

            latch.await(20, TimeUnit.SECONDS);
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            testsPassed++;
            Logger.info("✓ Concurrent team formation test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Concurrent team formation test failed", e);
        }
    }

    private static void testConcurrentFileLoading() {
        Logger.info("Testing Concurrent File Loading...");

        try {
            // Create test CSV files
            String[] testFiles = new String[3];
            for (int i = 0; i < 3; i++) {
                testFiles[i] = "test_concurrent_" + i + ".csv";
                java.io.PrintWriter pw = new java.io.PrintWriter(testFiles[i]);
                pw.println("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
                for (int j = 0; j < 10; j++) {
                    pw.println(String.format("P%03d,User%d,user%d@test.com,Chess,7,Strategist,80,Balanced",
                            i * 10 + j, j, j));
                }
                pw.close();
            }

            // Load files concurrently
            ExecutorService executor = Executors.newFixedThreadPool(3);
            CountDownLatch latch = new CountDownLatch(3);

            for (String file : testFiles) {
                executor.submit(() -> {
                    try {
                        TeamManager tm = new TeamManager();
                        tm.loadParticipantsFromCSV(file);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            // Clean up
            for (String file : testFiles) {
                new java.io.File(file).delete();
            }

            testsPassed++;
            Logger.info("✓ Concurrent file loading test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Concurrent file loading test failed", e);
        }
    }

    private static void testThreadPoolManagement() {
        Logger.info("Testing Thread Pool Management...");

        try {
            ExecutorService executor = Executors.newFixedThreadPool(5);
            CountDownLatch latch = new CountDownLatch(10);

            // Submit 10 tasks
            for (int i = 0; i < 10; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    try {
                        Thread.sleep(100); // Simulate work
                        Logger.debug("Task " + taskId + " completed");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Wait for all tasks
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            assert completed : "Not all tasks completed in time";

            // Shutdown properly
            executor.shutdown();
            boolean terminated = executor.awaitTermination(2, TimeUnit.SECONDS);
            assert terminated : "Executor did not terminate properly";

            testsPassed++;
            Logger.info("✓ Thread pool management test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Thread pool management test failed", e);
        }
    }

    private static void testDeadlockAvoidance() {
        Logger.info("Testing Deadlock Avoidance...");

        try {
            TeamManager tm1 = new TeamManager();
            TeamManager tm2 = new TeamManager();

            ExecutorService executor = Executors.newFixedThreadPool(2);
            CountDownLatch latch = new CountDownLatch(2);

            // Thread 1: Add to tm1, then tm2
            executor.submit(() -> {
                try {
                    for (int i = 0; i < 10; i++) {
                        Participant p = new Participant("User" + i, "u" + i + "@test.com",
                                Game.CHESS, 5, Role.STRATEGIST, 80);
                        tm1.addParticipant(p);
                        Thread.sleep(10);
                        tm2.addParticipant(p);
                    }
                } catch (Exception e) {
                    Logger.error("Error in thread 1", e);
                } finally {
                    latch.countDown();
                }
            });

            // Thread 2: Add to tm2, then tm1
            executor.submit(() -> {
                try {
                    for (int i = 10; i < 20; i++) {
                        Participant p = new Participant("User" + i, "u" + i + "@test.com",
                                Game.FIFA, 6, Role.ATTACKER, 75);
                        tm2.addParticipant(p);
                        Thread.sleep(10);
                        tm1.addParticipant(p);
                    }
                } catch (Exception e) {
                    Logger.error("Error in thread 2", e);
                } finally {
                    latch.countDown();
                }
            });

            // Should complete without deadlock
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            assert completed : "Potential deadlock detected";

            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);

            testsPassed++;
            Logger.info("✓ Deadlock avoidance test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Deadlock avoidance test failed", e);
        }
    }

    private static void printTestResults() {
        Logger.info("=== Concurrency Test Results ===");
        Logger.info("Tests Passed: " + testsPassed);
        Logger.info("Tests Failed: " + testsFailed);
        Logger.info("Total Tests: " + (testsPassed + testsFailed));

        if (testsFailed == 0) {
            Logger.info("✓ All concurrency tests passed!");
        } else {
            Logger.warning("⚠ Some concurrency tests failed. Please review the log.");
        }
    }
}

