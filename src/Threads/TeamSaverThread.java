package Threads;

import Entity.Participant;
import Entity.Team;
import Exceptions.NoTeamsFormedException;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class TeamSaverThread implements Callable<Boolean> {
    private final List<Team> teams;
    private final String filePath;

    public TeamSaverThread(List<Team> teams, String filePath) {
        this.teams = teams;
        this.filePath = filePath;
    }

    @Override
    public Boolean call() throws Exception {
        if (teams == null || teams.isEmpty()) {
            throw new NoTeamsFormedException("No teams available to save");
        }

        // Create directory if it doesn't exist
        File file = new File(filePath);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create directory: " + parentDir.getPath());
            }
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println("TeamID,ParticipantID,ParticipantName,Email,Game,SkillLevel,Role,PersonalityScore,PersonalityType");

            // Write team data
            int rowCount = 0;
            for (Team team : teams) {
                if (team != null && team.getMembers() != null) {
                    for (Participant p : team.getMembers()) {
                        if (p != null) {
                            try {
                                String csvLine = team.getTeamId() + "," + p.toCSVString();
                                writer.println(csvLine);
                                rowCount++;
                            } catch (Exception e) {
                                System.err.println("Error writing participant " + p.getId() + ": " + e.getMessage());
                                throw e;
                            }
                        }
                    }
                }
            }

            writer.flush();
            System.out.println("Debug: Wrote " + rowCount + " participant records to CSV");
            return true;

        } catch (IOException e) {
            System.err.println("IO Error while saving teams: " + e.getMessage());
            throw e;
        }
    }
}