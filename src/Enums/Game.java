package Enums;

public enum Game {
    CHESS("Chess"),
    FIFA("FIFA"),
    BASKETBALL("Basketball"),
    CSGO("CS:GO"),
    DOTA2("DOTA 2"),
    VALORANT("Valorant");

    private final String displayName;

    Game(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Game fromInt(int choice) {
        if (choice >= 1 && choice <= values().length) {
            return values()[choice - 1];
        }
        return CHESS; // Default
    }

    public static void displayOptions() {
        System.out.println("\nSelect your preferred game/sport:");
        for (int i = 0; i < values().length; i++) {
            System.out.println((i + 1) + ". " + values()[i].getDisplayName());
        }
    }
}