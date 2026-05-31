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

    private GenericConfig currentConfig = null;

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        Map<String, String> params = ri.getParameters();
        byte[] contentBytes = ri.getContent();

        // Extract filename 
        String filename = params.getOrDefault("filename", "uploaded.conf");
        filename = filename.replaceAll("^\"|\"$", "").trim();
        if (filename.isEmpty()) filename = "uploaded.conf";

        String rawContent = new String(contentBytes, "UTF-8").trim();

        // === התיקון: ניקוי שורות ה-Boundary שהדפדפן מוסיף אוטומטית ===
        StringBuilder cleanContent = new StringBuilder();
        for (String line : rawContent.split("\n")) {
            String trimmed = line.trim();
            // נתעלם משורות של WebKitFormBoundary שהדפדפן דוחף לסוף הקובץ
            if (trimmed.startsWith("----") || trimmed.contains("WebKitFormBoundary")) {
                continue;
            }
            cleanContent.append(trimmed).append("\n");
        }
        String configContent = cleanContent.toString().trim();
        // =========================================================

        String body;
        if (configContent.isEmpty()) {
            body = errorHtml("No configuration content received.");
        } else {
            try {
                // Save config file to disk
                File dir = new File("config_files");
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