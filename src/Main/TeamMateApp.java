package Main;

import Entity.Participant;
import Enums.Game;
import Enums.Role;  // FIXED: Changed from javax.management.relation.Role
import Enums.PersonalityType;
import Exceptions.*;
import Manager.TeamManager;

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
                    scanner.close();
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
            System.out.println("2. Form Teams");
            System.out.println("3. View All Participants");
            System.out.println("4. View Formed Teams");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    uploadCSV();
                    break;
                case 2:
                    formTeamsWithOptions();
                    break;
                case 3:
                    teamManager.viewAllParticipants();
                    break;
                case 4:
                    teamManager.viewFormedTeams();
                    break;
                case 5:
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

    private static void formTeamsWithOptions() {
        try {
            // Step 1: Define team size with validation
            System.out.print("\nEnter team size (3-10): ");
            int size = getIntInput();
            teamManager.setTeamSize(size);
            System.out.println("Team size set to: " + size);

            // Step 2: Form teams and get statistics
            FormationStatistics stats = teamManager.formTeams();

            // Step 3: Display statistics
            if (stats != null) {
                stats.display();
            }

            // Step 4: Ask to save
            System.out.print("\nDo you want to save the teams to CSV? (Y/N): ");
            scanner.nextLine(); // Clear buffer
            String saveChoice = scanner.nextLine().trim().toUpperCase();

            if (saveChoice.equals("Y") || saveChoice.equals("YES")) {
                System.out.print("Enter output file path (or press Enter for default 'formed_teams.csv'): ");
                String filePath = scanner.nextLine().trim();
                if (filePath.isEmpty()) {
                    filePath = "formed_teams.csv";
                }
                teamManager.saveTeamsToCSV(filePath);
            } else {
                System.out.println("Teams not saved.");
            }

        } catch (InvalidTeamSizeException e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private static void completeSurvey() {
        try {
            System.out.println("\n=== PARTICIPANT SURVEY ===\n");

            scanner.nextLine(); // Clear buffer
            System.out.print("Enter your name: ");
            String name = scanner.nextLine().trim();

            if (name.isEmpty()) {
                System.out.println("✗ Error: Name cannot be empty");
                return;
            }

            System.out.print("Enter your email: ");
            String email = scanner.nextLine().trim();

            if (!isValidEmail(email)) {
                throw new InvalidEmailException("Invalid email format. Must contain '@'");
            }

            System.out.print("Enter your skill level (1-10): ");
            int skill = getIntInput();

            if (skill < 1 || skill > 10) {
                throw new InvalidSkillLevelException("Skill level must be between 1 and 10");
            }

            // Game selection with enum
            Game.displayOptions();
            System.out.print("Enter choice: ");
            int gameChoice = getIntInput();
            Game game = Game.fromInt(gameChoice);

            // Role selection with enum
            Role.displayOptions();
            System.out.print("Enter choice: ");
            int roleChoice = getIntInput();
            Role role = Role.fromInt(roleChoice);

            System.out.println("\n=== PERSONALITY SURVEY ===");
            System.out.println("Rate each statement from 1 (Strongly Disagree) to 5 (Strongly Agree)\n");

            System.out.println("Q1: I enjoy taking the lead and guiding others during group activities.");
            int q1 = getRatingInput();

            System.out.println("\nQ2: I prefer analyzing situations and coming up with strategic solutions.");
            int q2 = getRatingInput();

            System.out.println("\nQ3: I work well with others and enjoy collaborative teamwork.");
            int q3 = getRatingInput();

            System.out.println("\nQ4: I am calm under pressure and can help maintain team morale.");
            int q4 = getRatingInput();

            System.out.println("\nQ5: I like making quick decisions and adapting in dynamic situations.");
            int q5 = getRatingInput();

            int totalScore = (q1 + q2 + q3 + q4 + q5) * 4;

            Participant participant = new Participant(name, email, game, skill, role, totalScore);
            teamManager.addParticipant(participant);

            System.out.println("\n✓ Survey completed successfully!");
            System.out.println("Your participant ID: " + participant.getId());
            System.out.println("Your personality type: " + participant.getPersonalityType().getDisplayName());

        } catch (InvalidRatingException | InvalidSkillLevelException | InvalidEmailException e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private static void viewMyInfo() {
        try {
            scanner.nextLine(); // Clear buffer
            System.out.print("\nEnter your name: ");
            String name = scanner.nextLine().trim();
            teamManager.viewParticipantInfo(name);
        } catch (ParticipantNotFoundException e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private static int getIntInput() {
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter a number: ");
            scanner.next();
        }
        return scanner.nextInt();
    }

    private static int getRatingInput() throws InvalidRatingException {
        System.out.print("Rating: ");
        int rating = getIntInput();

        if (rating < 1 || rating > 5) {
            throw new InvalidRatingException("Rating must be between 1 and 5. You entered: " + rating);
        }

        return rating;
    }

    private static boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.length() > 3;
    }
}