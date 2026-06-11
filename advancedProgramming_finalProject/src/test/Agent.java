package test;

/**
 * Core interface that every agent in the computational graph must implement.
 *
 * <p>An agent reacts to messages arriving on topics it has subscribed to,
 * performs a computation, and typically publishes a result to one or more
 * output topics.  Agents are registered with a {@link TopicManagerSingleton}
 * and wired together through named {@link Topic} channels.</p>
 *
 * <p>Implementations must be thread-safe when wrapped inside a
 * {@link ParallelAgent}.</p>
 */
public interface Agent {

    /**
     * Returns the display name of this agent.
     *
     * @return a non-null, human-readable identifier (e.g. {@code "PlusAgent"})
     */
    String getName();

    /**
     * Resets the agent's internal state to its initial values.
     * Called automatically by {@link ParallelAgent#close()} and may also be
     * invoked manually between test runs.
     */
    void reset();

    /**
     * Invoked whenever a message arrives on a topic this agent has subscribed to.
     *
     * @param topic the name of the topic that published the message
     * @param msg   the message that was published; implementations should
     *              tolerate {@code null} gracefully (ignore and return)
     */
    void callback(String topic, Message msg);

    /**
     * Releases all resources held by this agent.
     * After this call the agent must unsubscribe from all input topics and
     * deregister itself from all output topics.
     */
    void close();
}
