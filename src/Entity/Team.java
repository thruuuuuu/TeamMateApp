package Entity;

import Enums.Game;
import Enums.Role;
import Enums.PersonalityType;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private final int teamId;
    private final List<Participant> members;

    public Team(int teamId) {
        this.teamId = teamId;
        this.members = new ArrayList<>();
    }

    public void addMember(Participant participant) {
        members.add(participant);
    }

    public List<Participant> getMembers() {
        return members;
    }

    public int getTeamId() {
        return teamId;
    }

    public int getSize() {
        return members.size();
    }

    public double getAverageSkill() {
        if (members.isEmpty()) return 0;
        return members.stream()
                .mapToInt(Participant::getSkillLevel)
                .average()
                .orElse(0);
    }

    public boolean hasRole(Role role) {
        return members.stream()
                .anyMatch(p -> p.getRole() == role);
    }

    public long getRoleDiversity() {
        return members.stream()
                .map(Participant::getRole)
                .distinct()
                .count();
    }

    public long countGame(Game game) {
        return members.stream()
                .filter(p -> p.getGame() == game)
                .count();
    }

    public boolean hasPersonalityType(PersonalityType type) {
        return members.stream()
                .anyMatch(p -> p.getPersonalityType() == type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════════════════════════════════════════╗\n");
        sb.append(String.format("║  TEAM %d (Avg Skill: %.1f) - %d members, %d unique roles                   \n",
                teamId, getAverageSkill(), getSize(), getRoleDiversity()));
        sb.append("╚════════════════════════════════════════════════════════════════════════════════╝\n");
        for (Participant p : members) {
            sb.append("  ").append(p.toString()).append("\n");
        }
        return sb.toString();
    }
}