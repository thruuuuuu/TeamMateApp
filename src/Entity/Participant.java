package Entity;

import Enums.Game;
import Enums.Role;
import Enums.PersonalityType;

public class Participant {
    private final String id;
    private final String name;
    private final String email;
    private final Game game;
    private final int skillLevel;
    private final Role role;
    private final int personalityScore;
    private final PersonalityType personalityType;

    public Participant(String id, String name, String email, String game, int skillLevel, String role, int personalityScore) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.game = parseGame(game);
        this.skillLevel = skillLevel;
        this.role = parseRole(role);
        this.personalityScore = personalityScore;
        this.personalityType = PersonalityType.classify(personalityScore);
    }

    // Constructor without ID (for survey completion - auto-generate ID)
    public Participant(String name, String email, Game game, int skillLevel, Role role, int personalityScore) {
        this.id = generateId();
        this.name = name;
        this.email = email;
        this.game = game;
        this.skillLevel = skillLevel;
        this.role = role;
        this.personalityScore = personalityScore;
        this.personalityType = PersonalityType.classify(personalityScore);
    }

    private String generateId() {
        // Generate a unique ID with timestamp to avoid collisions
        return "P" + System.currentTimeMillis() % 100000;
    }

    private Game parseGame(String gameStr) {
        for (Game g : Game.values()) {
            if (g.getDisplayName().equalsIgnoreCase(gameStr)) {
                return g;
            }
        }
        return Game.CHESS; // Default
    }

    private Role parseRole(String roleStr) {
        for (Role r : Role.values()) {
            if (r.getDisplayName().equalsIgnoreCase(roleStr)) {
                return r;
            }
        }
        return Role.SUPPORTER; // Default
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Game getGame() {
        return game;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public Role getRole() {
        return role;
    }

    public int getPersonalityScore() {
        return personalityScore;
    }

    public PersonalityType getPersonalityType() {
        return personalityType;
    }

    @Override
    public String toString() {
        return String.format("%-8s | %-20s | %-25s | %-12s | Skill: %-2d | %-12s | %s (Score: %d)",
                id, name, email, game.getDisplayName(), skillLevel, role.getDisplayName(),
                personalityType.getDisplayName(), personalityScore);
    }

    public String toCSVString() {
        return String.format("%s,%s,%s,%s,%d,%s,%d,%s",
                id,
                name,
                email,
                game.getDisplayName(),
                skillLevel,
                role.getDisplayName(),
                personalityScore,
                personalityType.getDisplayName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}