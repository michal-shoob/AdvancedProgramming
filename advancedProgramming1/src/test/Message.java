package test;

import java.util.Date;

public class Message {
    public final byte[] data;
    public final String asText;
    public final double asDouble;
    public final Date date;

    // Primary constructor - receives a String and converts to all other fields
    public Message(String text) {
        // null is invalid input - throw exception
        if (text == null) {
            throw new NullPointerException("Message constructor: input string cannot be null !");
        }
        this.asText = text;
        this.data = text.getBytes();

        // trim whitespace before parsing to handle strings like " 3.14 "
        // if parsing fails store NaN
        double d;
        try {
            d = Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            d = Double.NaN;
        }
        this.asDouble = d;

        // store the time of message creation
        this.date = new Date();
    }

    // Helper constructor - receives byte array, converts to String and calls primary constructor
    // handle null byte array - treat as empty string
    public Message(byte[] data) {
        this(data == null ? "" : new String(data));
    }

    // Helper constructor - receives double, converts to String and calls primary constructor
    // NaN and Infinity are valid doubles - Double.toString handles them correctly
    public Message(double value) {
        this(Double.toString(value));
    }
    
    public Message(byte data) {
        this(Byte.toString(data));
    }
}