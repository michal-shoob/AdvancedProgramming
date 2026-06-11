package test;

/**
 * Agent that raises a base value to an exponent and publishes the result.
 *
 * <p>Subscribes to {@code subs[0]} (base) and {@code subs[1]} (exponent).
 * On every callback the stored value for the triggering topic is updated and
 * {@code Math.pow(base, exponent)} is published to {@code pubs[0]}.</p>
 *
 * <p>Non-numeric messages (NaN or unparseable text) are silently ignored.</p>
 *
 * <p>Config-file format (3-line block):</p>
 * <pre>
 *   test.PowerAgent
 *   A, B
 *   A^B
 * </pre>
 */
public class PowerAgent implements Agent {

    /** Current base value (from {@code subs[0]}). */
    private double base = 0.0;

    /** Current exponent value (from {@code subs[1]}). */
    private double exponent = 0.0;

    private final String name = "PowerAgent";
    private final String[] subs;
    private final String[] pubs;

    /**
     * Creates a {@code PowerAgent}, subscribing to the first two topics in
     * {@code subs} and registering as publisher of {@code pubs[0]}.
     *
     * @param subs subscription topics; must contain at least 2 entries
     * @param pubs publication topics;  must contain at least 1 entry
     * @throws IllegalArgumentException if the arrays are too short
     */
    public PowerAgent(String[] subs, String[] pubs) {
        if (subs == null || subs.length < 2)
            throw new IllegalArgumentException("At least two subscription topics are required.");
        if (pubs == null || pubs.length < 1)
            throw new IllegalArgumentException("At least one publisher topic is required.");

        this.subs = subs;
        this.pubs = pubs;

        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(subs[0]).subscribe(this);
        tm.getTopic(subs[1]).subscribe(this);
        tm.getTopic(pubs[0]).addPublisher(this);
    }

    /** {@inheritDoc} */
    @Override public String getName() { return name; }

    /** Resets both stored values to {@code 0.0}. */
    @Override public void reset() { base = 0.0; exponent = 0.0; }

    /**
     * Updates the stored base or exponent and publishes {@code base ^ exponent}.
     * Ignores null inputs and non-numeric messages.
     *
     * @param topic topic name that triggered this callback
     * @param msg   the received message
     */
    @Override
    public void callback(String topic, Message msg) {
        if (topic == null || msg == null) return;
        double value;
        try {
            value = Double.parseDouble(msg.asText);
            if (Double.isNaN(value)) return;
        } catch (Exception e) { return; }

        if      (topic.equals(subs[0])) base     = value;
        else if (topic.equals(subs[1])) exponent = value;
        else return;

        publishPower();
    }

    /** Computes and publishes {@code Math.pow(base, exponent)}. */
    private void publishPower() {
        TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(Math.pow(base, exponent)));
    }

    /** Unsubscribes from all topics and resets state. */
    @Override
    public void close() {
        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(subs[0]).unsubscribe(this);
        tm.getTopic(subs[1]).unsubscribe(this);
        tm.getTopic(pubs[0]).removePublisher(this);
        reset();
    }
}
