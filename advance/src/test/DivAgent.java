package test;

public class DivAgent implements Agent {

    private double x = 0.0;
    private double y = 0.0;

    private final String name = "DivAgent";

    private final String[] subs;
    private final String[] pubs;

    public DivAgent(String[] subs, String[] pubs) {

        if (subs == null || subs.length < 2) {
            throw new IllegalArgumentException(
                    "At least two subscription topics are required.");
        }

        if (pubs == null || pubs.length < 1) {
            throw new IllegalArgumentException(
                    "At least one publisher topic is required.");
        }

        this.subs = subs;
        this.pubs = pubs;

        TopicManagerSingleton.TopicManager tm =
                TopicManagerSingleton.get();

        tm.getTopic(subs[0]).subscribe(this);
        tm.getTopic(subs[1]).subscribe(this);

        tm.getTopic(pubs[0]).addPublisher(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void reset() {
        x = 0.0;
        y = 0.0;
    }

    @Override
    public void callback(String topic, Message msg) {

        if (topic == null || msg == null) {
            return;
        }

        double value;

        try {
            value = Double.parseDouble(msg.asText);

            if (Double.isNaN(value)) {
                return;
            }

        } catch (Exception e) {
            return;
        }

        if (topic.equals(subs[0])) {
            x = value;

        } else if (topic.equals(subs[1])) {
            y = value;

        } else {
            return;
        }

        if (y == 0.0) {
            return;
        }

        publishQuotient();
    }

    private void publishQuotient() {

        double quotient = x / y;

        TopicManagerSingleton.get()
                .getTopic(pubs[0])
                .publish(new Message(quotient));
    }

    @Override
    public void close() {

        TopicManagerSingleton.TopicManager tm =
                TopicManagerSingleton.get();

        tm.getTopic(subs[0]).unsubscribe(this);
        tm.getTopic(subs[1]).unsubscribe(this);

        tm.getTopic(pubs[0]).removePublisher(this);

        reset();
    }
}
