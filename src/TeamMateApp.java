import java.util.Scanner;

public class TeamMateApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final TeamManager teamManager = new TeamManager();

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   Welcome to TeamMate Formation System  ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        while (true) {
            System.out.println("\n=== SELECT USER TYPE ===");
            System.out.println("1. Organizer");
            System.out.println("2. Participant");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    organizerMenu();
                    break;
                case 2:
                    participantMenu();
                    break;
                case 3:
                    System.out.println("\nThank you for using TeamMate!");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void organizerMenu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║         ORGANIZER MENU                  ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("1. Upload CSV Data");
            System.out.println("2. Define Team Size");
            System.out.println("3. Form Teams");
            System.out.println("4. Save Teams to CSV");
            System.out.println("5. View All Participants");
            System.out.println("6. View Formed Teams");
            System.out.println("7. Back to Main Menu");
            System.out.print("Enter choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    uploadCSV();
                    break;
                case 2:
                    defineTeamSize();
                    break;
                case 3:
                    formTeams();
                    break;
                case 4:
                    saveTeams();
                    break;
                case 5:
                    teamManager.viewAllParticipants();
                    break;
                case 6:
                    teamManager.viewFormedTeams();
                    break;
                case 7:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void participantMenu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║         PARTICIPANT MENU                ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("1. Complete Survey");
            System.out.println("2. View My Information");
            System.out.println("3. View Formed Teams");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    completeSurvey();
                    break;
                case 2:
                    viewMyInfo();
                    break;
                case 3:
                    teamManager.viewFormedTeams();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void uploadCSV() {
        System.out.print("\nEnter CSV file path (or press Enter for default 'participants.csv'): ");
        scanner.nextLine(); // Clear buffer
        String filePath = scanner.nextLine().trim();
        if (filePath.isEmpty()) {
            filePath = "participants.csv";
        }
        teamManager.loadParticipantsFromCSV(filePath);
    }

    private static void defineTeamSize() {
        System.out.print("\nEnter team size (3-10): ");
        int size = getIntInput();
        if (size < 3 || size > 10) {
            System.out.println("Invalid team size. Must be between 3 and 10.");
        } else {
            teamManager.setTeamSize(size);
            System.out.println("Team size set to: " + size);
        }
    }

    private static void formTeams() {
        teamManager.formTeams();
    }

    private static void saveTeams() {
        System.out.print("\nEnter output file path (or press Enter for default 'formed_teams.csv'): ");
        scanner.nextLine(); // Clear buffer
        String filePath = scanner.nextLine().trim();
        if (filePath.isEmpty()) {
            filePath = "formed_teams.csv";
        }
        teamManager.saveTeamsToCSV(filePath);
    }

    private static void completeSurvey() {
        System.out.println("\n=== PARTICIPANT SURVEY ===\n");

        scanner.nextLine(); // Clear buffer
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        System.out.print("Enter your skill level (1-10): ");
        int skill = getIntInput();

        System.out.println("\nSelect your preferred game/sport:");
        System.out.println("1. Valorant  2. Dota  3. FIFA  4. Basketball  5. Badminton");
        System.out.print("Enter choice: ");
        int gameChoice = getIntInput();
        String[] games = {"Valorant", "Dota", "FIFA", "Basketball", "Badminton"};
        String game = (gameChoice >= 1 && gameChoice <= 5) ? games[gameChoice - 1] : "Valorant";

        System.out.println("\nSelect your preferred role:");
        System.out.println("1. Strategist  2. Attacker  3. Defender  4. Supporter  5. Coordinator");
        System.out.print("Enter choice: ");
        int roleChoice = getIntInput();
        String[] roles = {"Strategist", "Attacker", "Defender", "Supporter", "Coordinator"};
        String role = (roleChoice >= 1 && roleChoice <= 5) ? roles[roleChoice - 1] : "Supporter";

        System.out.println("\n=== PERSONALITY SURVEY ===");
        System.out.println("Rate each statement from 1 (Strongly Disagree) to 5 (Strongly Agree)\n");

        System.out.println("Q1: I enjoy taking the lead and guiding others during group activities.");
        System.out.print("Rating: ");
        int q1 = getIntInput();

        System.out.println("\nQ2: I prefer analyzing situations and coming up with strategic solutions.");
        System.out.print("Rating: ");
        int q2 = getIntInput();

        System.out.println("\nQ3: I work well with others and enjoy collaborative teamwork.");
        System.out.print("Rating: ");
        int q3 = getIntInput();

        System.out.println("\nQ4: I am calm under pressure and can help maintain team morale.");
        System.out.print("Rating: ");
        int q4 = getIntInput();

        System.out.println("\nQ5: I like making quick decisions and adapting in dynamic situations.");
        System.out.print("Rating: ");
        int q5 = getIntInput();

        int totalScore = (q1 + q2 + q3 + q4 + q5) * 4;

        Participant participant = new Participant(name, game, skill, role, totalScore);
        teamManager.addParticipant(participant);

        System.out.println("\n✓ Survey completed successfully!");
        System.out.println("Your personality type: " + participant.getPersonalityType());
    }

    private static void viewMyInfo() {
        scanner.nextLine(); // Clear buffer
        System.out.print("\nEnter your name: ");
        String name = scanner.nextLine();
        teamManager.viewParticipantInfo(name);
    }

    private static int getIntInput() {
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter a number: ");
            scanner.next();
        }
        return scanner.nextInt();
    }
}