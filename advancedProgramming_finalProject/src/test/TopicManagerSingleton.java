package test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holder class that exposes the application-wide singleton {@link TopicManager}.
 *
 * <p>The singleton is obtained via:</p>
 * <pre>
 *   TopicManagerSingleton.get().getTopic("MyTopic");
 * </pre>
 *
 * <p>Lazy initialisation is achieved through the
 * <em>Initialization-on-demand holder</em> idiom: {@link TopicManager} is a
 * static inner class and is loaded by the JVM only when {@link #get()} is
 * first called, giving thread-safe lazy initialisation with no locking.</p>
 */
public class TopicManagerSingleton {

    /** Prevents instantiation of the outer holder class. */
    private TopicManagerSingleton() {}

    /**
     * Returns the single shared {@link TopicManager} instance.
     *
     * @return the application-wide {@code TopicManager}
     */
    public static TopicManager get() {
        return TopicManager.instance;
    }

    /**
     * Thread-safe registry of all named {@link Topic} objects in the application.
     *
     * <p>Uses a {@link ConcurrentHashMap} so that topics can be created and
     * looked up from multiple threads without external synchronisation.</p>
     */
    public static class TopicManager {

        /** The single instance, created when this class is first loaded. */
        private static final TopicManager instance = new TopicManager();

        /** Maps topic name to its {@link Topic} object. */
        private final Map<String, Topic> topics;

        /** Private constructor — use {@link TopicManagerSingleton#get()}. */
        private TopicManager() {
            this.topics = new ConcurrentHashMap<>();
        }

        /**
         * Returns the topic with the given name, creating it if it does not
         * yet exist.  The operation is atomic, so concurrent callers requesting
         * the same name always receive the same {@link Topic} instance.
         *
         * @param name the topic name; must not be {@code null} or empty
         * @return the existing or newly created topic, or {@code null} if
         *         {@code name} is {@code null} or empty
         */
        public Topic getTopic(String name) {
            if (name == null || name.isEmpty()) {
                return null;
            }
            return topics.computeIfAbsent(name, Topic::new);
        }

        /**
         * Returns an unmodifiable view of all topics currently registered.
         *
         * @return an unmodifiable collection of {@link Topic} objects
         */
        public Collection<Topic> getTopics() {
            return Collections.unmodifiableCollection(topics.values());
        }

        /**
         * Removes all topics from the registry.
         * Typically called when loading a new configuration to start with a
         * clean state.
         */
        public void clear() {
            topics.clear();
        }
    }
}
