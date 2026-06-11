package test;

/**
 * Agent that increments a single input value by 1 and publishes the result.
 *
 * <p>Subscribes to {@code subs[0]}.  Every numeric message received triggers
 * an immediate publish of {@code value + 1} to {@code pubs[0]}.</p>
 *
 * <p>Non-numeric messages (NaN) are silently ignored.</p>
 *
 * <p>Config-file format (3-line block):</p>
 * <pre>
 *   test.IncAgent
 *   A
 *   A+1
 * </pre>
 */
public class IncAgent implements Agent {

    /** Most recently received input value. */
    private double x = 0.0;

    private final String name = "IncAgent";
    private final String[] subs;
    private final String[] pubs;

    /**
     * Creates an {@code IncAgent}, subscribing to {@code subs[0]} and
     * registering as publisher of {@code pubs[0]}.
     *
     * @param subs subscription topics; must contain at least 1 entry
     * @param pubs publication topics;  must contain at least 1 entry
     * @throws IllegalArgumentException if the arrays are too short
     */
    public IncAgent(String[] subs, String[] pubs) {
        if (subs == null || subs.length < 1)
            throw new IllegalArgumentException("At least one subscription topic is required.");
        if (pubs == null || pubs.length < 1)
            throw new IllegalArgumentException("At least one publisher topic is required.");

        this.subs = subs;
        this.pubs = pubs;

        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(subs[0]).subscribe(this);
        tm.getTopic(pubs[0]).addPublisher(this);
    }

    /** {@inheritDoc} */
    @Override public String getName() { return name; }

    /** Resets the stored input value to {@code 0.0}. */
    @Override public void reset() { x = 0.0; }

    /**
     * Stores the incoming value and publishes {@code value + 1}.
     * NaN messages are silently ignored.
     *
     * @param topic topic name that triggered this callback
     * @param msg   the received message
     */
    @Override
    public void callback(String topic, Message msg) {
        if (topic == null || msg == null) return;
        double value = msg.asDouble;
        if (Double.isNaN(value)) return;
        if (topic.equals(subs[0])) { x = value; publishIncrement(); }
    }

    /** Computes and publishes {@code x + 1}. */
    private void publishIncrement() {
        TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(x + 1));
    }

    /** Unsubscribes from all topics and resets state. */
    @Override
    public void close() {
        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(subs[0]).unsubscribe(this);
        tm.getTopic(pubs[0]).removePublisher(this);
        reset();
    }
}
