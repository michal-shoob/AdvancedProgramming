package test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import test.TopicManagerSingleton.TopicManager;

/**
 * A directed graph represented as a list of Nodes.
 * Nodes represent either Topics (name starts with 'T') or Agents (name starts with 'A').
 *
 * Edges:
 *   Topic  → all Agents subscribed to it  (subs)
 *   Agent  → all Topics it publishes to   (pubs)
 */
public class Graph extends ArrayList<Node> {

    /**
     * Returns true if ANY node in the graph is part of a cycle.
     */
    public boolean hasCycles() {
        for (Node node : this) {
            if (node.hasCycles()) return true;
        }
        return false;
    }

    private Node getOrCreate(Map<String, Node> map, String name) {
        Node n = map.get(name);

        if (n == null) {
            n = new Node(name);
            map.put(name, n);
        }

        return n;
    }
    
    /**
     * Builds the graph from the current state of the TopicManager.
     * Clears any previous content first.
     */
    public void createFromTopics() {
        clear();

        TopicManager tm = TopicManagerSingleton.get();
        Collection<Topic> topics = tm.getTopics();

        // map from name → Node so we don't create duplicates
        Map<String, Node> nodesByName = new HashMap<>();

        // helper lambdas to get-or-create nodes
        // Topic nodes: prefix 'T'
        // Agent nodes: prefix 'A'

        for (Topic topic : topics) {
            String tName = "T" + topic.getName();
            Node topicNode = getOrCreate(nodesByName, tName);

            // edges: Topic → each subscribed Agent
            for (Agent sub : topic.getSubs()) {
                String aName  = "A" + sub.getName();
                Node agentNode = getOrCreate(nodesByName, aName );
                topicNode.addEdge(agentNode);
            }

            // edges: each publishing Agent → Topic
            for (Agent pub : topic.getPubs()) {
                String aName = "A" + pub.getName();
                Node agentNode = getOrCreate(nodesByName, aName );
                agentNode.addEdge(topicNode);
            }
        }

        // add all nodes to the graph (no duplicates thanks to the map)
        addAll(nodesByName.values());
    }
    

}