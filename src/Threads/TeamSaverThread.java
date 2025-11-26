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

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("TeamID,ParticipantID,ParticipantName,Email,Enums.Game,SkillLevel,Role,PersonalityScore,Enums.PersonalityType");

            for (Team team : teams) {
                for (Participant p : team.getMembers()) {
                    writer.println(team.getTeamId() + "," + p.toCSVString());
                }
            }
            return true;
        }
    }
}