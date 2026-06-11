package test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

// Decorator for Agent that implements the Active Object concurrency pattern.
// Separates the thread that initiates a callback from the thread that runs it.
public class ParallelAgent implements Agent {

    private final Agent agent;                         
    private final BlockingQueue<QueuedMessage> queue;  
    private final Thread thread;                       
    private volatile boolean thread_is_active = true;           

    // Bundles topic and message together so both can be stored in the queue
    private static class QueuedMessage {
        final String topic;
        final Message message;

        public QueuedMessage(String topic, Message message) {
            this.topic = topic;
            this.message = message;
        }
    }

    // Constructor - takes the agent to wrap and the capacity of the queue
    public ParallelAgent(Agent agent, int capacity) {
        // null agent is invalid
        if (agent == null) {
            throw new IllegalArgumentException("Null agent!");
        }

        // capacity must be positive
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }

        this.agent = agent;
        this.queue = new ArrayBlockingQueue<>(capacity);

        // Start a dedicated thread that processes messages from the queue one by one.
        // take() blocks when queue is empty - thread rests until a message arrives.
        this.thread = new Thread(() -> {
            while (thread_is_active) {
                try {
                    QueuedMessage next = queue.take();
                    // check thread_is_active flag again after waking up (close() may have interrupted us)
                    if (!thread_is_active) break;
                    agent.callback(next.topic, next.message);
                } catch (InterruptedException e) {
                    // interrupted by close() - exit the loop
                    break;
                } catch (Exception e) {
                    
                }
            }
        });

        this.thread.setDaemon(true);
        this.thread.start();
    }

    @Override
    public String getName() {
        return agent.getName();
    }

    @Override
    public void reset() {
        agent.reset();
    }

    // Instead of running agent.callback() directly, puts the message in the queue.
    // Returns immediately regardless of how long agent.callback() takes.
    @Override
    public void callback(String topic, Message msg) {
        // ignore null inputs
        if (topic == null || msg == null) {
            return;
        }
        // ignore callbacks after close()
        if (!thread_is_active) {
            return;
        }
        try {
            // put() blocks if queue is full - waits until space is available
            queue.put(new QueuedMessage(topic, msg));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Stops the processing thread cleanly - guarantees no threads remain open after this call
    @Override
    public void close() {
        thread_is_active = false;
        // interrupt the thread in case it is blocked on take()
        thread.interrupt();
        try {
            // wait for the thread to fully finish before returning
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // close the wrapped agent as well
        agent.close();
    }
}