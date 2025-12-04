package Entity;

public class Organizer {
    private final String id;
    private final String name;
    private final String email;
    private final String password;

    public Organizer(String id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Constructor without ID (for new organizer registration - auto-generate ID)
    public Organizer(String name, String email, String password) {
        this.id = generateId();
        this.name = name;
        this.email = email;
        this.password = password;
    }

    private String generateId() {
        return "ORG" + String.format("%03d", (int)(Math.random() * 1000));
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

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return String.format("Organizer[ID: %s, Name: %s, Email: %s]", id, name, email);
    }
}