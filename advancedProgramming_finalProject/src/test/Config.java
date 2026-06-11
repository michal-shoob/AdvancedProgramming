package test;

/**
 * Lifecycle interface for a configuration that builds and manages a set of agents.
 *
 * <p>Implementations (e.g. {@link GenericConfig}) read an external description
 * of the computational graph (agent class names, subscription topics, publication
 * topics) and instantiate the corresponding agents.  Calling {@link #close()}
 * tears down all agents created by this configuration.</p>
 */
public interface Config {

    /**
     * Instantiates and starts all agents described by this configuration.
     * If agents were previously created by an earlier call to {@code create()},
     * they must be closed before the new ones are created.
     */
    void create();

    /**
     * Returns the human-readable name of this configuration.
     *
     * @return a non-null configuration name
     */
    String getName();

    /**
     * Returns the version number of this configuration.
     *
     * @return a positive integer version
     */
    int getVersion();

    /**
     * Stops and releases all agents created by the most recent {@link #create()} call.
     */
    void close();
}
