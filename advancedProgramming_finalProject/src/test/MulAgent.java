package test;

/**
 * Agent that multiplies the values of two input topics and publishes the product.
 *
 * <p>Subscribes to {@code subs[0]} (x) and {@code subs[1]} (y).  On every
 * callback the stored value for the triggering topic is updated and the current
 * product ({@code x * y}) is immediately published to {@code pubs[0]}.</p>
 *
 * <p>Non-numeric messages (NaN or unparseable text) are silently ignored.</p>
 *
 * <p>Config-file format (3-line block):</p>
 * <pre>
 *   test.MulAgent
 *   A, B
 *   A*B
 * </pre>
 */
public class MulAgent implements Agent {

    /** Current value of the first input topic. */
    private double x = 0.0;

    /** Current value of the second input topic. */
    private double y = 0.0;

    private final String name = "MulAgent";
    private final String[] subs;
    private final String[] pubs;

    /**
     * Creates a {@code MulAgent}, subscribing to the first two topics in
     * {@code subs} and registering as publisher of {@code pubs[0]}.
     *
     * @param subs subscription topics; must contain at least 2 entries
     * @param pubs publication topics;  must contain at least 1 entry
     * @throws IllegalArgumentException if the arrays are too short
     */
    public MulAgent(String[] subs, String[] pubs) {
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

    /** Resets both stored input values to {@code 0.0}. */
    @Override public void reset() { x = 0.0; y = 0.0; }

    /**
     * Updates the stored value for the triggering topic and publishes the
     * current product.  Ignores null inputs and non-numeric messages.
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

        if      (topic.equals(subs[0])) x = value;
        else if (topic.equals(subs[1])) y = value;
        else return;

        publishProduct();
    }

    /** Computes and publishes {@code x * y}. */
    private void publishProduct() {
        TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(x * y));
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
