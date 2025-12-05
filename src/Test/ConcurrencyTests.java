package Test;

import Entity.Participant;
import Enums.Game;
import Enums.Role;
import Log.Logger;
import Manager.TeamManager;
import Database.ParticipantDAO;

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
        testConcurrentDatabaseWrites();
        testThreadPoolManagement();
        testDeadlockAvoidance();

        printTestResults();
    }

    private static void testConcurrentParticipantAddition() {
        Logger.info("Testing Concurrent Participant Addition...");

        try {
            TeamManager tm = new TeamManager();
            ExecutorService executor = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(50);

            // Add 50 participants concurrently to database
            for (int i = 0; i < 50; i++) {
                final int id = i;
                executor.submit(() -> {
                    try {
                        Participant p = new Participant(
                                "ConcurrentUser" + id,
                                "concurrent" + id + "@test.com",
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

            boolean completed = latch.await(30, TimeUnit.SECONDS);
            assert completed : "All participants should be added within timeout";

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

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

            // Add participants to database
            for (int i = 0; i < 20; i++) {
                Participant p = new Participant(
                        "TeamFormUser" + i,
                        "teamform" + i + "@test.com",
                        Game.FIFA,
                        5 + (i % 6),
                        Role.ATTACKER,
                        60 + (i % 40)
                );
                tm.addParticipant(p);
            }

            // Form teams multiple times concurrently (stress test)
            ExecutorService executor = Executors.newFixedThreadPool(3);
            CountDownLatch latch = new CountDownLatch(3);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < 3; i++) {
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

            boolean completed = latch.await(30, TimeUnit.SECONDS);
            assert completed : "All team formations should complete within timeout";

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            testsPassed++;
            Logger.info("✓ Concurrent team formation test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Concurrent team formation test failed", e);
        }
    }

    private static void testConcurrentDatabaseWrites() {
        Logger.info("Testing Concurrent Database Writes...");

        try {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(30);

            // Concurrent writes to database
            for (int i = 0; i < 30; i++) {
                final int id = i;
                executor.submit(() -> {
                    try {
                        Participant p = new Participant(
                                "DBWrite" + id,
                                "dbwrite" + id + "@test.com",
                                Game.BASKETBALL,
                                7,
                                Role.DEFENDER,
                                80
                        );

                        TeamManager tm = new TeamManager();
                        tm.addParticipant(p);

                        // Verify written data
                        Participant retrieved = ParticipantDAO.getParticipantById(p.getId());
                        assert retrieved != null : "Participant should be in database";

                    } catch (Exception e) {
                        Logger.error("Error in concurrent DB write", e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean completed = latch.await(30, TimeUnit.SECONDS);
            assert completed : "All database writes should complete";

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            testsPassed++;
            Logger.info("✓ Concurrent database writes test passed");

        } catch (Exception e) {
            testsFailed++;
            Logger.error("✗ Concurrent database writes test failed", e);
        }
    }

    private static void testThreadPoolManagement() {
        Logger.info("Testing Thread Pool Management...");

        try {
            ExecutorService executor = Executors.newFixedThreadPool(5);
            CountDownLatch latch = new CountDownLatch(15);

            // Submit 15 tasks to 5-thread pool
            for (int i = 0; i < 15; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    try {
                        Thread.sleep(50); // Simulate work
                        Logger.debug("Task " + taskId + " completed");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Wait for all tasks
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            assert completed : "All tasks should complete in time";

            // Shutdown properly
            executor.shutdown();
            boolean terminated = executor.awaitTermination(3, TimeUnit.SECONDS);
            assert terminated : "Executor should terminate properly";

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

            // Thread 1: Add to tm1, then read from database
            executor.submit(() -> {
                try {
                    for (int i = 0; i < 10; i++) {
                        Participant p = new Participant(
                                "Deadlock1_" + i,
                                "dl1_" + i + "@test.com",
                                Game.CSGO,
                                5,
                                Role.SUPPORTER,
                                80
                        );
                        tm1.addParticipant(p);
                        Thread.sleep(10);

                        // Read from database
                        ParticipantDAO.getParticipantById(p.getId());
                    }
                } catch (Exception e) {
                    Logger.error("Error in thread 1", e);
                } finally {
                    latch.countDown();
                }
            });

            // Thread 2: Add to tm2, then read from database
            executor.submit(() -> {
                try {
                    for (int i = 10; i < 20; i++) {
                        Participant p = new Participant(
                                "Deadlock2_" + i,
                                "dl2_" + i + "@test.com",
                                Game.DOTA2,
                                6,
                                Role.COORDINATOR,
                                75
                        );
                        tm2.addParticipant(p);
                        Thread.sleep(10);

                        // Read from database
                        ParticipantDAO.getParticipantById(p.getId());
                    }
                } catch (Exception e) {
                    Logger.error("Error in thread 2", e);
                } finally {
                    latch.countDown();
                }
            });

            // Should complete without deadlock
            boolean completed = latch.await(20, TimeUnit.SECONDS);
            assert completed : "Should complete without deadlock";

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
        Logger.info("\n╔════════════════════════════════════╗");
        Logger.info("║   CONCURRENCY TEST RESULTS         ║");
        Logger.info("╚════════════════════════════════════╝");
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