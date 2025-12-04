package Manager;

import Database.ParticipantDAO;
import Database.TeamDAO;
import Database.AuthenticationService;
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
    private List<Participant> participants;
    private List<Team> formedTeams;
    private List<Participant> remainingParticipants;
    private int teamSize = 5;
    private String currentOrganizerId = null;

    public TeamManager() {
        this.participants = new ArrayList<>();
        this.formedTeams = new ArrayList<>();
        this.remainingParticipants = new ArrayList<>();
        Logger.debug("TeamManager instance created");
    }

    public void setCurrentOrganizer(String organizerId) {
        this.currentOrganizerId = organizerId;
    }

    public void addParticipant(Participant participant) {
        // Generate password for the participant
        String password = AuthenticationService.generateParticipantPassword(
                participant.getName(), participant.getId());

        // Insert into database
        if (ParticipantDAO.insertParticipant(participant, password)) {
            participants.add(participant);
            Logger.info("Participant added: " + participant.getId() + " - " + participant.getName());
            Logger.debug("Total participants: " + participants.size());
        }
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
        boolean exists = AuthenticationService.participantExists(participantId);
        Logger.debug("Participant existence check for " + participantId + ": " + exists);
        return exists;
    }

    public Participant getParticipantById(String participantId) throws ParticipantNotFoundException {
        Logger.debug("Retrieving participant: " + participantId);
        Participant participant = ParticipantDAO.getParticipantById(participantId);

        if (participant == null) {
            Logger.warning("Participant not found: " + participantId);
            throw new ParticipantNotFoundException("Participant not found: " + participantId);
        }

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

            // Bulk insert into database
            int insertedCount = ParticipantDAO.bulkInsertParticipants(loadedParticipants);

            // Reload from database to get fresh data
            participants.clear();
            participants.addAll(ParticipantDAO.getAllParticipants());

            Logger.info("Successfully loaded and inserted " + insertedCount + " participants from " + filePath);
            System.out.println("\n✓ Successfully loaded " + insertedCount + " participants from " + filePath);
            System.out.println("  All participants have been saved to the database.");

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
        System.out.println("\nLoading team formation from file...");

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            int lineNumber = 0;

            formedTeams.clear();
            remainingParticipants.clear();
            participants.clear();

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

                        // Get participant from database
                        Participant p = ParticipantDAO.getParticipantById(id);

                        if (p != null) {
                            participants.add(p);

                            if (teamId == 0) {
                                remainingParticipants.add(p);
                            } else {
                                Team team = teamMap.computeIfAbsent(teamId, Team::new);
                                team.addMember(p);
                            }
                        }
                    } catch (NumberFormatException e) {
                        Logger.warning("Invalid data format at line " + lineNumber + ": " + line);
                    }
                }
            }

            formedTeams.addAll(teamMap.values());
            formedTeams.sort(Comparator.comparingInt(Team::getTeamId));

            Logger.info("Successfully loaded team formation - Teams: " + formedTeams.size());
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
        }
    }

    public FormationStatistics formTeams() {
        Logger.info("Starting team formation process with team size: " + teamSize);

        // Load all participants from database
        participants = ParticipantDAO.getAllParticipants();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        TeamFormationThread formationThread = new TeamFormationThread(participants, teamSize);
        Future<List<Team>> future = executor.submit(formationThread);

        FormationStatistics stats = null;

        try {
            System.out.println("\nForming teams...");
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

            Logger.info("Team formation completed - Teams: " + teamsFormed);
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
        }

        return stats;
    }

    /**
     * NEW METHOD: Save formed teams to database
     * Called explicitly by user choice, not automatically
     */
    public void saveTeamsToDatabase() {
        if (formedTeams == null || formedTeams.isEmpty()) {
            Logger.warning("No teams to save to database");
            System.out.println("✗ No teams formed yet. Please form teams first.");
            return;
        }

        boolean saved = TeamDAO.saveTeamsToDatabase(formedTeams, teamSize, currentOrganizerId);

        if (saved) {
            Logger.info("Teams saved to database successfully");
        } else {
            Logger.error("Failed to save teams to database");
            System.out.println("✗ Error saving teams to database");
        }
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

            Logger.info("Appended " + remainingParticipants.size() + " remaining participants");
            System.out.println("✓ Saved " + remainingParticipants.size() + " remaining participants with TeamID=0");

        } catch (IOException e) {
            Logger.error("Error appending remaining participants", e);
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    public void viewAllParticipants() {
        // Load from database
        participants = ParticipantDAO.getAllParticipants();

        Logger.debug("Viewing all participants (count: " + participants.size() + ")");

        if (participants.isEmpty()) {
            System.out.println("\n✗ No participants available in database.");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  ALL PARTICIPANTS (" + participants.size() + " total) - Loaded from Database");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════════════════════╝");

        for (Participant p : participants) {
            System.out.println("  " + p.toString());
        }
    }

    public void viewFormedTeams() {
        // Load teams from database
        formedTeams = TeamDAO.getAllTeamsFromDatabase();

        Logger.debug("Viewing formed teams (count: " + formedTeams.size() + ")");

        if (formedTeams.isEmpty()) {
            System.out.println("\n✗ No teams formed yet. Please form teams first.");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  FORMED TEAMS (" + formedTeams.size() + " teams) - Loaded from Database");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════════════════════╝");

        for (Team team : formedTeams) {
            System.out.println(team.toString());
        }
    }

    public void viewRemainingParticipants() {
        // Get unassigned participants from database
        remainingParticipants = ParticipantDAO.getUnassignedParticipants();

        Logger.debug("Viewing remaining participants (count: " + remainingParticipants.size() + ")");

        if (remainingParticipants.isEmpty()) {
            System.out.println("\n✓ No remaining participants. All assigned to teams!");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  REMAINING PARTICIPANTS (" + remainingParticipants.size() + " unassigned)");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════════════════════╝");

        for (Participant p : remainingParticipants) {
            System.out.println("  " + p.toString());
        }
    }

    public void viewParticipantInfo(String participantId) throws ParticipantNotFoundException {
        Participant participant = getParticipantById(participantId);

        System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  PARTICIPANT INFORMATION (From Database)");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("  " + participant.toString());

        Integer teamId = TeamDAO.getParticipantTeamId(participantId);

        if (teamId != null) {
            System.out.println("\n  ✓ Assigned to Team " + teamId);
        } else {
            System.out.println("\n  ⚠ Not assigned to any team");
        }
    }

    public void viewParticipantTeamAssignment(String participantId) throws ParticipantNotFoundException {
        Participant participant = getParticipantById(participantId);

        System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  MY TEAM ASSIGNMENT (From Database)");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════════════════════╝");

        Integer teamId = TeamDAO.getParticipantTeamId(participantId);

        if (teamId != null) {
            Team team = TeamDAO.getTeamById(teamId);
            System.out.println("\n✓ You are assigned to Team " + teamId);
            System.out.println("\n" + team.toString());
        } else {
            System.out.println("\n⚠ You have not been assigned to a team yet.");
        }
    }

    public void updateParticipantEmail(String participantId, String newEmail) throws ParticipantNotFoundException {
        if (ParticipantDAO.updateParticipantEmail(participantId, newEmail)) {
            Logger.info("Email updated in database for: " + participantId);
        } else {
            throw new ParticipantNotFoundException("Failed to update participant email");
        }
    }

    public void updateParticipantSkill(String participantId, int newSkill) throws ParticipantNotFoundException {
        if (ParticipantDAO.updateParticipantSkill(participantId, newSkill)) {
            Logger.info("Skill updated in database for: " + participantId);
        } else {
            throw new ParticipantNotFoundException("Failed to update participant skill");
        }
    }

    public void updateParticipantGame(String participantId, Game newGame) throws ParticipantNotFoundException {
        if (ParticipantDAO.updateParticipantGame(participantId, newGame)) {
            Logger.info("Game updated in database for: " + participantId);
        } else {
            throw new ParticipantNotFoundException("Failed to update participant game");
        }
    }

    public void updateParticipantRole(String participantId, Role newRole) throws ParticipantNotFoundException {
        if (ParticipantDAO.updateParticipantRole(participantId, newRole)) {
            Logger.info("Role updated in database for: " + participantId);
        } else {
            throw new ParticipantNotFoundException("Failed to update participant role");
        }
    }
}