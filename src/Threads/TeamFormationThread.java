package Threads;

import Entity.Participant;
import Entity.Team;
import Enums.Game;
import Enums.Role;
import Enums.PersonalityType;
import Exceptions.InsufficientParticipantsException;
import Exceptions.NoParticipantsException;

import java.util.*;
import java.util.concurrent.*;

public class TeamFormationThread implements Callable<List<Team>> {
    private final List<Participant> participants;
    private final int teamSize;

    public TeamFormationThread(List<Participant> participants, int teamSize) {
        this.participants = new ArrayList<>(participants);
        this.teamSize = teamSize;
    }

    @Override
    public List<Team> call() throws Exception {
        if (participants.isEmpty()) {
            throw new NoParticipantsException("No participants available for team formation");
        }

        if (participants.size() < teamSize) {
            throw new InsufficientParticipantsException(
                    "Not enough participants. Need at least " + teamSize + ", but only " + participants.size() + " available"
            );
        }

        List<Team> formedTeams = new ArrayList<>();
        List<Participant> available = new ArrayList<>(participants);
        Collections.shuffle(available); // Randomize for fairness

        int teamId = 1;

        while (available.size() >= teamSize) {
            Team team = new Team(teamId++);
            List<Participant> teamMembers = selectBalancedTeam(available, teamSize);

            for (Participant p : teamMembers) {
                team.addMember(p);
                available.remove(p);
            }

            formedTeams.add(team);
        }

        return formedTeams;
    }

    private List<Participant> selectBalancedTeam(List<Participant> available, int size) {
        List<Participant> selected = new ArrayList<>();
        List<Participant> pool = new ArrayList<>(available);

        // 1. Try to get at least one leader
        Participant leader = pool.stream()
                .filter(p -> p.getPersonalityType() == PersonalityType.LEADER)
                .findFirst()
                .orElse(null);

        if (leader != null) {
            selected.add(leader);
            pool.remove(leader);
        }

        // 2. Select remaining members ensuring role diversity
        Set<Role> usedRoles = new HashSet<>();
        Set<Game> usedGames = new HashSet<>();

        if (leader != null) {
            usedRoles.add(leader.getRole());
            usedGames.add(leader.getGame());
        }

        while (selected.size() < size && !pool.isEmpty()) {
            Participant best = null;
            int bestScore = -1;

            for (Participant p : pool) {
                int score = 0;

                // Prefer different roles
                if (!usedRoles.contains(p.getRole())) {
                    score += 3;
                }

                // Prefer different games (max 2 per game)
                long gameCount = selected.stream()
                        .filter(s -> s.getGame() == p.getGame())
                        .count();
                if (gameCount < 2) {
                    score += 2;
                }

                // Balance personality types
                if (selected.size() < size - 1) {
                    if (p.getPersonalityType() == PersonalityType.THINKER &&
                            selected.stream().noneMatch(s -> s.getPersonalityType() == PersonalityType.THINKER)) {
                        score += 2;
                    }
                }

                if (score > bestScore) {
                    bestScore = score;
                    best = p;
                }
            }

            if (best != null) {
                selected.add(best);
                usedRoles.add(best.getRole());
                usedGames.add(best.getGame());
                pool.remove(best);
            } else {
                // Just pick the first available
                selected.add(pool.getFirst());
                pool.removeFirst();
            }
        }

        return selected;
    }
}