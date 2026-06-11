package test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import test.TopicManagerSingleton.TopicManager;

/**
 * Directed graph of the current computational pipeline, stored as a list of {@link Node}s.
 *
 * <p>Two kinds of node exist, distinguished by a name prefix:</p>
 * <ul>
 *   <li><b>Topic nodes</b>  (prefix {@code 'T'}) — one per named {@link Topic}</li>
 *   <li><b>Agent nodes</b>  (prefix {@code 'A'}) — one per unique agent name</li>
 * </ul>
 *
 * <p>Edges represent data flow:</p>
 * <ul>
 *   <li>Topic → Agent : the topic has the agent as a subscriber</li>
 *   <li>Agent → Topic : the agent publishes to the topic</li>
 * </ul>
 *
 * <p>The graph is rebuilt from scratch on every call to {@link #createFromTopics()},
 * reflecting the live state of {@link TopicManagerSingleton}.</p>
 */
public class Graph extends ArrayList<Node> {

    /**
     * Returns {@code true} if any node in the graph is part of a directed cycle.
     *
     * @return {@code true} if a cycle exists
     */
    public boolean hasCycles() {
        for (Node node : this) {
            if (node.hasCycles()) return true;
        }
        return false;
    }

    private Node getOrCreate(Map<String, Node> map, String name) {
        Node n = map.get(name);
        if (n == null) { n = new Node(name); map.put(name, n); }
        return n;
    }

    /**
     * Rebuilds the graph from the current state of {@link TopicManagerSingleton}.
     *
     * <p>Clears any previously stored nodes, then iterates over all registered
     * topics and creates topic nodes, agent nodes, and the edges that represent
     * their live subscription and publication relationships.</p>
     */
    public void createFromTopics() {
        clear();
        TopicManager tm = TopicManagerSingleton.get();
        Collection<Topic> topics = tm.getTopics();
        Map<String, Node> nodesByName = new HashMap<>();

        for (Topic topic : topics) {
            String tName = "T" + topic.getName();
            Node topicNode = getOrCreate(nodesByName, tName);

            for (Agent sub : topic.getSubs()) {
                Node agentNode = getOrCreate(nodesByName, "A" + sub.getName());
                topicNode.addEdge(agentNode);
            }
            for (Agent pub : topic.getPubs()) {
                Node agentNode = getOrCreate(nodesByName, "A" + pub.getName());
                agentNode.addEdge(topicNode);
            }
        }
        addAll(nodesByName.values());
    }
}
