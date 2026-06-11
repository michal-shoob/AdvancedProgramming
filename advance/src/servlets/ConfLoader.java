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
 * Handles POST /upload.
 * Saves the uploaded config file, loads it via GenericConfig,
 * builds a Graph from the resulting topics, and returns the graph HTML.
 */
public class ConfLoader implements Servlet {

    private final String configDir;
    private GenericConfig currentConfig = null;

    public ConfLoader(String configDir) {
        this.configDir = configDir;
    }

    @Override
    public synchronized void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        Map<String, String> params = ri.getParameters();
        byte[] contentBytes = ri.getContent();

        // Extract filename 
        String filename = params.getOrDefault("filename", "uploaded.conf");
        filename = filename.replaceAll("^\"|\"$", "").trim();
        if (filename.isEmpty()) filename = "uploaded.conf";

        String rawContent = new String(contentBytes, "UTF-8");

        // Extract only the file content from a multipart/form-data body.
        // Multipart structure: --boundary → part-headers → blank line → content → --boundary--
        // If the first non-empty line starts with "--" it is a multipart upload;
        // skip everything up to (and including) the first blank line, then strip
        // the closing boundary at the end.  Otherwise treat the body as raw content.
        String configContent;
        boolean isMultipart = false;
        for (String l : rawContent.split("\r?\n")) {
            if (!l.trim().isEmpty()) { isMultipart = l.trim().startsWith("--"); break; }
        }

        if (isMultipart) {
            // Find the blank line that separates part-headers from file content
            int sep = rawContent.indexOf("\r\n\r\n");
            String afterHeaders = (sep >= 0)
                    ? rawContent.substring(sep + 4)
                    : rawContent.replaceFirst("(?s)^.*?\n\n", "");

            // Strip closing boundary lines (start with "--") and normalise
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
                // Save config file to disk
                File dir = new File(configDir);
                if (!dir.exists()) dir.mkdirs();
                File saved = new File(dir, filename);
                try (FileWriter fw = new FileWriter(saved)) { fw.write(configContent); }

                // Close previous config cleanly
                if (currentConfig != null) {
                    currentConfig.close();
                    TopicManagerSingleton.get().clear();
                }

                // Load new config
                currentConfig = new GenericConfig();
                currentConfig.setConfFile(saved.getPath());
                currentConfig.create();

                // Build graph from current topics/agents
                Graph graph = new Graph();
                graph.createFromTopics();

                // // Generate HTML visualization
                // List<String> lines = HtmlGraphWriter.getGraphHTML(graph);
                // StringBuilder sb = new StringBuilder();
                // for (String l : lines) sb.append(l).append("\n");
                // body = sb.toString();

// Generate HTML visualization
                List<String> lines = HtmlGraphWriter.getGraphHTML(graph);
                StringBuilder sb = new StringBuilder();
                for (String l : lines) sb.append(l).append("\n");
                
                // === התיקון מתחיל כאן ===
                sb.append("<script>\n");
                sb.append("  if(window.parent && window.parent.frames['values_frame']) {\n");
                sb.append("      window.parent.frames['values_frame'].location.href = 'http://localhost:8080/publish';\n");
                sb.append("  }\n");
                sb.append("</script>\n");
                
                body = sb.toString();
                // === התיקון נגמר כאן ===

            } 
            catch (Exception e) {




            // } catch (Exception e) {
                body = errorHtml("Error loading config: " + esc(e.getMessage()));
            }
        }

        String response = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n"
                + "Content-Length: " + body.getBytes("UTF-8").length + "\r\n\r\n" + body;
        toClient.write(response.getBytes("UTF-8"));
    }

    private String errorHtml(String msg) {
        return "<!DOCTYPE html><html><body style='font-family:Arial;padding:20px;background:#1e1e2e;color:#f38ba8'>"
                + "<h2>Configuration Error</h2><p>" + msg + "</p></body></html>";
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    @Override
    public void close() throws IOException {
        if (currentConfig != null) currentConfig.close();
    }
}