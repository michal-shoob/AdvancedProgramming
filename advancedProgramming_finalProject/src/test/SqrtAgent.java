package test;

/**
 * Agent that computes the square root of a single input value and publishes the result.
 *
 * <p>Subscribes to {@code subs[0]}.  Every numeric, non-negative message
 * triggers an immediate publish of {@code Math.sqrt(value)} to {@code pubs[0]}.
 * Negative values and NaN are silently ignored.</p>
 *
 * <p>Config-file format (3-line block):</p>
 * <pre>
 *   test.SqrtAgent
 *   A
 *   sqrtA
 * </pre>
 */
public class SqrtAgent implements Agent {

    /** Most recently received input value. */
    private double x = 0.0;

    private final String name = "SqrtAgent";
    private final String[] subs;
    private final String[] pubs;

    /**
     * Creates a {@code SqrtAgent}, subscribing to {@code subs[0]} and
     * registering as publisher of {@code pubs[0]}.
     *
     * @param subs subscription topics; must contain at least 1 entry
     * @param pubs publication topics;  must contain at least 1 entry
     * @throws IllegalArgumentException if the arrays are too short
     */
    public SqrtAgent(String[] subs, String[] pubs) {
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
     * Stores the incoming value and publishes its square root.
     * Ignores NaN and negative values (square root is undefined for negatives).
     *
     * @param topic topic name that triggered this callback
     * @param msg   the received message
     */
    @Override
    public void callback(String topic, Message msg) {
        if (topic == null || msg == null) return;
        double value = msg.asDouble;
        if (Double.isNaN(value) || value < 0) return;
        if (topic.equals(subs[0])) { x = value; publishSqrt(); }
    }

    /** Computes and publishes {@code Math.sqrt(x)}. */
    private void publishSqrt() {
        TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(Math.sqrt(x)));
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
