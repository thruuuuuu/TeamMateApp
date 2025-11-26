package Manager;

import Entity.Participant;
import Entity.Team;
import Enums.Game;
import Enums.Role;
import Exceptions.*;
import Threads.*;
import Main.FormationStatistics;

import java.util.*;
import java.util.concurrent.*;

public class TeamManager {
    private final List<Participant> participants;
    private final List<Team> formedTeams;
    private final List<Participant> remainingParticipants;
    private int teamSize = 5;
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

    // Check if participant exists by ID
    public boolean participantExists(String participantId) {
        return participants.stream()
                .anyMatch(p -> p.getId().equalsIgnoreCase(participantId));
    }

    // Get participant by ID
    public Participant getParticipantById(String participantId) throws ParticipantNotFoundException {
        return participants.stream()
                .filter(p -> p.getId().equalsIgnoreCase(participantId))
                .findFirst()
                .orElseThrow(() -> new ParticipantNotFoundException("Participant not found: " + participantId));
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

            for (Participant p : participants) {
                String id = p.getId();
                if (id.startsWith("P")) {
                    try {
                        int idNum = Integer.parseInt(id.substring(1));
                        if (idNum >= nextParticipantId) {
                            nextParticipantId = idNum + 1;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            System.out.println("Error: " + cause.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Error: Loading was interrupted");
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
            System.out.println("\n Forming teams...");

            int totalParticipants = participants.size();
            List<Team> teams = future.get();
            formedTeams.clear();
            formedTeams.addAll(teams);

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
                System.out.println("\nTeams saved successfully to " + filePath);
            }
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            System.out.println("Error: " + cause.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Error: Saving was interrupted");
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }

    public void viewAllParticipants() {
        if (participants.isEmpty()) {
            System.out.println("\nNo participants available.");
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
            System.out.println("\nNo teams formed yet. Please form teams first.");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  FORMED TEAMS (" + formedTeams.size() + " teams)");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════════════════╝");

        for (Team team : formedTeams) {
            System.out.println(team.toString());
        }

        if (!remainingParticipants.isEmpty()) {
            System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║  REMAINING PARTICIPANTS (" + remainingParticipants.size() + " unassigned)");
            System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════════════════╝");

            for (Participant p : remainingParticipants) {
                System.out.println("  " + p.toString());
            }
        }
    }

    public void viewParticipantInfo(String participantId) throws ParticipantNotFoundException {
        Participant participant = getParticipantById(participantId);

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  PARTICIPANT INFORMATION");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("  " + participant.toString());

        for (Team team : formedTeams) {
            if (team.getMembers().contains(participant)) {
                System.out.println("\nAssigned to Team " + team.getTeamId());
                return;
            }
        }
        System.out.println("\nNot yet assigned to a team");
    }

    public void viewParticipantTeamAssignment(String participantId) throws ParticipantNotFoundException {
        Participant participant = getParticipantById(participantId);

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  MY TEAM ASSIGNMENT");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════════════════╝");

        for (Team team : formedTeams) {
            if (team.getMembers().contains(participant)) {
                System.out.println("\nYou are assigned to Team " + team.getTeamId());
                System.out.println("\n" + team.toString());
                return;
            }
        }
        System.out.println("\nYou have not been assigned to a team yet.");
        System.out.println("  Teams will be formed by the organizer soon.");
    }

    // Update methods
    public void updateParticipantEmail(String participantId, String newEmail) throws ParticipantNotFoundException {
        Participant participant = getParticipantById(participantId);
        // Create new participant with updated email
        Participant updated = new Participant(
                participant.getId(),
                participant.getName(),
                newEmail,
                participant.getGame().getDisplayName(),
                participant.getSkillLevel(),
                participant.getRole().getDisplayName(),
                participant.getPersonalityScore()
        );
        participants.set(participants.indexOf(participant), updated);
    }

    public void updateParticipantSkill(String participantId, int newSkill) throws ParticipantNotFoundException {
        Participant participant = getParticipantById(participantId);
        Participant updated = new Participant(
                participant.getId(),
                participant.getName(),
                participant.getEmail(),
                participant.getGame().getDisplayName(),
                newSkill,
                participant.getRole().getDisplayName(),
                participant.getPersonalityScore()
        );
        participants.set(participants.indexOf(participant), updated);
    }

    public void updateParticipantGame(String participantId, Game newGame) throws ParticipantNotFoundException {
        Participant participant = getParticipantById(participantId);
        Participant updated = new Participant(
                participant.getId(),
                participant.getName(),
                participant.getEmail(),
                newGame.getDisplayName(),
                participant.getSkillLevel(),
                participant.getRole().getDisplayName(),
                participant.getPersonalityScore()
        );
        participants.set(participants.indexOf(participant), updated);
    }

    public void updateParticipantRole(String participantId, Role newRole) throws ParticipantNotFoundException {
        Participant participant = getParticipantById(participantId);
        Participant updated = new Participant(
                participant.getId(),
                participant.getName(),
                participant.getEmail(),
                participant.getGame().getDisplayName(),
                participant.getSkillLevel(),
                newRole.getDisplayName(),
                participant.getPersonalityScore()
        );
        participants.set(participants.indexOf(participant), updated);
    }

    public String generateNextParticipantId() {
        return "P" + String.format("%03d", nextParticipantId++);
    }
}