package Enums;

public enum Role {
    STRATEGIST("Strategist"),
    ATTACKER("Attacker"),
    DEFENDER("Defender"),
    SUPPORTER("Supporter"),
    COORDINATOR("Coordinator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromInt(int choice) {
        if (choice >= 1 && choice <= values().length) {
            return values()[choice - 1];
        }
        return SUPPORTER; // Default
    }

    public static void displayOptions() {
        System.out.println("\nSelect your preferred role:");
        for (int i = 0; i < values().length; i++) {
            System.out.print((i + 1) + ". " + values()[i].getDisplayName());
            if (i < values().length - 1) System.out.print("  ");
        }
        System.out.println();
    }
}