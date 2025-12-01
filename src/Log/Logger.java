package Log;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = "logs/teammate_system.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static boolean loggingEnabled = true;

    static {
        // Create logs directory if it doesn't exist
        File logDir = new File("logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    public enum Level {
        INFO, WARNING, ERROR, DEBUG
    }

    public static void setLoggingEnabled(boolean enabled) {
        loggingEnabled = enabled;
    }

    public static void log(Level level, String message) {
        if (!loggingEnabled) return;

        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] [%s] %s%n", timestamp, level, message);

        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.print(logEntry);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void warning(String message) {
        log(Level.WARNING, message);
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }

    public static void error(String message, Exception e) {
        log(Level.ERROR, message + " - " + e.getMessage());
    }

    public static void debug(String message) {
        log(Level.DEBUG, message);
    }

    public static void logUserAction(String userId, String action) {
        info(String.format("User[%s] Action: %s", userId, action));
    }

    public static void logSystemEvent(String event) {
        info(String.format("System Event: %s", event));
    }

    public static void clearLog() {
        try (PrintWriter pw = new PrintWriter(LOG_FILE)) {
            pw.print("");
            info("Log file cleared");
        } catch (IOException e) {
            error("Failed to clear log file", e);
        }
    }
}