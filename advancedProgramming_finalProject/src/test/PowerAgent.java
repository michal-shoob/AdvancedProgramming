package test;

public class PowerAgent implements Agent {

    private double base = 0.0;
    private double exponent = 0.0;

    private final String name = "PowerAgent";

    private final String[] subs;
    private final String[] pubs;

    public PowerAgent(String[] subs, String[] pubs) {

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
        base = 0.0;
        exponent = 0.0;
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
            base = value;

        } else if (topic.equals(subs[1])) {
            exponent = value;

        } else {
            return;
        }

        publishPower();
    }

    private void publishPower() {

        double result = Math.pow(base, exponent);

        TopicManagerSingleton.get()
                .getTopic(pubs[0])
                .publish(new Message(result));
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
