package test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A vertex in the computational {@link Graph}.
 *
 * <p>Each node has a name, a list of directed edges to other nodes, and an
 * optional {@link Message} representing the most recent value (meaningful only
 * for topic nodes whose name starts with {@code 'T'}).</p>
 *
 * <p>Naming convention used by {@link Graph}:</p>
 * <ul>
 *   <li>Topic nodes  — name prefixed with {@code 'T'} (e.g. {@code "TMyTopic"})</li>
 *   <li>Agent nodes  — name prefixed with {@code 'A'} (e.g. {@code "APlusAgent"})</li>
 * </ul>
 */
public class Node {

    /** Unique identifier of this node (includes the T/A prefix). */
    private String name;

    /** Directed edges from this node to its neighbours. */
    private List<Node> edges;

    /** Latest message value; only relevant for topic nodes. */
    private Message message;

    /**
     * Creates a node with the given name and no edges.
     *
     * @param name a non-null, non-empty identifier
     * @throws IllegalArgumentException if {@code name} is {@code null} or blank
     */
    public Node(String name) {
        setName(name);
        this.edges = new ArrayList<>();
        this.message = null;
    }

    /**
     * Returns the name of this node.
     *
     * @return the node's identifier
     */
    public String getName() { return name; }

    /**
     * Sets the name of this node.
     *
     * @param name a non-null, non-empty identifier
     * @throws IllegalArgumentException if {@code name} is {@code null} or blank
     */
    public void setName(String name) {
        if (name == null) throw new IllegalArgumentException("Node cant be null");
        if (name.trim().isEmpty()) throw new IllegalArgumentException("Node name cannot be empty");
        this.name = name;
    }

    /**
     * Returns the list of nodes this node has directed edges to.
     *
     * @return a live, mutable list of neighbour nodes
     */
    public List<Node> getEdges() { return edges; }

    /**
     * Replaces the edge list with the given nodes, removing duplicates.
     *
     * @param edges the new set of neighbours; must not be {@code null} or contain {@code null}
     * @return the deduplicated edge list that was stored
     * @throws IllegalArgumentException if {@code edges} is {@code null} or contains {@code null}
     */
    public List<Node> setEdges(List<Node> edges) {
        if (edges == null) throw new IllegalArgumentException("Edges can not be null");
        List<Node> copy = new ArrayList<>();
        for (Node node : edges) {
            if (node == null) throw new IllegalArgumentException("Edges list cannot contain null nodes");
            if (!copy.contains(node)) copy.add(node);
        }
        this.edges = copy;
        return this.edges;
    }

    /**
     * Returns the last message associated with this node, or {@code null} if none.
     *
     * @return the stored message
     */
    public Message getMessage() { return message; }

    /**
     * Stores a message value on this node.
     *
     * @param message the message to store; may be {@code null}
     */
    public void setMessage(Message message) { this.message = message; }

    /**
     * Adds a directed edge to {@code node} if not already present.
     *
     * @param node the target node; must not be {@code null}
     * @throws IllegalArgumentException if {@code node} is {@code null}
     */
    public void addEdge(Node node) {
        if (node == null) throw new IllegalArgumentException("Edge node cannot be null");
        if (!edges.contains(node)) edges.add(node);
    }

    /**
     * Returns {@code true} if this node is part of a directed cycle.
     *
     * @return {@code true} if a cycle is reachable from this node
     */
    public boolean hasCycles() {
        return hasCyclesHelper(this, new HashSet<>(), new HashSet<>());
    }

    private boolean hasCyclesHelper(Node current, Set<Node> visited, Set<Node> recursionStack) {
        if (current == null) return false;
        if (recursionStack.contains(current)) return true;
        if (visited.contains(current)) return false;
        visited.add(current);
        recursionStack.add(current);
        for (Node neighbor : current.edges) {
            if (neighbor == null) throw new IllegalStateException("Graph contains null edge");
            if (hasCyclesHelper(neighbor, visited, recursionStack)) return true;
        }
        recursionStack.remove(current);
        return false;
    }
}
