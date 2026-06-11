package test;

import java.util.ArrayList;
import java.util.List;

/**
 * A named publish-subscribe channel in the computational graph.
 *
 * <p>Topics connect producers (agents that call {@link #publish(Message)}) to
 * consumers (agents registered via {@link #subscribe(Agent)}).  Each call to
 * {@link #publish} delivers the message to every current subscriber and stores
 * it as the latest value, which can be retrieved with {@link #getMessage()}.</p>
 *
 * <p>Topics are created and managed exclusively by
 * {@link TopicManagerSingleton.TopicManager}; the constructor is package-private
 * to enforce this.</p>
 */
public class Topic {

    /** The immutable name that identifies this topic in the graph. */
    public final String name;

    /** Agents subscribed to receive messages published on this topic. */
    private final List<Agent> subs = new ArrayList<Agent>();

    /** Agents registered as publishers of this topic (used for graph rendering). */
    private final List<Agent> pubs = new ArrayList<Agent>();

    /** Most recent message published to this topic; {@code null} if none yet. */
    private Message lastMessage = null;

    /**
     * Package-private constructor — only {@link TopicManagerSingleton.TopicManager}
     * may create topics.
     *
     * @param name the topic name; must not be {@code null} or empty
     */
    Topic(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this topic.
     *
     * @return the topic name
     */
    public String getName() {
        return name;
    }

    /**
     * Registers an agent to receive future messages on this topic.
     *
     * @param a the agent to subscribe
     */
    public void subscribe(Agent a) {
        subs.add(a);
    }

    /**
     * Removes a previously registered subscriber.
     * Does nothing if the agent is not currently subscribed.
     *
     * @param a the agent to unsubscribe
     */
    public void unsubscribe(Agent a) {
        subs.remove(a);
    }

    /**
     * Publishes a message to all current subscribers and caches it as the
     * latest value.  Subscribers are notified in registration order.
     *
     * @param msg the message to deliver; should not be {@code null}
     */
    public void publish(Message msg) {
        this.lastMessage = msg;
        for (Agent a : new ArrayList<Agent>(subs)) {
            a.callback(name, msg);
        }
    }

    /**
     * Records an agent as a publisher of this topic (used when building the
     * graph visualisation — no functional effect on message delivery).
     *
     * @param a the agent to register as a publisher
     */
    public void addPublisher(Agent a) {
        pubs.add(a);
    }

    /**
     * Removes an agent from the publisher registry.
     *
     * @param a the agent to deregister
     */
    public void removePublisher(Agent a) {
        pubs.remove(a);
    }

    /**
     * Returns the most recent message published to this topic.
     *
     * @return the last {@link Message}, or {@code null} if nothing has been
     *         published yet
     */
    public Message getMessage() {
        return lastMessage;
    }

    /**
     * Returns all currently subscribed agents.
     *
     * @return a live list of subscribers (do not modify externally)
     */
    public List<Agent> getSubs() { return subs; }

    /**
     * Returns all agents registered as publishers.
     *
     * @return a live list of publishers (do not modify externally)
     */
    public List<Agent> getPubs() { return pubs; }
}
