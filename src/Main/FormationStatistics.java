package Main;

public class FormationStatistics {
    private final int totalParticipants;
    private final int teamsFormed;
    private final int participantsAssigned;
    private final int participantsRemaining;
    private final int teamSize;

    public FormationStatistics(int totalParticipants, int teamsFormed, int participantsAssigned,
                               int participantsRemaining, int teamSize) {
        this.totalParticipants = totalParticipants;
        this.teamsFormed = teamsFormed;
        this.participantsAssigned = participantsAssigned;
        this.participantsRemaining = participantsRemaining;
        this.teamSize = teamSize;
    }

    // Getters for database operations
    public int getTotalParticipants() {
        return totalParticipants;
    }

    public int getTeamsFormed() {
        return teamsFormed;
    }

    public int getParticipantsAssigned() {
        return participantsAssigned;
    }

    public int getParticipantsRemaining() {
        return participantsRemaining;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void display() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     TEAM FORMATION STATISTICS          ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("  Total Participants:      " + totalParticipants);
        System.out.println("  Team Size:               " + teamSize);
        System.out.println("  Teams Formed:            " + teamsFormed);
        System.out.println("  Participants Assigned:   " + participantsAssigned);
        System.out.println("  Participants Remaining:  " + participantsRemaining);
        System.out.println("════════════════════════════════════════");
    }
}