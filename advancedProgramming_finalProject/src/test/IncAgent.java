package test;

public class IncAgent implements Agent {
    private double x = 0.0;
    private final String name = "IncAgent";
    private final String[] subs;
    private final String[] pubs;

    public IncAgent(String[] subs, String[] pubs) {
        if (subs == null || subs.length < 1) {
            throw new IllegalArgumentException("At least one subscription topic is required.");
        }
        if (pubs == null || pubs.length < 1) {
            throw new IllegalArgumentException("At least one publisher topic is required.");
        }

        this.subs = subs;
        this.pubs = pubs;

        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();

        tm.getTopic(subs[0]).subscribe(this);
        tm.getTopic(pubs[0]).addPublisher(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void reset() {
        x = 0.0;
    }

    @Override
    public void callback(String topic, Message msg) {
        if (topic == null || msg == null) {
            return;
        }

        double value = msg.asDouble;

        if (Double.isNaN(value)) {
            return;
        }

        if (topic.equals(subs[0])) {
            x = value;
            publishIncrement();
        }
    }

    private void publishIncrement() {
        double result = x + 1;
        TopicManagerSingleton.get()
                .getTopic(pubs[0])
                .publish(new Message(result));
    }

    @Override
    public void close() {
        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();

        tm.getTopic(subs[0]).unsubscribe(this);
        tm.getTopic(pubs[0]).removePublisher(this);

        reset();
    }
}