package test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GenericConfig implements Config {

    private String confFile;
    private final List<ParallelAgent> parallelAgents = new ArrayList<>();

    public void setConfFile(String confFile) {
        if (confFile == null || confFile.trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration file path cannot be null or empty");
        }

        this.confFile = confFile;
    }

    @Override
    public void create() {
        close(); // סוגר סוכנים קודמים אם create נקרא שוב

        if (confFile == null) {
            throw new IllegalStateException("Configuration file path not set");
        }

        List<String> lines;

        try {
            lines = Files.readAllLines(Paths.get(confFile));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config file: " + confFile, e);
        }

        lines.removeIf(line -> line == null || line.trim().isEmpty());

        if (lines.isEmpty() || lines.size() % 3 != 0) {
            throw new IllegalArgumentException(
                    "Config file format error: number of non-empty lines must be divisible by 3"
            );
        }

        for (int i = 0; i < lines.size(); i += 3) {
            String className = lines.get(i).trim();
            String[] subs = splitTopics(lines.get(i + 1));
            String[] pubs = splitTopics(lines.get(i + 2));

            try {
                Class<?> clazz = Class.forName(className);

                Constructor<?> constructor =
                        clazz.getConstructor(String[].class, String[].class);

                Object obj =
                        constructor.newInstance((Object) subs, (Object) pubs);

                if (!(obj instanceof Agent)) {
                    throw new IllegalArgumentException(className + " does not implement Agent");
                }

                ParallelAgent pa = new ParallelAgent((Agent) obj, 10);
                parallelAgents.add(pa);

            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate agent: " + className, e);
            }
        }
    }

    private String[] splitTopics(String line) {
        if (line == null || line.trim().isEmpty()) {
            return new String[0];
        }

        String[] parts = line.split(",");

        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        return parts;
    }

    @Override
    public String getName() {
        return "GenericConfig";
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void close() {
        for (ParallelAgent pa : parallelAgents) {
            if (pa != null) {
                pa.close();
            }
        }

        parallelAgents.clear();
    }
}