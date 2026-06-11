package test;

import java.util.Random;

public class MainTrain {

    public static void main(String[] args) {
        int c = Thread.activeCount();
        GenericConfig gc = new GenericConfig();
        gc.setConfFile("C:/Users/97252/eclipse-workspace/AdvancedProgramming/advancedProgramming4/src/config_files/simple.conf"); // change to the exact location where you put the file.
        gc.create();

        // Test 1: Verify the correct number of threads are created
        if (Thread.activeCount() != c + 2) {
            System.out.println("The configuration did not create the right number of threads (-10)");
        }

        double result[] = {0.0};

        TopicManagerSingleton.get().getTopic("D").subscribe(new Agent() {

            @Override
            public String getName() {
                return "";
            }

            @Override
            public void reset() {
            }

            @Override
            public void callback(String topic, Message msg) {
                result[0] = msg.asDouble;
            }

            @Override
            public void close() {
            }

        });

        Random r = new Random();

        // Test 2: Verify correct addition and increment operations
        for (int i = 0; i < 9; i++) {
            int x, y;
            x = r.nextInt(1000);
            y = r.nextInt(1000);
            TopicManagerSingleton.get().getTopic("A").publish(new Message(x));
            TopicManagerSingleton.get().getTopic("B").publish(new Message(y));

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            if (result[0] != x + y + 1) {
                System.out.println("Your agents did not produce the desired result (-10)");
            }
        }

        // Test 3: Edge case - Publish invalid (non-numeric) messages
        TopicManagerSingleton.get().getTopic("A").publish(new Message("invalid"));
        TopicManagerSingleton.get().getTopic("B").publish(new Message("invalid"));

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        if (result[0] == 0.0) {
            System.out.println("Agents did not handle invalid input correctly (-10): " + result[0]);
        }

        // Test 4: Edge case - Publish to topics with no subscribers
        TopicManagerSingleton.get().getTopic("UnusedTopic").publish(new Message(42));
        // No assertion here, just ensuring no exceptions are thrown.

        // Test 5: Reset agents and verify behavior
        gc.close();
        gc.create(); // Recreate agents after closing

        TopicManagerSingleton.get().getTopic("A").publish(new Message(5));
        TopicManagerSingleton.get().getTopic("B").publish(new Message(10));

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        if (result[0] != 16) { // 5 + 10 + 1
            System.out.println("Agents did not reset correctly (-10)");
        }

        // Test 6: Verify all threads are closed after gc.close()
        gc.close();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        if (Thread.activeCount() != c) {
            System.out.println("Your code did not close all threads (-10)");
        }

        // Test 7: Edge case - Empty configuration file
        try {
            GenericConfig emptyConfig = new GenericConfig();
            emptyConfig.setConfFile("/Users/omritriki/IdeaProjects/AP-4/src/config_files/empty.conf");
            emptyConfig.create();
            System.out.println("Empty configuration file did not throw an error (-10)");
        } catch (Exception e) {
            // Expected behavior
        }

        // Test 8: Edge case - Invalid configuration file format
        try {
            GenericConfig invalidConfig = new GenericConfig();
            invalidConfig.setConfFile("/Users/omritriki/IdeaProjects/AP-4/src/config_files/invalid.conf");
            invalidConfig.create();
            System.out.println("Invalid configuration file did not throw an error (-10)");
        } catch (Exception e) {
            // Expected behavior
        }

        System.out.println("done");
    }
}