package servlets;

import server.RequestParser.RequestInfo;
import test.GenericConfig;
import test.Graph;
import test.TopicManagerSingleton;
import views.HtmlGraphWriter;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Servlet that handles {@code POST /upload} — the "Deploy" action in the UI.
 *
 * <p>Workflow:</p>
 * <ol>
 *   <li>Extracts the config file content from the multipart/form-data request body.</li>
 *   <li>Saves the content to {@code &lt;configDir&gt;/&lt;filename&gt;}.</li>
 *   <li>Closes the previous {@link GenericConfig} (if any) and clears all topics.</li>
 *   <li>Creates a new {@link GenericConfig}, loads the saved file, and starts all agents.</li>
 *   <li>Builds a {@link Graph} from the live topics and returns an HTML graph visualisation.</li>
 * </ol>
 *
 * <p>{@code handle} is {@code synchronized} to prevent concurrent deploys from
 * corrupting the shared agent state when the Deploy button is clicked rapidly.</p>
 */
public class ConfLoader implements Servlet {

    /** Directory where uploaded config files are saved. */
    private final String configDir;

    /** The most recently loaded configuration; {@code null} if none has been deployed yet. */
    private GenericConfig currentConfig = null;

    /**
     * Creates a {@code ConfLoader} that saves config files to {@code configDir}.
     *
     * @param configDir directory path for storing uploaded config files
     */
    public ConfLoader(String configDir) {
        this.configDir = configDir;
    }

    /**
     * Handles a config-file upload, deploys the new configuration, and returns
     * a graph visualisation HTML page.
     *
     * <p>Synchronized so that rapid consecutive clicks do not cause concurrent
     * modifications to the topic registry or the agent list.</p>
     *
     * @param ri       the parsed POST request; body contains the multipart file
     * @param toClient output stream for the HTTP response
     * @throws IOException if writing to the client fails
     */
    @Override
    public synchronized void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        Map<String, String> params = ri.getParameters();
        byte[] contentBytes = ri.getContent();

        String filename = params.getOrDefault("filename", "uploaded.conf");
        filename = filename.replaceAll("^\"|\"$", "").trim();
        if (filename.isEmpty()) filename = "uploaded.conf";

        String rawContent = new String(contentBytes, "UTF-8");

        // --- Extract file content from multipart/form-data body ---
        // Multipart structure: --boundary → part-headers → blank line → content → --boundary--
        // Detect multipart by checking whether the first non-empty line starts with "--".
        String configContent;
        boolean isMultipart = false;
        for (String l : rawContent.split("\r?\n")) {
            if (!l.trim().isEmpty()) { isMultipart = l.trim().startsWith("--"); break; }
        }

        if (isMultipart) {
            int sep = rawContent.indexOf("\r\n\r\n");
            String afterHeaders = (sep >= 0)
                    ? rawContent.substring(sep + 4)
                    : rawContent.replaceFirst("(?s)^.*?\n\n", "");

            StringBuilder sb = new StringBuilder();
            for (String line : afterHeaders.split("\r?\n")) {
                if (!line.trim().startsWith("--")) sb.append(line.trim()).append("\n");
            }
            configContent = sb.toString().trim();
        } else {
            configContent = rawContent.trim();
        }

        String body;
        if (configContent.isEmpty()) {
            body = errorHtml("No configuration content received.");
        } else {
            try {
                // Save the config file to disk
                File dir = new File(configDir);
                if (!dir.exists()) dir.mkdirs();
                File saved = new File(dir, filename);
                try (FileWriter fw = new FileWriter(saved)) { fw.write(configContent); }

                // Tear down the previous configuration
                if (currentConfig != null) {
                    currentConfig.close();
                    TopicManagerSingleton.get().clear();
                }

                // Load the new configuration
                currentConfig = new GenericConfig();
                currentConfig.setConfFile(saved.getPath());
                currentConfig.create();

                // Build and render the graph
                Graph graph = new Graph();
                graph.createFromTopics();

                List<String> lines = HtmlGraphWriter.getGraphHTML(graph);
                StringBuilder sb = new StringBuilder();
                for (String l : lines) sb.append(l).append("\n");

                // Refresh the topic-values panel after a successful deploy
                sb.append("<script>\n");
                sb.append("  if(window.parent && window.parent.frames['values_frame']) {\n");
                sb.append("      window.parent.frames['values_frame'].location.href = 'http://localhost:8080/publish';\n");
                sb.append("  }\n");
                sb.append("</script>\n");

                body = sb.toString();

            } catch (Exception e) {
                body = errorHtml("Error loading config: " + esc(e.getMessage()));
            }
        }

        String response = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n"
                + "Content-Length: " + body.getBytes("UTF-8").length + "\r\n\r\n" + body;
        toClient.write(response.getBytes("UTF-8"));
    }

    /**
     * Generates an HTML error page with the given message.
     *
     * @param msg the error description to display
     * @return a complete HTML string
     */
    private String errorHtml(String msg) {
        return "<!DOCTYPE html><html><body style='font-family:Arial;padding:20px;background:#1e1e2e;color:#f38ba8'>"
                + "<h2>Configuration Error</h2><p>" + msg + "</p></body></html>";
    }

    /**
     * Escapes HTML special characters to prevent XSS in error messages.
     *
     * @param s raw string; may be {@code null}
     * @return the escaped string
     */
    private String esc(String s) {
        return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    /**
     * Closes the currently active configuration and releases all agents.
     *
     * @throws IOException if shutdown fails
     */
    @Override
    public void close() throws IOException {
        if (currentConfig != null) currentConfig.close();
    }
}
