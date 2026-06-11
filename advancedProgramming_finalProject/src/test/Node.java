package test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node {

    private String name;
    private List<Node> edges;
    private Message message;

    public Node(String name) {
        setName(name);
        this.edges = new ArrayList<>();
        this.message = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Node cant be null");
        }

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Node name cannot be empty");
        }

        this.name = name;
    }

    public List<Node> getEdges() {
        return edges;
    }

    
    public List<Node> setEdges(List<Node> edges) {
        if (edges == null) {
            throw new IllegalArgumentException("Edges can not be null");
        }

        List<Node> copy = new ArrayList<>();

        for (Node node : edges) {
            if (node == null) {
                throw new IllegalArgumentException("Edges list cannot contain null nodes");
            }
            if (!copy.contains(node)) { // מניעת כפילויות
                copy.add(node);
            }
        }

        this.edges = copy;
        return this.edges;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void addEdge(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Edge node cannot be null");
        }

        if (!edges.contains(node)) {
            edges.add(node);
        }
    }

    public boolean hasCycles() {
        Set<Node> visited = new HashSet<>();
        Set<Node> recursionStack = new HashSet<>();

        return hasCyclesHelper(this, visited, recursionStack);
    }

    private boolean hasCyclesHelper(Node current,
                                    Set<Node> visited,
                                    Set<Node> recursionStack) {

        if (current == null) {
            return false;
        }

        if (recursionStack.contains(current)) {
            return true;
        }

        if (visited.contains(current)) {
            return false;
        }

        visited.add(current);
        recursionStack.add(current);

        for (Node neighbor : current.edges) {
            if (neighbor == null) {
                throw new IllegalStateException("Graph contains null edge");
            }

            if (hasCyclesHelper(neighbor, visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(current);
        return false;
    }
}