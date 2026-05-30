package test;

import java.util.Arrays;
import java.util.Random;

public class MainTrain {

    static int failures = 0;

    public static void main(String[] args) {
        System.out.println("Running Assignment 1 Full Tests...\n");

        testMessageDefinitions();
        testTopicDefinitions();
        testTopicManagerDefinitions();

        testMessageConversions();
        testTopicPublishSubscribe();
        testTopicManagerSingleton();

        testSimpleComputationalGraph();
        testRandomComputationalGraph();

        if (failures == 0) {
            System.out.println("\nAll Assignment 1 full tests passed ✅");
        } else {
            System.out.println("\nTotal failures: " + failures);
        }
    }

    static void check(boolean condition, String message) {
        if (!condition) {
            failures++;
            System.out.println("FAIL: " + message);
        }
    }

    // =========================
    // Definition / structure tests
    // =========================

    static void testMessageDefinitions() {
        Message m1 = new Message("12.5");
        Message m2 = new Message(3.14);
        Message m3 = new Message("abc".getBytes());

        check(m1.data != null, "Message: data should exist");
        check(m1.asText != null, "Message: asText should exist");
        check(!Double.isNaN(m1.asDouble), "Message: asDouble should parse numeric string");
        check(m1.date != null, "Message: date should exist");

        check(m2.asDouble == 3.14, "Message(double): incorrect asDouble");
        check("abc".equals(m3.asText), "Message(byte[]): incorrect asText");

        System.out.println("testMessageDefinitions passed");
    }

    static void testTopicDefinitions() {
        TopicManagerSingleton.get().clear();

        Topic t = TopicManagerSingleton.get().getTopic("Test");
        DummyAgent a = new DummyAgent("A");

        t.subscribe(a);
        t.unsubscribe(a);
        t.addPublisher(a);
        t.removePublisher(a);
        t.publish(new Message("1"));

        check("Test".equals(t.name), "Topic: public final name incorrect");

        System.out.println("testTopicDefinitions passed");
    }

    static void testTopicManagerDefinitions() {
        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();

        tm.clear();
        Topic t1 = tm.getTopic("A");
        Topic t2 = tm.getTopic("A");

        check(t1 == t2, "TopicManager: same topic name should return same object");
        check(tm.getTopics().size() == 1, "TopicManager: getTopics size incorrect");

        tm.clear();
        check(tm.getTopics().isEmpty(), "TopicManager: clear should remove all topics");

        System.out.println("testTopicManagerDefinitions passed");
    }

    // =========================
    // Message tests
    // =========================

    static void testMessageConversions() {
        Message m1 = new Message("123.45");
        check("123.45".equals(m1.asText), "Message String: asText incorrect");
        check(m1.asDouble == 123.45, "Message String: asDouble incorrect");
        check(Arrays.equals(m1.data, "123.45".getBytes()), "Message String: data incorrect");

        Message m2 = new Message("hello");
        check("hello".equals(m2.asText), "Message non numeric: asText incorrect");
        check(Double.isNaN(m2.asDouble), "Message non numeric: asDouble should be NaN");

        Message m3 = new Message("   42.0   ");
        check(m3.asDouble == 42.0, "Message spaces: should parse after trim");

        Message m4 = new Message(7.5);
        check(m4.asDouble == 7.5, "Message double: asDouble incorrect");
        check(Double.toString(7.5).equals(m4.asText), "Message double: asText incorrect");

        Message m5 = new Message("abc".getBytes());
        check("abc".equals(m5.asText), "Message byte[]: asText incorrect");

        System.out.println("testMessageConversions passed");
    }

    // =========================
    // Topic tests
    // =========================

    static void testTopicPublishSubscribe() {
        TopicManagerSingleton.get().clear();

        Topic t = TopicManagerSingleton.get().getTopic("Numbers");

        DummyAgent a1 = new DummyAgent("A1");
        DummyAgent a2 = new DummyAgent("A2");

        t.subscribe(a1);
        t.subscribe(a2);

        Message msg = new Message(10);
        t.publish(msg);

        check(a1.callCount == 1, "Topic: first subscriber did not receive message");
        check(a2.callCount == 1, "Topic: second subscriber did not receive message");

        check("Numbers".equals(a1.lastTopic), "Topic: callback topic name incorrect");
        check(a1.lastMessage == msg, "Topic: callback message reference incorrect");

        t.unsubscribe(a1);
        t.publish(new Message(20));

        check(a1.callCount == 1, "Topic: unsubscribed agent should not receive message");
        check(a2.callCount == 2, "Topic: subscribed agent should receive second message");

        System.out.println("testTopicPublishSubscribe passed");
    }

    // =========================
    // TopicManagerSingleton tests
    // =========================

    static void testTopicManagerSingleton() {
        TopicManagerSingleton.TopicManager tm1 = TopicManagerSingleton.get();
        TopicManagerSingleton.TopicManager tm2 = TopicManagerSingleton.get();

        check(tm1 == tm2, "TopicManagerSingleton: get should return same instance");

        tm1.clear();

        Topic a1 = tm1.getTopic("A");
        Topic a2 = tm1.getTopic("A");
        Topic b = tm1.getTopic("B");

        check(a1 == a2, "TopicManager: same name should return same topic");
        check(a1 != b, "TopicManager: different names should return different topics");
        check(tm1.getTopics().size() == 2, "TopicManager: should contain 2 topics");

        tm1.clear();
        check(tm1.getTopics().size() == 0, "TopicManager: clear failed");

        System.out.println("testTopicManagerSingleton passed");
    }

    // =========================
    // Computational graph tests
    // =========================

    static void testSimpleComputationalGraph() {
        TopicManagerSingleton.get().clear();

        Topic A = TopicManagerSingleton.get().getTopic("A");
        Topic B = TopicManagerSingleton.get().getTopic("B");
        Topic R = TopicManagerSingleton.get().getTopic("R");

        SumAgent sum = new SumAgent("sum", "A", "B", "R");

        A.subscribe(sum);
        B.subscribe(sum);
        R.addPublisher(sum);

        ResultAgent result = new ResultAgent("result");
        R.subscribe(result);

        A.publish(new Message(5));
        B.publish(new Message(3));

        check(result.received, "Computational graph: result was not received");
        check(result.value == 8.0, "Computational graph: expected 8.0, got " + result.value);

        System.out.println("testSimpleComputationalGraph passed");
    }

    static void testRandomComputationalGraph() {
        TopicManagerSingleton.get().clear();

        Topic A = TopicManagerSingleton.get().getTopic("A");
        Topic B = TopicManagerSingleton.get().getTopic("B");
        Topic R1 = TopicManagerSingleton.get().getTopic("R1");
        Topic R2 = TopicManagerSingleton.get().getTopic("R2");
        Topic R3 = TopicManagerSingleton.get().getTopic("R3");

        SumAgent plus = new SumAgent("plus", "A", "B", "R1");
        SubAgent minus = new SubAgent("minus", "A", "B", "R2");
        MulAgent mul = new MulAgent("mul", "R1", "R2", "R3");

        A.subscribe(plus);
        B.subscribe(plus);
        R1.addPublisher(plus);

        A.subscribe(minus);
        B.subscribe(minus);
        R2.addPublisher(minus);

        R1.subscribe(mul);
        R2.subscribe(mul);
        R3.addPublisher(mul);

        ResultAgent result = new ResultAgent("result");
        R3.subscribe(result);

        Random rand = new Random(1);

        for (int i = 0; i < 100; i++) {
            result.reset();

            double a = rand.nextInt(100) - 50;
            double b = rand.nextInt(100) - 50;

            A.publish(new Message(a));
            B.publish(new Message(b));

            double expected = (a + b) * (a - b);

            check(result.received, "Random graph: result was not received on iteration " + i);
            check(result.value == expected,
                    "Random graph: expected " + expected + ", got " + result.value + " on iteration " + i);
        }

        System.out.println("testRandomComputationalGraph passed");
    }

    // =========================
    // Helper agents
    // =========================

    static class DummyAgent implements Agent {
        String name;
        int callCount = 0;
        String lastTopic = null;
        Message lastMessage = null;

        DummyAgent(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void reset() {
            callCount = 0;
            lastTopic = null;
            lastMessage = null;
        }

        public void callback(String topic, Message msg) {
            callCount++;
            lastTopic = topic;
            lastMessage = msg;
        }

        public void close() {}
    }

    static class ResultAgent implements Agent {
        String name;
        boolean received = false;
        double value = Double.NaN;

        ResultAgent(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void reset() {
            received = false;
            value = Double.NaN;
        }

        public void callback(String topic, Message msg) {
            received = true;
            value = msg.asDouble;
        }

        public void close() {}
    }

    static abstract class BinaryAgent implements Agent {
        String name;
        String input1;
        String input2;
        String output;

        Double val1 = null;
        Double val2 = null;

        BinaryAgent(String name, String input1, String input2, String output) {
            this.name = name;
            this.input1 = input1;
            this.input2 = input2;
            this.output = output;
        }

        public String getName() {
            return name;
        }

        public void reset() {
            val1 = null;
            val2 = null;
        }

        public void callback(String topic, Message msg) {
            if (msg == null || Double.isNaN(msg.asDouble)) {
                return;
            }

            if (topic.equals(input1)) {
                val1 = msg.asDouble;
            } else if (topic.equals(input2)) {
                val2 = msg.asDouble;
            }

            if (val1 != null && val2 != null) {
                double result = calculate(val1, val2);
                TopicManagerSingleton.get().getTopic(output).publish(new Message(result));
                reset();
            }
        }

        public void close() {}

        abstract double calculate(double x, double y);
    }

    static class SumAgent extends BinaryAgent {
        SumAgent(String name, String input1, String input2, String output) {
            super(name, input1, input2, output);
        }

        double calculate(double x, double y) {
            return x + y;
        }
    }

    static class SubAgent extends BinaryAgent {
        SubAgent(String name, String input1, String input2, String output) {
            super(name, input1, input2, output);
        }

        double calculate(double x, double y) {
            return x - y;
        }
    }

    static class MulAgent extends BinaryAgent {
        MulAgent(String name, String input1, String input2, String output) {
            super(name, input1, input2, output);
        }

        double calculate(double x, double y) {
            return x * y;
        }
    }
}