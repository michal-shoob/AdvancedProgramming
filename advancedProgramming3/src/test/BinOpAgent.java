package test;

import java.util.function.BinaryOperator;
import test.TopicManagerSingleton.TopicManager;

public class BinOpAgent implements Agent {

    private final String name;
    private final String input1;
    private final String input2;
    private final String output;
    private final BinaryOperator<Double> op;

    private double val1;
    private double val2;

    private boolean hasVal1;
    private boolean hasVal2;

    public BinOpAgent(String name, String input1, String input2, String output, BinaryOperator<Double> op) {
        if (name == null || input1 == null || input2 == null || output == null || op == null) {
            throw new IllegalArgumentException("BinOpAgent arguments cannot be null");
        }

        this.name = name;
        this.input1 = input1;
        this.input2 = input2;
        this.output = output;
        this.op = op;

        reset();

        TopicManager tm = TopicManagerSingleton.get();

        tm.getTopic(input1).subscribe(this);
        tm.getTopic(input2).subscribe(this);
        tm.getTopic(output).addPublisher(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void reset() {
        val1 = 0.0;
        val2 = 0.0;
        hasVal1 = false;
        hasVal2 = false;
    }

    @Override
    public void callback(String topic, Message msg) {
        if (topic == null || msg == null) {
            return;
        }

        if (Double.isNaN(msg.asDouble)) {
            return;
        }

        if (topic.equals(input1)) {
            val1 = msg.asDouble;
            hasVal1 = true;
        } else if (topic.equals(input2)) {
            val2 = msg.asDouble;
            hasVal2 = true;
        } else {
            return;
        }

        if (hasVal1 && hasVal2) {
            double result = op.apply(val1, val2);
            TopicManagerSingleton.get()
                    .getTopic(output)
                    .publish(new Message(result));
            reset();
        }
    }

    @Override
    public void close() {
        TopicManager tm = TopicManagerSingleton.get();

        tm.getTopic(input1).unsubscribe(this);
        tm.getTopic(input2).unsubscribe(this);
        tm.getTopic(output).removePublisher(this);
    }
}