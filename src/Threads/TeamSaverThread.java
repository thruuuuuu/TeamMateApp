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
        if (teams.isEmpty()) {
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
            writer.println("TeamID,ParticipantID,ParticipantName,Email,Game,SkillLevel,Role,PersonalityScore,PersonalityType");

            for (Team team : teams) {
                for (Participant p : team.getMembers()) {
                    writer.println(team.getTeamId() + "," + p.toCSVString());
                }
            }
            return true;
        }
    }
}