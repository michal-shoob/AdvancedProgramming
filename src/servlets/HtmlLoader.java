package servlets;

import server.RequestParser.RequestInfo;

import java.io.*;
import java.nio.file.Files;

/**
 * Handles GET /app/* - serves static HTML files from a configured folder.
 * The folder is a constructor parameter, not hard-coded.
 */
public class HtmlLoader implements Servlet {

    private final String htmlFolder;

    public HtmlLoader(String htmlFolder) {
        this.htmlFolder = htmlFolder;
    }

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        // URI segments: /app/index.html -> ["app", "index.html"]
        String[] segments = ri.getUriSegments();
        String filename = (segments.length >= 2) ? segments[1] : "index.html";
        // support sub-paths like /app/sub/file.html
        if (segments.length > 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < segments.length; i++) {
                if (i > 1) sb.append(File.separator);
                sb.append(segments[i]);
            }
            filename = sb.toString();
        }

        File file = new File(htmlFolder + File.separator + filename);

        if (file.exists() && file.isFile()) {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String ct = filename.endsWith(".css") ? "text/css"
                      : filename.endsWith(".js")  ? "application/javascript"
                      : "text/html; charset=UTF-8";
            String header = "HTTP/1.1 200 OK\r\nContent-Type: " + ct
                    + "\r\nContent-Length: " + bytes.length + "\r\n\r\n";
            toClient.write(header.getBytes("UTF-8"));
            toClient.write(bytes);
        } else {
            String body = "<!DOCTYPE html><html><body style='font-family:Arial;padding:20px'>"
                    + "<h2>404 - Not Found</h2><p>File not found: <b>"
                    + esc(filename) + "</b></p></body></html>";
            String response = "HTTP/1.1 404 Not Found\r\nContent-Type: text/html; charset=UTF-8\r\n"
                    + "Content-Length: " + body.getBytes("UTF-8").length + "\r\n\r\n" + body;
            toClient.write(response.getBytes("UTF-8"));
        }
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    @Override
    public void close() throws IOException {}
}
