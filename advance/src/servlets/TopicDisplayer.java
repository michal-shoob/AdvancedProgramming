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
 * Handles GET /publish?topic=X&message=Y
 * Publishes a value to a topic and returns an HTML table of all topic values.
 */
public class TopicDisplayer implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        Map<String, String> params = ri.getParameters();
        String topicName = params.get("topic");
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

    private void sendHtml(OutputStream out, String body) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html; charset=UTF-8\r\n"
                + "Content-Length: " + body.getBytes("UTF-8").length + "\r\n"
                + "\r\n" + body;
        out.write(response.getBytes("UTF-8"));
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    @Override
    public void close() throws IOException {}
}
