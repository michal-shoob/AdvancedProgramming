package test;

public class MainTrain2 {
    public static void main(String[] args) {
        System.out.println("Starting extra tests...");

        // ----- Test 1: Direct PlusAgent test -----
        final double[] plusResult = new double[1];
        Agent plusResultAgent = new Agent() {
            @Override
            public String getName() { return "PlusResultSubscriber"; }
            @Override public void reset() {}
            @Override public void close() {}
            @Override public void callback(String topic, Message msg) {
                plusResult[0] = msg.asDouble;
            }
        };
        TopicManagerSingleton.get().getTopic("TestC").subscribe(plusResultAgent);
        PlusAgent plusAgent = new PlusAgent(new String[]{"TestA", "TestB"}, new String[]{"TestC"});
        TopicManagerSingleton.get().getTopic("TestA").publish(new Message(10));
        TopicManagerSingleton.get().getTopic("TestB").publish(new Message(20));
        sleep(100);
        System.out.println(plusResult[0] == 30 ?
                "PlusAgent direct test passed" :
                "PlusAgent direct test FAILED: got " + plusResult[0]);

        // ----- Test 2: Direct IncAgent test -----
        final double[] incResult = new double[1];
        Agent incResultAgent = new Agent() {
            @Override public String getName() { return "IncResultSubscriber"; }
            @Override public void reset() {}
            @Override public void close() {}
            @Override public void callback(String topic, Message msg) {
                incResult[0] = msg.asDouble;
            }
        };
        TopicManagerSingleton.get().getTopic("TestD").subscribe(incResultAgent);
        IncAgent incAgent = new IncAgent(new String[]{"TestC"}, new String[]{"TestD"});
        TopicManagerSingleton.get().getTopic("TestC").publish(new Message(30));
        sleep(100);
        System.out.println(incResult[0] == 31 ?
                "IncAgent direct test passed" :
                "IncAgent direct test FAILED: got " + incResult[0]);

        // ----- Test 3: Chained Agents -----
        final double[] chainResult = new double[1];
        Agent chainResultAgent = new Agent() {
            @Override public String getName() { return "ChainResultSubscriber"; }
            @Override public void reset() {}
            @Override public void close() {}
            @Override public void callback(String topic, Message msg) {
                chainResult[0] = msg.asDouble;
            }
        };
        TopicManagerSingleton.get().getTopic("ChainD").subscribe(chainResultAgent);
        ParallelAgent parallelPlusChain = new ParallelAgent(
                new PlusAgent(new String[]{"ChainA", "ChainB"}, new String[]{"ChainC"}), 10);
        ParallelAgent parallelIncChain = new ParallelAgent(
                new IncAgent(new String[]{"ChainC"}, new String[]{"ChainD"}), 10);
        TopicManagerSingleton.get().getTopic("ChainA").publish(new Message(5));
        TopicManagerSingleton.get().getTopic("ChainB").publish(new Message(15));
        sleep(200);
        System.out.println(chainResult[0] == 21 ?
                "Chained agents test passed" :
                "Chained agents test FAILED: got " + chainResult[0]);

        // ----- Test 4: ParallelAgent close -----
        ParallelAgent testParallel = new ParallelAgent(
                new PlusAgent(new String[]{"TempA", "TempB"}, new String[]{"TempC"}), 10);
        testParallel.callback("TempA", new Message(100));
        testParallel.callback("TempB", new Message(200));
        sleep(100);
        testParallel.close();
        System.out.println("ParallelAgent close() test executed.");

        // ----- Test 5: Invalid input to PlusAgent (should be ignored silently) -----
        try {
            PlusAgent invalidPlus = new PlusAgent(new String[]{"InvA", "InvB"}, new String[]{"InvC"});
            invalidPlus.callback("InvA", new Message("invalid"));  // Should NOT throw
            System.out.println("PlusAgent invalid input test passed (handled internally)");
        } catch (Exception e) {
            System.out.println("PlusAgent invalid input test FAILED: threw exception");
        }

        // ----- Test 6: Invalid input to IncAgent (should be ignored silently) -----
        try {
            IncAgent invalidInc = new IncAgent(new String[]{"InvD"}, new String[]{"InvE"});
            invalidInc.callback("InvD", new Message("invalid"));  // Should NOT throw
            System.out.println("IncAgent invalid input test passed (handled internally)");
        } catch (Exception e) {
            System.out.println("IncAgent invalid input test FAILED: threw exception");
        }

        System.out.println("Extra tests completed.");
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}
