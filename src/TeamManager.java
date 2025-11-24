import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class TeamManager {
    private final List<Participant> participants;
    private final List<Team> formedTeams;
    private int teamSize = 5; // Default team size

    public TeamManager() {
        this.participants = new ArrayList<>();
        this.formedTeams = new ArrayList<>();
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
    }

    public void setTeamSize(int size) {
        this.teamSize = size;
    }

    public void loadParticipantsFromCSV(String filePath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<?> future = executor.submit(() -> {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                boolean isHeader = true;
                int count = 0;

                while ((line = br.readLine()) != null) {
                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }

                    String[] data = line.split(",");
                    if (data.length >= 5) {
                        String name = data[0].trim();
                        String game = data[1].trim();
                        int skill = Integer.parseInt(data[2].trim());
                        String role = data[3].trim();
                        int personalityScore = Integer.parseInt(data[4].trim());

                        Participant p = new Participant(name, game, skill, role, personalityScore);
                        participants.add(p);
                        count++;
                    }
                }

                System.out.println("\n✓ Successfully loaded " + count + " participants from " + filePath);

            } catch (FileNotFoundException e) {
                System.out.println("✗ Error: File not found - " + filePath);
            } catch (IOException e) {
                System.out.println("✗ Error reading file: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("✗ Error: Invalid number format in CSV");
            }
        });

        try {
            future.get(); // Wait for completion
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("✗ Error loading participants: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    public void formTeams() {
        if (participants.isEmpty()) {
            System.out.println("\n✗ No participants available. Please upload CSV or add participants first.");
            return;
        }

        if (participants.size() < teamSize) {
            System.out.println("\n✗ Not enough participants. Need at least " + teamSize + " participants.");
            return;
        }

        System.out.println("\n⏳ Forming teams...");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<?> future = executor.submit(() -> {
            formedTeams.clear();
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

            System.out.println("✓ Successfully formed " + formedTeams.size() + " teams!");
            if (!available.isEmpty()) {
                System.out.println("  Note: " + available.size() + " participants remain unassigned.");
            }
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("✗ Error forming teams: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    private List<Participant> selectBalancedTeam(List<Participant> available, int size) {
        List<Participant> selected = new ArrayList<>();
        List<Participant> pool = new ArrayList<>(available);

        // 1. Try to get at least one leader
        Participant leader = pool.stream()
                .filter(p -> p.getPersonalityType().equals("Leader"))
                .findFirst()
                .orElse(null);

        if (leader != null) {
            selected.add(leader);
            pool.remove(leader);
        }

        // 2. Select remaining members ensuring role diversity
        Set<String> usedRoles = new HashSet<>();
        Set<String> usedGames = new HashSet<>();

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
                        .filter(s -> s.getGame().equals(p.getGame()))
                        .count();
                if (gameCount < 2) {
                    score += 2;
                }

                // Balance personality types
                if (selected.size() < size - 1) {
                    if (p.getPersonalityType().equals("Thinker") &&
                            selected.stream().noneMatch(s -> s.getPersonalityType().equals("Thinker"))) {
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

    public void saveTeamsToCSV(String filePath) {
        if (formedTeams.isEmpty()) {
            System.out.println("\n✗ No teams formed yet. Please form teams first.");
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("TeamID,ParticipantName,Game,SkillLevel,Role,PersonalityScore,PersonalityType");

            for (Team team : formedTeams) {
                for (Participant p : team.getMembers()) {
                    writer.println(team.getTeamId() + "," + p.toCSVString());
                }
            }

            System.out.println("\n✓ Teams saved successfully to " + filePath);

        } catch (IOException e) {
            System.out.println("✗ Error saving teams: " + e.getMessage());
        }
    }

    public void viewAllParticipants() {
        if (participants.isEmpty()) {
            System.out.println("\n✗ No participants available.");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  ALL PARTICIPANTS (" + participants.size() + " total)");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════╝");

        for (Participant p : participants) {
            System.out.println("  " + p.toString());
        }
    }

    public void viewFormedTeams() {
        if (formedTeams.isEmpty()) {
            System.out.println("\n✗ No teams formed yet. Please form teams first.");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  FORMED TEAMS (" + formedTeams.size() + " teams)");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════╝");

        for (Team team : formedTeams) {
            System.out.println(team.toString());
        }
    }

    public void viewParticipantInfo(String name) {
        Participant participant = participants.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (participant == null) {
            System.out.println("\n✗ Participant not found: " + name);
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  PARTICIPANT INFORMATION");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("  " + participant);

        // Check if in a team
        for (Team team : formedTeams) {
            if (team.getMembers().contains(participant)) {
                System.out.println("\n  ✓ Assigned to Team " + team.getTeamId());
                return;
            }
        }
        System.out.println("\n  ⚠ Not yet assigned to a team");
    }
}