package test;

import java.util.function.BinaryOperator;
import test.TopicManagerSingleton.TopicManager;

/**
 * Generic binary-operation agent that subscribes to two input topics, applies
 * a configurable {@link BinaryOperator} to their values, and publishes the
 * result to one output topic.
 *
 * <p>The computation fires as soon as <em>both</em> inputs have been received
 * at least once.  After publishing, the stored values are reset via
 * {@link #reset()} so the next pair of inputs triggers a fresh computation.</p>
 *
 * <p>Concrete subclasses (e.g. {@link MulAgent}, {@link DivAgent}) simply
 * pass the appropriate operator to this constructor.</p>
 */
public class BinOpAgent implements Agent {

    /** Display name returned by {@link #getName()}. */
    private final String name;

    /** Name of the first input topic ({@code subs[0]}). */
    private final String input1;

    /** Name of the second input topic ({@code subs[1]}). */
    private final String input2;

    /** Name of the output topic where the result is published. */
    private final String output;

    /** The operation applied to the two input values. */
    private final BinaryOperator<Double> op;

    /** Most recently received value from {@code input1}; {@code null} if not yet received. */
    private Double val1;

    /** Most recently received value from {@code input2}; {@code null} if not yet received. */
    private Double val2;

    /**
     * Creates a {@code BinOpAgent}, subscribes it to both input topics, and
     * registers it as a publisher of the output topic.
     *
     * @param name   display name for this agent instance
     * @param input1 name of the first input topic
     * @param input2 name of the second input topic
     * @param output name of the output topic
     * @param op     binary operation to apply; must not be {@code null}
     */
    public BinOpAgent(String name, String input1, String input2,
                      String output, BinaryOperator<Double> op) {
        this.name   = name;
        this.input1 = input1;
        this.input2 = input2;
        this.output = output;
        this.op     = op;

        TopicManagerSingleton.get().getTopic(input1).subscribe(this);
        TopicManagerSingleton.get().getTopic(input2).subscribe(this);
        TopicManagerSingleton.get().getTopic(output).addPublisher(this);

        callback(name, new Message(name));
    }

    /** {@inheritDoc} */
    @Override
    public String getName() { return name; }

    /**
     * Resets both stored input values to {@code null} so the next computation
     * waits for fresh values from both input topics.
     */
    @Override
    public void reset() {
        this.val1 = null;
        this.val2 = null;
    }

    /**
     * Stores the incoming value for the matching input topic.
     * Once both inputs are available the operation is applied, the result is
     * published, and the inputs are reset.
     * Non-numeric messages (NaN) are silently ignored.
     *
     * @param topic the topic that sent the message
     * @param msg   the received message
     */
    @Override
    public void callback(String topic, Message msg) {
        if (!Double.isNaN(msg.asDouble)) {
            if (topic.equals(input1))      val1 = msg.asDouble;
            else if (topic.equals(input2)) val2 = msg.asDouble;
        }

        if (val1 != null && val2 != null) {
            double result = op.apply(val1, val2);
            TopicManagerSingleton.get().getTopic(output).publish(new Message(result));
            reset();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void close() {}
}
