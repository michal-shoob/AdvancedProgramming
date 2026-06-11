package test;

import java.util.function.BinaryOperator;

import test.TopicManagerSingleton.TopicManager;

public class BinOpAgent implements Agent{
    //private final Agent agent;
    private final String name;
    private final String input1;
    private final String input2;
    private final String output;
    private final BinaryOperator<Double> op;

    private Double val1;
    private Double val2;


    public BinOpAgent (String name, String input1, String input2, String output, BinaryOperator<Double> op) {
        this.name = name;
        this.input1 = input1;
        this.input2 = input2;
        this.output = output;
        this.op = op;

        TopicManagerSingleton.get().getTopic(input1).subscribe(this);
        TopicManagerSingleton.get().getTopic(input2).subscribe(this);
        TopicManagerSingleton.get().getTopic(output).addPublisher(this);

        callback(name, new Message(name));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void reset() {
        this.val1 = 0.0;
        this.val2 = 0.0;
        //agent.reset();
    }

    @Override
    public void callback(String topic, Message msg) {
        if(!Double.isNaN(msg.asDouble)) {
            if(topic.equals(input1)) {
                val1 = msg.asDouble;
            } else if(topic.equals(input2)) {
                val2 = msg.asDouble;
            }
        }

        // Once both inputs have been received, compute, publish, and reset.
        if (val1 != null && val2 != null) {
            double result = op.apply(val1, val2);
            TopicManagerSingleton.get().getTopic(output).publish(new Message(result));
            reset();
        }
    }

    @Override
    public void close() {
    }
}
