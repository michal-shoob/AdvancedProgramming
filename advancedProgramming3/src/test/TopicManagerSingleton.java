package test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Outer class whose only purpose is to expose the get() method
// that returns the single instance of TopicManager
public class TopicManagerSingleton {

    // Prevent instantiation of the outer class
    private TopicManagerSingleton() {}

    // Returns the single instance of TopicManager.
    // The inner class is only loaded by the JVM when this method is first called,
    // guaranteeing lazy initialization and thread safety without synchronized or volatile
    public static TopicManager get() {
        return TopicManager.instance;
    }

    // Inner static class - loaded on demand (only when first accessed)
    // This gives us lazy + thread-safe singleton for free via the JVM class loading mechanism
    public static class TopicManager {

        // The single instance - created once when the class is first loaded
        private static final TopicManager instance = new TopicManager();

        // Maps topic name -> Topic object
        // Using Map interface for flexibility, ConcurrentHashMap for concurrency safety
        private final Map<String, Topic> topics;

        // Private constructor - no one outside this class can instantiate TopicManager
        private TopicManager() {
            this.topics = new ConcurrentHashMap<>();
        }

        // Returns existing topic by name, or creates and stores a new one if it doesn't exist.
        // Returns null if name is null or empty - a topic must have a valid name.
        // computeIfAbsent is atomic - safe to call from multiple threads simultaneously
        public Topic getTopic(String name) {
            if (name == null || name.isEmpty()) {
                return null;
            }
            return topics.computeIfAbsent(name, Topic::new);
        }

        // Returns an unmodifiable view of all topics - prevents external modification
        public Collection<Topic> getTopics() {
            return Collections.unmodifiableCollection(topics.values());
        }

        // Clears all topics from the map - safe to call even if map is already empty
        public void clear() {
            topics.clear();
        }
    }
}