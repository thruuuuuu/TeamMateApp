package Main;

import Entity.Participant;
import Enums.Game;
import Enums.Role;
import Exceptions.*;
import Log.Logger;
import Manager.TeamManager;

import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TeamMateApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final TeamManager teamManager = new TeamManager();
    private static String loggedInParticipantId = null;

    public static void main(String[] args) {
        Logger.info("=== TeamMate System Started ===");
        Logger.logSystemEvent("Application initialized");

        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   Welcome to TeamMate Formation System ║");
        System.out.println("╚════════════════════════════════════════╝");

        while (true) {
            System.out.println("\n=== SELECT USER TYPE ===");
            System.out.println("1. Organizer");
            System.out.println("2. Participant");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            int choice = getIntInput();
            Logger.debug("User selected main menu option: " + choice);

            switch (choice) {
                case 1:
                    Logger.info("Organizer mode selected");
                    organizerMenu();
                    break;
                case 2:
                    Logger.info("Participant mode selected");
                    participantLogin();
                    break;
                case 3:
                    Logger.info("User exiting application");
                    System.out.println("\nThank you for using TeamMate!");
                    Logger.logSystemEvent("Application shutdown");
                    scanner.close();
                    System.exit(0);
                default:
                    Logger.warning("Invalid main menu choice: " + choice);
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void organizerMenu() {
        Logger.info("Entered Organizer Menu");

        while (true) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║         ORGANIZER MENU                 ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("1. Upload Participants CSV");
            System.out.println("2. Form Teams");
            System.out.println("3. Load Previous Team Formation");
            System.out.println("4. View All Participants");
            System.out.println("5. View Formed Teams");
            System.out.println("6. View Remaining Participants");
            System.out.println("7. Back to Main Menu");
            System.out.print("Enter choice: ");

            int choice = getIntInput();
            Logger.debug("Organizer menu choice: " + choice);

            switch (choice) {
                case 1:
                    uploadCSV();
                    break;
                case 2:
                    formTeamsWithOptions();
                    break;
                case 3:
                    loadPreviousTeamFormation();
                    break;
                case 4:
                    Logger.logUserAction("Organizer", "Viewed all participants");
                    teamManager.viewAllParticipants();
                    break;
                case 5:
                    Logger.logUserAction("Organizer", "Viewed formed teams");
                    teamManager.viewFormedTeams();
                    break;
                case 6:
                    Logger.logUserAction("Organizer", "Viewed remaining participants");
                    teamManager.viewRemainingParticipants();
                    break;
                case 7:
                    Logger.info("Exiting Organizer Menu");
                    return;
                default:
                    Logger.warning("Invalid organizer menu choice: " + choice);
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void participantLogin() {
        Logger.info("Participant login initiated");

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║       PARTICIPANT LOGIN                ║");
        System.out.println("╚════════════════════════════════════════╝");

        scanner.nextLine();

        System.out.println("1. Login with Existing ID");
        System.out.println("2. Register as New Participant");
        System.out.println("3. Back to Main Menu");
        System.out.print("Enter choice: ");

        int choice = getIntInput();
        scanner.nextLine();

        Logger.debug("Participant login option: " + choice);

        switch (choice) {
            case 1:
                loginExistingParticipant();
                break;
            case 2:
                registerNewParticipant();
                break;
            case 3:
                Logger.info("Returned to main menu from login");
                return;
            default:
                Logger.warning("Invalid login choice: " + choice);
                System.out.println("Invalid choice.");
        }
    }

    private static void loginExistingParticipant() {
        System.out.print("\nEnter your Participant ID (e.g., P001): ");
        String participantId = scanner.nextLine().trim().toUpperCase();

        Logger.info("Login attempt for participant ID: " + participantId);

        try {
            if (teamManager.participantExists(participantId)) {
                loggedInParticipantId = participantId;
                Participant participant = teamManager.getParticipantById(participantId);

                System.out.println("\n✓ Login successful!");
                System.out.println("Welcome, " + participant.getName() + "!");

                Logger.logUserAction(participantId, "Logged in successfully");

                participantMenu();
            } else {
                Logger.warning("Failed login attempt - ID not found: " + participantId);
                System.out.println("\n✗ Error: Participant ID not found. Please check your ID or register as new participant.");
            }
        } catch (ParticipantNotFoundException e) {
            Logger.error("Login error for ID: " + participantId, e);
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private static void registerNewParticipant() {
        Logger.info("New participant registration started");
        System.out.println("\n=== NEW PARTICIPANT REGISTRATION ===");
        completeSurvey();
    }

    private static void participantMenu() {
        Logger.info("Participant " + loggedInParticipantId + " entered participant menu");

        while (true) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║         PARTICIPANT MENU               ║");
            System.out.println("║         (Logged in as: " + loggedInParticipantId + ")      ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("1. View My Information");
            System.out.println("2. View My Team Assignment");
            System.out.println("3. View All Formed Teams");
            System.out.println("4. Update My Profile");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");

            int choice = getIntInput();
            Logger.debug("Participant " + loggedInParticipantId + " menu choice: " + choice);

            switch (choice) {
                case 1:
                    Logger.logUserAction(loggedInParticipantId, "Viewed own information");
                    viewMyInfo();
                    break;
                case 2:
                    Logger.logUserAction(loggedInParticipantId, "Viewed team assignment");
                    viewMyTeamAssignment();
                    break;
                case 3:
                    Logger.logUserAction(loggedInParticipantId, "Viewed all formed teams");
                    teamManager.viewFormedTeams();
                    break;
                case 4:
                    updateMyProfile();
                    break;
                case 5:
                    Logger.logUserAction(loggedInParticipantId, "Logged out");
                    System.out.println("\n✓ Logged out successfully!");
                    loggedInParticipantId = null;
                    return;
                default:
                    Logger.warning("Invalid participant menu choice: " + choice);
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void uploadCSV() {
        System.out.print("\nEnter CSV file path (or press Enter for default 'participants.csv'): ");
        scanner.nextLine();
        String filePath = scanner.nextLine().trim();
        if (filePath.isEmpty()) {
            filePath = "participants.csv";
        }

        Logger.info("Organizer uploading CSV: " + filePath);
        teamManager.loadParticipantsFromCSV(filePath);
    }

    private static void loadPreviousTeamFormation() {
        System.out.print("\nEnter team formation CSV file path: ");
        scanner.nextLine();
        String filePath = scanner.nextLine().trim();

        if (filePath.isEmpty()) {
            Logger.warning("Empty file path provided for loading formation");
            System.out.println("✗ Error: File path cannot be empty.");
            return;
        }

        Logger.info("Loading previous formation from: " + filePath);
        teamManager.loadTeamFormationFromCSV(filePath);
    }

    private static void formTeamsWithOptions() {
        try {
            System.out.print("\nEnter team size (3-10): ");
            int size = getIntInput();

            Logger.info("Setting team size to: " + size);
            teamManager.setTeamSize(size);
            System.out.println("Team size set to: " + size);

            Logger.info("Starting team formation process");
            FormationStatistics stats = teamManager.formTeams();

            if (stats != null) {
                stats.display();
            }

            System.out.print("\nDo you want to save the teams to CSV? (Y/N): ");
            scanner.nextLine();
            String saveChoice = scanner.nextLine().trim().toUpperCase();

            if (saveChoice.equals("Y") || saveChoice.equals("YES")) {
                String autoFileName = generateTeamFileName(size);

                System.out.println("\nSuggested filename: " + autoFileName);
                System.out.print("Press Enter to use this name, or type a custom path: ");
                String filePath = scanner.nextLine().trim();

                if (filePath.isEmpty()) {
                    filePath = autoFileName;
                }

                Logger.info("Saving teams to: " + filePath);
                teamManager.saveTeamsToCSV(filePath);

                if (teamManager.hasRemainingParticipants()) {
                    System.out.print("\nSave remaining participants to the same file? (Y/N): ");
                    String saveRemaining = scanner.nextLine().trim().toUpperCase();

                    if (saveRemaining.equals("Y") || saveRemaining.equals("YES")) {
                        Logger.info("Appending remaining participants to: " + filePath);
                        teamManager.appendRemainingParticipantsToCSV(filePath);
                    }
                }
            } else {
                Logger.info("Teams not saved - user declined");
                System.out.println("Teams not saved.");
            }

        } catch (InvalidTeamSizeException e) {
            Logger.error("Invalid team size provided", e);
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private static void completeSurvey() {
        try {
            System.out.println("\n=== PARTICIPANT SURVEY ===\n");

            System.out.print("Enter your name: ");
            String name = scanner.nextLine().trim();

            if (name.isEmpty()) {
                Logger.warning("Empty name provided in survey");
                System.out.println("✗ Error: Name cannot be empty");
                return;
            }

            System.out.print("Enter your email: ");
            String email = scanner.nextLine().trim();

            if (isValidEmail(email)) {
                throw new InvalidEmailException("Invalid email format. Must contain '@'");
            }

            System.out.print("Enter your skill level (1-10): ");
            int skill = getIntInput();

            if (skill < 1 || skill > 10) {
                throw new InvalidSkillLevelException("Skill level must be between 1 and 10");
            }

            Game.displayOptions();
            System.out.print("Enter choice: ");
            int gameChoice = getIntInput();
            Game game = Game.fromInt(gameChoice);

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

            Logger.info("New participant registered: " + participant.getId() + " - " + name);
            Logger.logUserAction(participant.getId(), "Completed registration survey");

            System.out.println("\n✓ Registration completed successfully!");
            System.out.println("═══════════════════════════════════════");
            System.out.println("Your Participant ID: " + participant.getId());
            System.out.println("IMPORTANT: Save this ID for future login!");
            System.out.println("═══════════════════════════════════════");
            System.out.println("Your personality type: " + participant.getPersonalityType().getDisplayName());

            loggedInParticipantId = participant.getId();

            System.out.print("\nPress Enter to continue to your dashboard...");
            scanner.nextLine();

            participantMenu();

        } catch (InvalidRatingException | InvalidSkillLevelException | InvalidEmailException e) {
            Logger.error("Survey completion error", e);
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private static void viewMyInfo() {
        try {
            teamManager.viewParticipantInfo(loggedInParticipantId);
        } catch (ParticipantNotFoundException e) {
            Logger.error("Error viewing participant info", e);
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private static void viewMyTeamAssignment() {
        try {
            teamManager.viewParticipantTeamAssignment(loggedInParticipantId);
        } catch (ParticipantNotFoundException e) {
            Logger.error("Error viewing team assignment", e);
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private static void updateMyProfile() {
        Logger.logUserAction(loggedInParticipantId, "Started profile update");

        System.out.println("\n=== UPDATE PROFILE ===");
        System.out.println("What would you like to update?");
        System.out.println("1. Email");
        System.out.println("2. Skill Level");
        System.out.println("3. Preferred Game");
        System.out.println("4. Preferred Role");
        System.out.println("5. Cancel");
        System.out.print("Enter choice: ");

        int choice = getIntInput();
        scanner.nextLine();

        try {
            switch (choice) {
                case 1:
                    System.out.print("Enter new email: ");
                    String newEmail = scanner.nextLine().trim();
                    if (isValidEmail(newEmail)) {
                        throw new InvalidEmailException("Invalid email format");
                    }
                    teamManager.updateParticipantEmail(loggedInParticipantId, newEmail);
                    Logger.logUserAction(loggedInParticipantId, "Updated email to: " + newEmail);
                    System.out.println("✓ Email updated successfully!");
                    break;
                case 2:
                    System.out.print("Enter new skill level (1-10): ");
                    int newSkill = getIntInput();
                    if (newSkill < 1 || newSkill > 10) {
                        throw new InvalidSkillLevelException("Skill level must be between 1 and 10");
                    }
                    teamManager.updateParticipantSkill(loggedInParticipantId, newSkill);
                    Logger.logUserAction(loggedInParticipantId, "Updated skill level to: " + newSkill);
                    System.out.println("✓ Skill level updated successfully!");
                    break;
                case 3:
                    Game.displayOptions();
                    System.out.print("Enter choice: ");
                    int gameChoice = getIntInput();
                    Game newGame = Game.fromInt(gameChoice);
                    teamManager.updateParticipantGame(loggedInParticipantId, newGame);
                    Logger.logUserAction(loggedInParticipantId, "Updated game to: " + newGame.getDisplayName());
                    System.out.println("✓ Preferred game updated successfully!");
                    break;
                case 4:
                    Role.displayOptions();
                    System.out.print("Enter choice: ");
                    int roleChoice = getIntInput();
                    Role newRole = Role.fromInt(roleChoice);
                    teamManager.updateParticipantRole(loggedInParticipantId, newRole);
                    Logger.logUserAction(loggedInParticipantId, "Updated role to: " + newRole.getDisplayName());
                    System.out.println("✓ Preferred role updated successfully!");
                    break;
                case 5:
                    Logger.debug("Profile update cancelled");
                    return;
                default:
                    Logger.warning("Invalid profile update choice: " + choice);
                    System.out.println("Invalid choice.");
            }
        } catch (ParticipantNotFoundException | InvalidEmailException | InvalidSkillLevelException e) {
            Logger.error("Profile update error for " + loggedInParticipantId, e);
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private static int getIntInput() {
        while (!scanner.hasNextInt()) {
            Logger.warning("Invalid integer input received");
            System.out.print("Invalid input. Please enter a number: ");
            scanner.next();
        }
        return scanner.nextInt();
    }

    private static int getRatingInput() throws InvalidRatingException {
        System.out.print("Rating: ");
        int rating = getIntInput();

        if (rating < 1 || rating > 5) {
            Logger.warning("Invalid rating provided: " + rating);
            throw new InvalidRatingException("Rating must be between 1 and 5. You entered: " + rating);
        }

        return rating;
    }

    private static boolean isValidEmail(String email) {
        return email == null || !email.contains("@") || email.length() <= 3;
    }

    private static String generateTeamFileName(int teamSize) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = now.format(formatter);
        return "TeamFormations/teamsSize" + teamSize + "_" + timestamp + ".csv";
    }
}