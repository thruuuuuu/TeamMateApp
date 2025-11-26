package Threads;

import Entity.Participant;
import Exceptions.CSVFormatException;
import Exceptions.InvalidEmailException;
import Exceptions.InvalidSkillLevelException;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class CSVLoaderThread implements Callable<List<Participant>> {
    private final String filePath;

    public CSVLoaderThread(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<Participant> call() throws Exception {
        List<Participant> participants = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] data = line.split(",");
                if (data.length < 7) {
                    throw new CSVFormatException("Invalid CSV format at line " + lineNumber + ": Expected 7+ columns, found " + data.length);
                }

                try {
                    Participant p = getParticipant(data, lineNumber);
                    participants.add(p);

                } catch (NumberFormatException e) {
                    throw new CSVFormatException("Invalid number format at line " + lineNumber);
                }
            }

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("CSV file not found: " + filePath);
        }

        return participants;
    }

    private static Participant getParticipant(String[] data, int lineNumber) throws InvalidEmailException, InvalidSkillLevelException {
        String id = data[0].trim();
        String name = data[1].trim();
        String email = data[2].trim();
        String game = data[3].trim();
        int skill = Integer.parseInt(data[4].trim());
        String role = data[5].trim();
        int personalityScore = Integer.parseInt(data[6].trim());

        // Validate data
        if (name.isEmpty()) {
            throw new CSVFormatException("Empty name at line " + lineNumber);
        }
        if (!email.contains("@")) {
            throw new InvalidEmailException("Invalid email format at line " + lineNumber + ": " + email);
        }
        if (skill < 1 || skill > 10) {
            throw new InvalidSkillLevelException("Invalid skill level at line " + lineNumber + ": " + skill);
        }

        return new Participant(id, name, email, game, skill, role, personalityScore);
    }
}