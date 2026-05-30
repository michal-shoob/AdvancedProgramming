
package test;

import java.util.*;

public class MainTrain {

    static int failures = 0;

    public static void main(String[] args) {

        // Message
        testMessageBasic();
        testMessageEdgeCases();

        // Topic
        testTopicBasic();
        testTopicEdgeCases();

        // TopicManager
        testTopicManager();

        // Node
        testNodeBasic();
        testNodeEdgeCases();

        // Graph
        testGraphBasic();
        testGraphEdgeCases();

        // Config / Integration
        testMathExample();

        System.out.println("\n====================");
        if (failures == 0) {
            System.out.println("✅ ALL TESTS PASSED");
        } else {
            System.out.println("❌ FAILURES: " + failures);
        }
    }

    static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            failures++;
            System.out.println("FAIL: " + msg);
        }
    }

    // ================= MESSAGE =================

    static void testMessageBasic() {
        Message m = new Message("123.45");
        assertTrue(m.asDouble == 123.45, "Message double parse failed");
        assertTrue(m.asText.equals("123.45"), "Message text mismatch");
    }

    static void testMessageEdgeCases() {
        Message m1 = new Message("hello");
        assertTrue(Double.isNaN(m1.asDouble), "Message NaN expected");

        Message m2 = new Message("   42.0   ");
        assertTrue(m2.asDouble == 42.0, "Message trim failed");

        try {
            new Message((String) null);
            assertTrue(false, "Message null should throw");
        } catch (Exception e) {}
    }

    // ================= TOPIC =================

    static class DummyAgent implements Agent {
        int count = 0;
        List<Message> received = new ArrayList<>();
        String name;

        DummyAgent(String name) {
            this.name = name;
        }

        public String getName() { return name; }
        public void reset() { count = 0; received.clear(); }

        public void callback(String topic, Message msg) {
            count++;
            received.add(msg);
        }

        public void close() {}
    }

    static void testTopicBasic() {
        TopicManagerSingleton.get().clear();

        Topic t = TopicManagerSingleton.get().getTopic("T");
        DummyAgent a = new DummyAgent("A");

        t.subscribe(a);
        t.publish(new Message("1"));

        assertTrue(a.count == 1, "Topic publish failed");

        t.unsubscribe(a);
        t.publish(new Message("2"));

        assertTrue(a.count == 1, "Topic unsubscribe failed");
    }

    static void testTopicEdgeCases() {
        TopicManagerSingleton.get().clear();

        Topic t = TopicManagerSingleton.get().getTopic("T");
        DummyAgent a = new DummyAgent("A");

        t.subscribe(a);
        t.subscribe(a);

        t.publish(new Message("X"));

        assertTrue(a.count == 2, "Duplicate subscription not handled");
    }

    // ================= TOPIC MANAGER =================

    static void testTopicManager() {
        TopicManagerSingleton.get().clear();

        Topic t1 = TopicManagerSingleton.get().getTopic("A");
        Topic t2 = TopicManagerSingleton.get().getTopic("A");

        assertTrue(t1 == t2, "Singleton topic failed");

        TopicManagerSingleton.get().clear();
        assertTrue(TopicManagerSingleton.get().getTopics().isEmpty(), "Clear failed");
    }

    // ================= NODE =================

    static void testNodeBasic() {
        Node a = new Node("A");
        Node b = new Node("B");

        a.addEdge(b);
        assertTrue(a.getEdges().contains(b), "Edge not added");
    }

    static void testNodeEdgeCases() {
        Node a = new Node("A");
        a.addEdge(a);

        assertTrue(a.hasCycles(), "Self cycle not detected");

        Node x = new Node("X");
        Node y = new Node("Y");
        Node z = new Node("Z");

        x.addEdge(y);
        y.addEdge(z);
        z.addEdge(x);

        assertTrue(x.hasCycles(), "Indirect cycle not detected");

        Node root = new Node("Root");
        Node safe = new Node("Safe");
        Node c1 = new Node("C1");
        Node c2 = new Node("C2");

        root.addEdge(safe);
        root.addEdge(c1);
        c1.addEdge(c2);
        c2.addEdge(c1);

        assertTrue(root.hasCycles(), "Branch cycle not detected");
    }

    // ================= GRAPH =================

    static Node find(Graph g, String name) {
        for (Node n : g) {
            if (n.getName().equals(name)) return n;
        }
        return null;
    }

    static boolean hasEdge(Graph g, String from, String to) {
        Node f = find(g, from);
        Node t = find(g, to);
        return f != null && t != null && f.getEdges().contains(t);
    }

    static void testGraphBasic() {
        Graph g = new Graph();

        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");

        a.addEdge(b);
        b.addEdge(c);

        g.add(a); g.add(b); g.add(c);

        assertTrue(!g.hasCycles(), "Graph false cycle");

        c.addEdge(a);
        assertTrue(g.hasCycles(), "Graph cycle not detected");
    }

    static void testGraphEdgeCases() {

        // Empty manager
        TopicManagerSingleton.get().clear();
        Graph g = new Graph();
        g.createFromTopics();
        assertTrue(g.size() == 0, "Empty graph not handled");

        // createFromTopics twice
        Config config = new MathExampleConfig();
        config.create();

        g.createFromTopics();
        int size1 = g.size();

        g.createFromTopics();
        int size2 = g.size();

        assertTrue(size1 == size2, "Duplicate nodes created");

        // Agent multiple topics
        TopicManagerSingleton.get().clear();

        Topic t1 = TopicManagerSingleton.get().getTopic("T1");
        Topic t2 = TopicManagerSingleton.get().getTopic("T2");

        DummyAgent a = new DummyAgent("A");

        t1.subscribe(a);
        t2.subscribe(a);

        g = new Graph();
        g.createFromTopics();

        assertTrue(hasEdge(g, "TT1", "AA"), "Missing edge TT1->AA");
        assertTrue(hasEdge(g, "TT2", "AA"), "Missing edge TT2->AA");
    }

    // ================= CONFIG =================

    static void testMathExample() {
        TopicManagerSingleton.get().clear();

        Config config = new MathExampleConfig();
        config.create();

        final Message[] result = new Message[1];

        Agent a = new Agent() {
            public String getName() { return "collector"; }
            public void reset() {}
            public void close() {}

            public void callback(String topic, Message msg) {
                result[0] = msg;
            }
        };

        TopicManagerSingleton.get().getTopic("R3").subscribe(a);

        TopicManagerSingleton.get().getTopic("A").publish(new Message(10));
        TopicManagerSingleton.get().getTopic("B").publish(new Message(5));

        assertTrue(result[0] != null, "No result from config");
        assertTrue(result[0].asDouble == 75.0, "Wrong computation result");
    }
}

