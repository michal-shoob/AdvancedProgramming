package servlets;

import server.RequestParser.RequestInfo;
import test.Message;
import test.Topic;
import test.TopicManagerSingleton;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Servlet that handles {@code GET /publish?topic=X&amp;message=Y}.
 *
 * <p>If both {@code topic} and {@code message} query parameters are present
 * and {@code message} is a valid double, the value is published to the named
 * topic via {@link TopicManagerSingleton}.  The response is always an HTML
 * table showing every currently registered topic and its most recent value,
 * so the browser panel refreshes after each publish.</p>
 *
 * <p>If no parameters are provided (plain {@code GET /publish}) the table is
 * returned without publishing anything — useful for an initial page load.</p>
 */
public class TopicDisplayer implements Servlet {

    /**
     * Publishes the given message (if any) and returns an HTML table of all
     * topic values.
     *
     * @param ri       the parsed request (may contain {@code topic} and
     *                 {@code message} query parameters)
     * @param toClient output stream to write the HTTP response to
     * @throws IOException if writing to the client fails
     */
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        Map<String, String> params = ri.getParameters();
        String topicName  = params.get("topic");
        String messageStr = params.get("message");

        if (topicName != null && messageStr != null && !topicName.isEmpty()) {
            try {
                double value = Double.parseDouble(messageStr);
                TopicManagerSingleton.get().getTopic(topicName).publish(new Message(value));
            } catch (NumberFormatException ignored) {}
        }

        Collection<Topic> topics = TopicManagerSingleton.get().getTopics();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
            .append("<style>")
            .append("body{font-family:Arial,sans-serif;padding:8px;background:#1e1e2e;color:#cdd6f4;margin:0;}")
            .append("h3{color:#89b4fa;margin:0 0 8px;font-size:0.95em;}")
            .append("table{border-collapse:collapse;width:100%;}")
            .append("th{background:#313244;color:#89b4fa;padding:6px 10px;text-align:left;font-size:0.85em;}")
            .append("td{padding:6px 10px;border-bottom:1px solid #313244;font-size:0.85em;}")
            .append("tr:hover td{background:#313244;}")
            .append("</style></head><body>")
            .append("<h3>Topic Values</h3>")
            .append("<table><tr><th>Topic</th><th>Value</th></tr>");

        if (topics.isEmpty()) {
            html.append("<tr><td colspan='2'><em>No topics yet</em></td></tr>");
        } else {
            for (Topic t : topics) {
                String val = (t.getMessage() != null) ? t.getMessage().asText : "-";
                html.append("<tr><td>").append(esc(t.name)).append("</td>")
                    .append("<td>").append(esc(val)).append("</td></tr>");
            }
        }
        html.append("</table></body></html>");
        sendHtml(toClient, html.toString());
    }

    /**
     * Writes a complete HTTP 200 response with the given HTML body.
     *
     * @param out  the client output stream
     * @param body the HTML string to send
     * @throws IOException if writing fails
     */
    private void sendHtml(OutputStream out, String body) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html; charset=UTF-8\r\n"
                + "Content-Length: " + body.getBytes("UTF-8").length + "\r\n"
                + "\r\n" + body;
        out.write(response.getBytes("UTF-8"));
    }

    /**
     * Escapes HTML special characters in the given string to prevent XSS.
     *
     * @param s the raw string; may be {@code null}
     * @return the escaped string, or an empty string if {@code s} is {@code null}
     */
    private String esc(String s) {
        return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {}
}
