public class Participant {
    private final String name;
    private final String game;
    private final int skillLevel;
    private final String role;
    private final int personalityScore;
    private final String personalityType;

    public Participant(String name, String game, int skillLevel, String role, int personalityScore) {
        this.name = name;
        this.game = game;
        this.skillLevel = skillLevel;
        this.role = role;
        this.personalityScore = personalityScore;
        this.personalityType = classifyPersonality(personalityScore);
    }

    private String classifyPersonality(int score) {
        if (score >= 90) {
            return "Leader";
        } else if (score >= 70) {
            return "Balanced";
        } else {
            return "Thinker";
        }
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getGame() {
        return game;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public String getRole() {
        return role;
    }

    public int getPersonalityScore() {
        return personalityScore;
    }

    public String getPersonalityType() {
        return personalityType;
    }

    @Override
    public String toString() {
        return String.format("%-20s | %-12s | Skill: %-2d | %-12s | %s (Score: %d)",
                name, game, skillLevel, role, personalityType, personalityScore);
    }

    public String toCSVString() {
        return String.format("%s,%s,%d,%s,%d,%s",
                name, game, skillLevel, role, personalityScore, personalityType);
    }
}
