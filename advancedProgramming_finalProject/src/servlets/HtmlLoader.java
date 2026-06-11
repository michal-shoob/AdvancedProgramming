package servlets;

import server.RequestParser.RequestInfo;

import java.io.*;
import java.nio.file.Files;

/**
 * Servlet that serves static HTML, CSS, and JS files from a configured folder.
 *
 * <p>Registered on the path prefix {@code /app/}.  The file name is taken from
 * the URI segment after the prefix; if none is given, {@code index.html} is used.</p>
 *
 * <p>Returns {@code 200 OK} with the correct {@code Content-Type}, or
 * {@code 404 Not Found} if the file does not exist.</p>
 */
public class HtmlLoader implements Servlet {

    /** Base directory from which static files are served. */
    private final String htmlFolder;

    /**
     * Creates an {@code HtmlLoader} that serves files from {@code htmlFolder}.
     *
     * @param htmlFolder directory containing the static files
     *                   (relative to the JVM working directory, or absolute)
     */
    public HtmlLoader(String htmlFolder) {
        this.htmlFolder = htmlFolder;
    }

    /**
     * Resolves the requested file and writes it to the client.
     * Returns a {@code 404} response if the file is not found.
     *
     * @param ri       parsed request; URI segments identify the file
     * @param toClient output stream for the HTTP response
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        String[] segments = ri.getUriSegments();
        String filename = (segments.length >= 2) ? segments[1] : "index.html";

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
            toClient.write(("HTTP/1.1 200 OK\r\nContent-Type: " + ct
                    + "\r\nContent-Length: " + bytes.length + "\r\n\r\n").getBytes("UTF-8"));
            toClient.write(bytes);
        } else {
            String body = "<!DOCTYPE html><html><body style='font-family:Arial;padding:20px'>"
                    + "<h2>404 - Not Found</h2><p>File not found: <b>"
                    + esc(filename) + "</b></p></body></html>";
            toClient.write(("HTTP/1.1 404 Not Found\r\nContent-Type: text/html; charset=UTF-8\r\n"
                    + "Content-Length: " + body.getBytes("UTF-8").length + "\r\n\r\n" + body)
                    .getBytes("UTF-8"));
        }
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

    /** {@inheritDoc} */
    @Override public void close() throws IOException {}
}
