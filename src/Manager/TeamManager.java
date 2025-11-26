package Manager;

import Entity.Participant;
import Entity.Team;
import Exceptions.*;
import Threads.*;
import Main.FormationStatistics;

import java.util.*;
import java.util.concurrent.*;

public class TeamManager {
    private final List<Participant> participants;
    private final List<Team> formedTeams;
    private final List<Participant> remainingParticipants;
    private int teamSize = 5; // Default team size
    private int nextParticipantId = 1000;

    public TeamManager() {
        this.participants = new ArrayList<>();
        this.formedTeams = new ArrayList<>();
        this.remainingParticipants = new ArrayList<>();
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
    }

    public void setTeamSize(int size) throws InvalidTeamSizeException {
        if (size < 3 || size > 10) {
            throw new InvalidTeamSizeException("Team size must be between 3 and 10");
        }
        this.teamSize = size;
    }

    public void loadParticipantsFromCSV(String filePath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        CSVLoaderThread loader = new CSVLoaderThread(filePath);
        Future<List<Participant>> future = executor.submit(loader);

        try {
            List<Participant> loadedParticipants = future.get();
            participants.clear();
            participants.addAll(loadedParticipants);

            System.out.println("\n✓ Successfully loaded " + participants.size() + " participants from " + filePath);

            // Update next ID counter
            for (Participant p : participants) {
                String id = p.getId();
                if (id.startsWith("P")) {
                    try {
                        int idNum = Integer.parseInt(id.substring(1));
                        if (idNum >= nextParticipantId) {
                            nextParticipantId = idNum + 1;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore invalid ID format
                    }
                }
            }

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            System.out.println("✗ Error: " + cause.getMessage());
        } catch (InterruptedException e) {
            System.out.println("✗ Error: Loading was interrupted");
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }

    public FormationStatistics formTeams() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        TeamFormationThread formationThread = new TeamFormationThread(participants, teamSize);
        Future<List<Team>> future = executor.submit(formationThread);

        FormationStatistics stats = null;

        try {
            System.out.println("\n⏳ Forming teams...");

            int totalParticipants = participants.size();
            List<Team> teams = future.get();
            formedTeams.clear();
            formedTeams.addAll(teams);

            // Calculate remaining participants
            remainingParticipants.clear();
            Set<Participant> assignedParticipants = new HashSet<>();
            for (Team team : formedTeams) {
                assignedParticipants.addAll(team.getMembers());
            }

            for (Participant p : participants) {
                if (!assignedParticipants.contains(p)) {
                    remainingParticipants.add(p);
                }
            }

            int teamsFormed = formedTeams.size();
            int participantsAssigned = assignedParticipants.size();
            int participantsRemaining = remainingParticipants.size();

            stats = new FormationStatistics(totalParticipants, teamsFormed, participantsAssigned, participantsRemaining, teamSize);

            System.out.println("✓ Successfully formed " + teamsFormed + " teams!");

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            System.out.println("✗ Error: " + cause.getMessage());
        } catch (InterruptedException e) {
            System.out.println("✗ Error: Team formation was interrupted");
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        return stats;
    }

    public void saveTeamsToCSV(String filePath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        TeamSaverThread saver = new TeamSaverThread(formedTeams, filePath);
        Future<Boolean> future = executor.submit(saver);

        try {
            Boolean success = future.get();
            if (success) {
                System.out.println("\n✓ Teams saved successfully to " + filePath);
            }
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            System.out.println("✗ Error: " + cause.getMessage());
        } catch (InterruptedException e) {
            System.out.println("✗ Error: Saving was interrupted");
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }

    public void viewAllParticipants() {
        if (participants.isEmpty()) {
            System.out.println("\n✗ No participants available.");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  ALL PARTICIPANTS (" + participants.size() + " total)");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════════════════╝");

        for (Participant p : participants) {
            System.out.println("  " + p.toString());
        }
    }

    public void viewFormedTeams() {
        if (formedTeams.isEmpty()) {
            System.out.println("\n✗ No teams formed yet. Please form teams first.");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  FORMED TEAMS (" + formedTeams.size() + " teams)");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════════════════╝");

        for (Team team : formedTeams) {
            System.out.println(team.toString());
        }

        // Show remaining participants
        if (!remainingParticipants.isEmpty()) {
            System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║  REMAINING PARTICIPANTS (" + remainingParticipants.size() + " unassigned)");
            System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════════════════╝");

            for (Participant p : remainingParticipants) {
                System.out.println("  " + p.toString());
            }
        }
    }

    public void viewParticipantInfo(String name) throws ParticipantNotFoundException {
        Participant participant = participants.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (participant == null) {
            throw new ParticipantNotFoundException("Participant not found: " + name);
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  PARTICIPANT INFORMATION");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("  " + participant.toString());

        // Check if in a team
        for (Team team : formedTeams) {
            if (team.getMembers().contains(participant)) {
                System.out.println("\n  ✓ Assigned to Team " + team.getTeamId());
                return;
            }
        }
        System.out.println("\n  ⚠ Not yet assigned to a team");
    }

    public String generateNextParticipantId() {
        return "P" + String.format("%03d", nextParticipantId++);
    }
}