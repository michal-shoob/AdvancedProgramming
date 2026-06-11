package test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * File-based implementation of {@link Config} that instantiates agents
 * reflectively from a plain-text configuration file.
 *
 * <h2>Config-file format</h2>
 * <p>The file contains one or more 3-line blocks (blank lines between blocks
 * are ignored):</p>
 * <pre>
 *   &lt;fully-qualified class name&gt;
 *   &lt;comma-separated subscription topics&gt;
 *   &lt;comma-separated publication topics&gt;
 * </pre>
 * <p>Example:</p>
 * <pre>
 *   test.PlusAgent
 *   A, B
 *   A+B
 * </pre>
 *
 * <p>Each class must have a public constructor {@code (String[] subs, String[] pubs)}
 * and must implement {@link Agent}.  Every agent is wrapped in a
 * {@link ParallelAgent} with a queue capacity of 10.</p>
 */
public class GenericConfig implements Config {

    /** Path to the configuration file. */
    private String confFile;

    /** All {@link ParallelAgent} wrappers created by the most recent {@link #create()} call. */
    private final List<ParallelAgent> parallelAgents = new ArrayList<>();

    /**
     * Sets the path to the configuration file that {@link #create()} will read.
     *
     * @param confFile absolute or relative path to a {@code .conf} file
     * @throws IllegalArgumentException if {@code confFile} is {@code null} or blank
     */
    public void setConfFile(String confFile) {
        if (confFile == null || confFile.trim().isEmpty())
            throw new IllegalArgumentException("Configuration file path cannot be null or empty");
        this.confFile = confFile;
    }

    /**
     * Reads the configuration file and instantiates all agents described in it.
     * Any agents from a previous call are closed first.
     *
     * @throws IllegalStateException    if {@link #setConfFile(String)} has not been called
     * @throws RuntimeException         if the file cannot be read or an agent cannot be instantiated
     * @throws IllegalArgumentException if the non-empty line count is not divisible by 3
     */
    @Override
    public void create() {
        close();

        if (confFile == null)
            throw new IllegalStateException("Configuration file path not set");

        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(confFile));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config file: " + confFile, e);
        }

        lines.removeIf(line -> line == null || line.trim().isEmpty());

        if (lines.isEmpty() || lines.size() % 3 != 0)
            throw new IllegalArgumentException(
                    "Config file format error: number of non-empty lines must be divisible by 3");

        for (int i = 0; i < lines.size(); i += 3) {
            String className = lines.get(i).trim();
            String[] subs = splitTopics(lines.get(i + 1));
            String[] pubs = splitTopics(lines.get(i + 2));

            try {
                Class<?> clazz = Class.forName(className);
                Constructor<?> constructor = clazz.getConstructor(String[].class, String[].class);
                Object obj = constructor.newInstance((Object) subs, (Object) pubs);

                if (!(obj instanceof Agent))
                    throw new IllegalArgumentException(className + " does not implement Agent");

                parallelAgents.add(new ParallelAgent((Agent) obj, 10));
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate agent: " + className, e);
            }
        }
    }

    /**
     * Splits a comma-separated topic line into a trimmed string array.
     *
     * @param line e.g. {@code "A, B, C"}
     * @return trimmed array of topic names; empty array if {@code line} is blank
     */
    private String[] splitTopics(String line) {
        if (line == null || line.trim().isEmpty()) return new String[0];
        String[] parts = line.split(",");
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
        return parts;
    }

    /** {@inheritDoc} */
    @Override public String getName() { return "GenericConfig"; }

    /** {@inheritDoc} */
    @Override public int getVersion() { return 1; }

    /**
     * Stops and releases all agents created by the most recent {@link #create()} call.
     */
    @Override
    public void close() {
        for (ParallelAgent pa : parallelAgents) { if (pa != null) pa.close(); }
        parallelAgents.clear();
    }
}
