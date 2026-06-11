package test;

import java.util.Date;

/**
 * Immutable value object that carries a single piece of data through the topic system.
 *
 * <p>A {@code Message} stores its payload in three equivalent representations so
 * that subscribers can consume whichever form suits them:</p>
 * <ul>
 *   <li>{@link #data}     – raw UTF-8 bytes</li>
 *   <li>{@link #asText}   – the original string representation</li>
 *   <li>{@link #asDouble} – numeric value, or {@link Double#NaN} if not parseable</li>
 * </ul>
 *
 * <p>All fields are {@code final} and set at construction time; the creation
 * timestamp is captured in {@link #date}.</p>
 */
public class Message {

    /** Raw bytes of the message payload (UTF-8 encoding of {@link #asText}). */
    public final byte[] data;

    /** String representation of the payload. Never {@code null}. */
    public final String asText;

    /**
     * Numeric representation of the payload.
     * Set to {@link Double#NaN} when the text cannot be parsed as a number.
     */
    public final double asDouble;

    /** Timestamp recording when this message was created. */
    public final Date date;

    /**
     * Primary constructor — creates a message from a string payload.
     *
     * @param text the string payload; must not be {@code null}
     * @throws NullPointerException if {@code text} is {@code null}
     */
    public Message(String text) {
        if (text == null) {
            throw new NullPointerException("Message constructor: input string cannot be null !");
        }
        this.asText = text;
        this.data = text.getBytes();

        double d;
        try {
            d = Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            d = Double.NaN;
        }
        this.asDouble = d;
        this.date = new Date();
    }

    /**
     * Convenience constructor — creates a message from a byte array.
     * A {@code null} array is treated as an empty string.
     *
     * @param data raw bytes; may be {@code null}
     */
    public Message(byte[] data) {
        this(data == null ? "" : new String(data));
    }

    /**
     * Convenience constructor — creates a message from a {@code double} value.
     * {@link Double#NaN} and infinity are valid inputs and are stored as-is.
     *
     * @param value the numeric payload
     */
    public Message(double value) {
        this(Double.toString(value));
    }
}
