package game_s1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

final class ScoreRepository {
    private static final Pattern NAME_PATTERN =
            Pattern.compile("\"name\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern LEVEL_PATTERN =
            Pattern.compile("\"level\"\\s*:\\s*(\\d+)");

    private final Path file;

    ScoreRepository(Path file) {
        this.file = file;
    }

    ScoreRecord load() {
        if (!Files.exists(file)) {
            return new ScoreRecord("None", 0);
        }

        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            Matcher nameMatch = NAME_PATTERN.matcher(json);
            Matcher levelMatch = LEVEL_PATTERN.matcher(json);
            String name = nameMatch.find() ? nameMatch.group(1) : "None";
            int level = levelMatch.find() ? Integer.parseInt(levelMatch.group(1)) : 0;
            return new ScoreRecord(name, level);
        } catch (IOException | NumberFormatException error) {
            showError("Could not read scores.json: " + error.getMessage());
            return new ScoreRecord("None", 0);
        }
    }

    void save(ScoreRecord record) {
        String safeName = record.name().replace("\\", "\\\\").replace("\"", "\\\"");
        String json = "{\n  \"name\": \"" + safeName + "\",\n  \"level\": "
                + record.level() + "\n}\n";

        try {
            Files.writeString(file, json, StandardCharsets.UTF_8);
        } catch (IOException error) {
            showError("Could not save scores.json: " + error.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                "Score file error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
