package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Topic in the system.
 * A Topic acts as a communication channel between Agents.
 * Agents can subscribe to receive messages and publish messages to it.
 */
public class Topic {

    // Name of the topic (immutable)
    public final String name;

    // List of agents subscribed to this topic (receive messages)
    private final List<Agent> subs = new ArrayList<>();

    // List of agents that publish messages to this topic
    private final List<Agent> pubs = new ArrayList<>();

    /**
     * Constructor (package-private)
     * Validates that the topic name is not null or empty.
     */
    Topic(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Topic name cannot be null or empty");
        }

        this.name = name;
    }

    /**
     * Returns the topic name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a read-only view of subscribers.
     * Used by Graph to build edges: Topic → Agents
     */
    public Iterable<Agent> getSubs() {
        return Collections.unmodifiableList(subs);
    }

    /**
     * Returns a read-only view of publishers.
     * Used by Graph to build edges: Agents → Topic
     */
    public Iterable<Agent> getPubs() {
        return Collections.unmodifiableList(pubs);
    }

    /**
     * Subscribes an agent to this topic.
     * Ignores null and avoids duplicates.
     */
    public void subscribe(Agent a) {
        if (a != null && !subs.contains(a)) {
            subs.add(a);
        }
    }

    /**
     * Unsubscribes an agent from this topic.
     * Ignores null.
     */
    public void unsubscribe(Agent a) {
        if (a != null) {
            subs.remove(a);
        }
    }

    /**
     * Publishes a message to all subscribed agents.
     * Each subscriber receives the message via callback.
     * Ignores null messages.
     */
    public void publish(Message m) {
        if (m == null) {
            return;
        }

        for (Agent agent : subs) {
            agent.callback(name, m);
        }
    }

    /**
     * Registers an agent as a publisher of this topic.
     * Ignores null and avoids duplicates.
     */
    public void addPublisher(Agent a) {
        if (a != null && !pubs.contains(a)) {
            pubs.add(a);
        }
    }

    /**
     * Removes an agent from the publishers list.
     * Ignores null.
     */
    public void removePublisher(Agent a) {
        if (a != null) {
            pubs.remove(a);
        }
    }
}