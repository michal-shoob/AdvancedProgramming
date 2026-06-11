package test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Decorator that wraps any {@link Agent} and executes its callbacks on a
 * dedicated background thread (Active Object pattern).
 *
 * <p>When {@link #callback(String, Message)} is called from a publisher's
 * thread, the message is placed in a bounded blocking queue and the call
 * returns immediately.  A single worker thread drains the queue and calls the
 * wrapped agent's {@code callback} sequentially, ensuring that the underlying
 * agent never needs to be thread-safe.</p>
 *
 * <p>The worker thread is a daemon thread, so it does not prevent JVM shutdown,
 * but {@link #close()} should still be called explicitly to drain remaining
 * messages and release resources cleanly.</p>
 */
public class ParallelAgent implements Agent {

    /** The wrapped agent whose callbacks are executed on the worker thread. */
    private final Agent agent;

    /** Bounded queue that decouples the caller's thread from the worker thread. */
    private final BlockingQueue<QueuedMessage> queue;

    /** Dedicated worker thread that processes queued callbacks. */
    private final Thread thread;

    /** Set to {@code false} by {@link #close()} to stop the worker loop. */
    private volatile boolean thread_is_active = true;

    /**
     * Pairs a topic name with its message so both can travel through the queue together.
     */
    private static class QueuedMessage {
        final String topic;
        final Message message;

        /**
         * @param topic   the topic name associated with the message
         * @param message the message payload
         */
        public QueuedMessage(String topic, Message message) {
            this.topic = topic;
            this.message = message;
        }
    }

    /**
     * Creates a {@code ParallelAgent} wrapping the given agent with a queue of
     * the specified capacity.
     *
     * @param agent    the agent to wrap; must not be {@code null}
     * @param capacity maximum number of messages the queue can hold before
     *                 {@link #callback} blocks; must be positive
     * @throws IllegalArgumentException if {@code agent} is {@code null} or
     *                                  {@code capacity} is not positive
     */
    public ParallelAgent(Agent agent, int capacity) {
        if (agent == null) {
            throw new IllegalArgumentException("Null agent!");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }

        this.agent = agent;
        this.queue = new ArrayBlockingQueue<>(capacity);

        this.thread = new Thread(() -> {
            while (thread_is_active) {
                try {
                    QueuedMessage next = queue.take();
                    if (!thread_is_active) break;
                    agent.callback(next.topic, next.message);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    // swallow unexpected exceptions so the worker keeps running
                }
            }
        });

        this.thread.setDaemon(true);
        this.thread.start();
    }

    /**
     * {@inheritDoc}
     * Delegates to the wrapped agent's {@link Agent#getName()}.
     */
    @Override
    public String getName() {
        return agent.getName();
    }

    /**
     * {@inheritDoc}
     * Delegates to the wrapped agent's {@link Agent#reset()}.
     */
    @Override
    public void reset() {
        agent.reset();
    }

    /**
     * Enqueues the message for asynchronous delivery to the wrapped agent.
     * Returns immediately; the actual callback runs on the worker thread.
     * Silently ignores {@code null} inputs or calls made after {@link #close()}.
     *
     * @param topic the topic name
     * @param msg   the message to deliver
     */
    @Override
    public void callback(String topic, Message msg) {
        if (topic == null || msg == null) return;
        if (!thread_is_active) return;
        try {
            queue.put(new QueuedMessage(topic, msg));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Stops the worker thread and releases all resources.
     *
     * <p>Sets the active flag to {@code false}, interrupts the worker thread
     * (in case it is blocked on {@code take()}), waits for it to finish, then
     * closes the wrapped agent.</p>
     */
    @Override
    public void close() {
        thread_is_active = false;
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        agent.close();
    }
}
