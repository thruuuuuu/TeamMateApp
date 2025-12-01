package Manager;

import Entity.Participant;
import Entity.Team;
import Enums.Game;
import Enums.Role;
import Exceptions.*;
import Threads.*;
import Main.FormationStatistics;
import Log.Logger;

import java.io.*;
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
        Logger.debug("TeamManager instance created");
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
        Logger.info("Participant added: " + participant.getId() + " - " + participant.getName());
        Logger.debug("Total participants: " + participants.size());
    }

    public void setTeamSize(int size) throws InvalidTeamSizeException {
        Logger.info("Setting team size to: " + size);
        if (size < 3 || size > 10) {
            Logger.warning("Invalid team size attempted: " + size);
            throw new InvalidTeamSizeException("Team size must be between 3 and 10");
        }
        this.teamSize = size;
        Logger.info("Team size successfully set to: " + size);
    }

    public boolean participantExists(String participantId) {
        boolean exists = participants.stream()
                .anyMatch(p -> p.getId().equalsIgnoreCase(participantId));
        Logger.debug("Participant existence check for " + participantId + ": " + exists);
        return exists;
    }

    public Participant getParticipantById(String participantId) throws ParticipantNotFoundException {
        Logger.debug("Retrieving participant: " + participantId);
        Participant participant = participants.stream()
                .filter(p -> p.getId().equalsIgnoreCase(participantId))
                .findFirst()
                .orElseThrow(() -> {
                    Logger.warning("Participant not found: " + participantId);
                    return new ParticipantNotFoundException("Participant not found: " + participantId);
                });
        Logger.debug("Participant retrieved: " + participantId);
        return participant;
    }

    public boolean hasRemainingParticipants() {
        boolean hasRemaining = !remainingParticipants.isEmpty();
        Logger.debug("Has remaining participants: " + hasRemaining + " (count: " + remainingParticipants.size() + ")");
        return hasRemaining;
    }

    public void loadParticipantsFromCSV(String filePath) {
        Logger.info("Loading participants from CSV: " + filePath);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        CSVLoaderThread loader = new CSVLoaderThread(filePath);
        Future<List<Participant>> future = executor.submit(loader);

        try {
            List<Participant> loadedParticipants = future.get();
            participants.clear();
            participants.addAll(loadedParticipants);

            Logger.info("Successfully loaded " + participants.size() + " participants from " + filePath);
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
                        Logger.warning("Invalid participant ID format: " + id);
                    }
                }
            }
            Logger.debug("Next participant ID set to: " + nextParticipantId);

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            Logger.error("Error loading CSV: " + cause.getMessage(), (Exception) cause);
            System.out.println("✗ Error: " + cause.getMessage());
        } catch (InterruptedException e) {
            Logger.error("CSV loading interrupted", e);
            System.out.println("✗ Error: Loading was interrupted");
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
            Logger.debug("CSV loader executor shutdown");
        }
    }

    public void loadTeamFormationFromCSV(String filePath) {
        Logger.info("Loading team formation from CSV: " + filePath);
        System.out.println("\nLoading team formation...");

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            int lineNumber = 0;

            // Clear existing data
            formedTeams.clear();
            remainingParticipants.clear();
            participants.clear();
            Logger.debug("Cleared existing data for fresh load");

            Map<Integer, Team> teamMap = new HashMap<>();

            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] data = line.split(",");
                if (data.length >= 9) {
                    try {
                        int teamId = Integer.parseInt(data[0].trim());
                        String id = data[1].trim();
                        String name = data[2].trim();
                        String email = data[3].trim();
                        String game = data[4].trim();
                        int skill = Integer.parseInt(data[5].trim());
                        String role = data[6].trim();
                        int personalityScore = Integer.parseInt(data[7].trim());

                        Participant p = new Participant(id, name, email, game, skill, role, personalityScore);
                        participants.add(p);

                        // TeamID 0 means remaining/unassigned participant
                        if (teamId == 0) {
                            remainingParticipants.add(p);
                            Logger.debug("Added remaining participant: " + id);
                        } else {
                            // Add to team
                            Team team = teamMap.computeIfAbsent(teamId, Team::new);
                            team.addMember(p);
                            Logger.debug("Added participant " + id + " to team " + teamId);
                        }
                    } catch (NumberFormatException e) {
                        Logger.warning("Invalid data format at line " + lineNumber + ": " + line);
                    }
                }
            }

            // Add all teams to formedTeams list (sorted by team ID)
            formedTeams.addAll(teamMap.values());
            formedTeams.sort(Comparator.comparingInt(Team::getTeamId));

            Logger.info("Successfully loaded team formation - Teams: " + formedTeams.size() +
                    ", Participants: " + participants.size() +
                    ", Remaining: " + remainingParticipants.size());

            System.out.println("✓ Successfully loaded team formation from " + filePath);
            System.out.println("  Teams loaded: " + formedTeams.size());
            System.out.println("  Total participants: " + participants.size());
            System.out.println("  Remaining participants: " + remainingParticipants.size());

        } catch (FileNotFoundException e) {
            Logger.error("Team formation file not found: " + filePath, e);
            System.out.println("✗ Error: File not found - " + filePath);
        } catch (IOException e) {
            Logger.error("Error reading team formation file: " + filePath, e);
            System.out.println("✗ Error reading file: " + e.getMessage());
        } catch (NumberFormatException e) {
            Logger.error("Invalid number format in team formation CSV", e);
            System.out.println("✗ Error: Invalid number format in CSV");
        }
    }

    public FormationStatistics formTeams() {
        Logger.info("Starting team formation process with team size: " + teamSize);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        TeamFormationThread formationThread = new TeamFormationThread(participants, teamSize);
        Future<List<Team>> future = executor.submit(formationThread);

        FormationStatistics stats = null;

        try {
            System.out.println("\nForming teams...");

            int totalParticipants = participants.size();
            Logger.debug("Total participants for team formation: " + totalParticipants);

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

            Logger.info("Team formation completed - " +
                    "Teams: " + teamsFormed +
                    ", Assigned: " + participantsAssigned +
                    ", Remaining: " + participantsRemaining);

            stats = new FormationStatistics(totalParticipants, teamsFormed, participantsAssigned, participantsRemaining, teamSize);

            System.out.println("✓ Successfully formed " + teamsFormed + " teams!");

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            Logger.error("Team formation failed: " + cause.getMessage(), (Exception) cause);
            System.out.println("✗ Error: " + cause.getMessage());
        } catch (InterruptedException e) {
            Logger.error("Team formation interrupted", e);
            System.out.println("✗ Error: Team formation was interrupted");
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
            Logger.debug("Team formation executor shutdown");
        }

        return stats;
    }

    public void saveTeamsToCSV(String filePath) {
        Logger.info("Saving teams to CSV: " + filePath);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        TeamSaverThread saver = new TeamSaverThread(formedTeams, filePath);
        Future<Boolean> future = executor.submit(saver);

        try {
            Boolean success = future.get();
            if (success) {
                Logger.info("Teams saved successfully to: " + filePath);
                System.out.println("\n✓ Teams saved successfully to " + filePath);
            }
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            Logger.error("Error saving teams to CSV: " + cause.getMessage(), (Exception) cause);
            System.out.println("✗ Error: " + cause.getMessage());
        } catch (InterruptedException e) {
            Logger.error("Team saving interrupted", e);
            System.out.println("✗ Error: Saving was interrupted");
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
            Logger.debug("Team saver executor shutdown");
        }
    }

    public void appendRemainingParticipantsToCSV(String filePath) {
        Logger.info("Appending remaining participants to CSV: " + filePath);

        if (remainingParticipants.isEmpty()) {
            Logger.debug("No remaining participants to append");
            System.out.println("\n⚠ No remaining participants to save.");
            return;
        }

        try (FileWriter fw = new FileWriter(filePath, true);
             PrintWriter writer = new PrintWriter(fw)) {

            for (Participant p : remainingParticipants) {
                writer.println("0," + p.toCSVString());
            }

            Logger.info("Appended " + remainingParticipants.size() + " remaining participants with TeamID=0");
            System.out.println("✓ Saved " + remainingParticipants.size() + " remaining participants with TeamID=0");

        } catch (IOException e) {
            Logger.error("Error appending remaining participants", e);
            System.out.println("✗ Error appending remaining participants: " + e.getMessage());
        }
    }

    public void viewAllParticipants() {
        Logger.debug("Viewing all participants (count: " + participants.size() + ")");

        if (participants.isEmpty()) {
            Logger.debug("No participants to display");
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
        Logger.debug("Viewing formed teams (count: " + formedTeams.size() + ")");

        if (formedTeams.isEmpty()) {
            Logger.debug("No teams to display");
            System.out.println("\n✗ No teams formed yet. Please form teams first.");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  FORMED TEAMS (" + formedTeams.size() + " teams)");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════════════════╝");

        for (Team team : formedTeams) {
            System.out.println(team.toString());
        }
    }

    public void viewRemainingParticipants() {
        Logger.debug("Viewing remaining participants (count: " + remainingParticipants.size() + ")");

        if (remainingParticipants.isEmpty()) {
            Logger.debug("No remaining participants");
            System.out.println("\n✓ No remaining participants. All participants have been assigned to teams!");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  REMAINING PARTICIPANTS (" + remainingParticipants.size() + " unassigned)");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════════════════╝");

        for (Participant p : remainingParticipants) {
            System.out.println("  " + p.toString());
        }
    }

    public void viewParticipantInfo(String participantId) throws ParticipantNotFoundException {
        Logger.debug("Viewing info for participant: " + participantId);
        Participant participant = getParticipantById(participantId);

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  PARTICIPANT INFORMATION");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("  " + participant.toString());

        for (Team team : formedTeams) {
            if (team.getMembers().contains(participant)) {
                Logger.debug("Participant " + participantId + " is in team " + team.getTeamId());
                System.out.println("\n  ✓ Assigned to Team " + team.getTeamId());
                return;
            }
        }

        if (remainingParticipants.contains(participant)) {
            Logger.debug("Participant " + participantId + " is in remaining pool");
            System.out.println("\n  ⚠ Remaining participant (not assigned to any team)");
        } else {
            Logger.debug("Participant " + participantId + " not yet assigned");
            System.out.println("\n  ⚠ Not yet assigned to a team");
        }
    }

    public void viewParticipantTeamAssignment(String participantId) throws ParticipantNotFoundException {
        Logger.debug("Viewing team assignment for participant: " + participantId);
        Participant participant = getParticipantById(participantId);

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  MY TEAM ASSIGNMENT");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════════════════╝");

        for (Team team : formedTeams) {
            if (team.getMembers().contains(participant)) {
                Logger.info("Displayed team assignment for " + participantId + " - Team " + team.getTeamId());
                System.out.println("\n✓ You are assigned to Team " + team.getTeamId());
                System.out.println("\n" + team.toString());
                return;
            }
        }

        if (remainingParticipants.contains(participant)) {
            Logger.debug("Participant " + participantId + " is in remaining pool");
            System.out.println("\n⚠ You are in the remaining participants pool.");
            System.out.println("  You were not assigned to a team in the current formation.");
        } else {
            Logger.debug("Participant " + participantId + " waiting for team formation");
            System.out.println("\n⚠ You have not been assigned to a team yet.");
            System.out.println("  Teams will be formed by the organizer soon.");
        }
    }

    public void updateParticipantEmail(String participantId, String newEmail) throws ParticipantNotFoundException {
        Logger.info("Updating email for participant " + participantId + " to: " + newEmail);
        Participant participant = getParticipantById(participantId);
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
        Logger.info("Email updated successfully for: " + participantId);
    }

    public void updateParticipantSkill(String participantId, int newSkill) throws ParticipantNotFoundException {
        Logger.info("Updating skill level for participant " + participantId + " to: " + newSkill);
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
        Logger.info("Skill level updated successfully for: " + participantId);
    }

    public void updateParticipantGame(String participantId, Game newGame) throws ParticipantNotFoundException {
        Logger.info("Updating game for participant " + participantId + " to: " + newGame.getDisplayName());
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
        Logger.info("Game updated successfully for: " + participantId);
    }

    public void updateParticipantRole(String participantId, Role newRole) throws ParticipantNotFoundException {
        Logger.info("Updating role for participant " + participantId + " to: " + newRole.getDisplayName());
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
        Logger.info("Role updated successfully for: " + participantId);
    }
}