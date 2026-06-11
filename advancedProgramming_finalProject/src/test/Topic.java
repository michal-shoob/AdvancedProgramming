package test;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a named topic through which messages are published and received.
 * Constructor is package-private: only TopicManager (same package) can create Topics.
 */
public class Topic {

    public final String name;

    private final List<Agent> subs = new ArrayList<Agent>();
    private final List<Agent> pubs = new ArrayList<Agent>();

    /** Last message published to this topic. */
    private Message lastMessage = null;

    /** Package-private constructor - only TopicManager can create Topics. */
    Topic(String name) {
        this.name = name;
    }

    /** Returns the name of this topic. */
    public String getName() {
        return name;
    }

    public void subscribe(Agent a) {
        subs.add(a);
    }

    public void unsubscribe(Agent a) {
        subs.remove(a);
    }

    public void publish(Message msg) {
        this.lastMessage = msg;
        for (Agent a : new ArrayList<Agent>(subs)) {
            a.callback(name, msg);
        }
    }

    public void addPublisher(Agent a) {
        pubs.add(a);
    }

    public void removePublisher(Agent a) {
        pubs.remove(a);
    }

    /** Returns the last message published to this topic, or null if none yet. */
    public Message getMessage() {
        return lastMessage;
    }

    public List<Agent> getSubs() { return subs; }
    public List<Agent> getPubs() { return pubs; }
}