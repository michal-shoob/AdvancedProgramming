import server.HTTPServer;
import server.MyHTTPServer;
import servlets.ConfLoader;
import servlets.HtmlLoader;
import servlets.TopicDisplayer;

/**
 * Application entry point for the Computational Graph server.
 *
 * <p>Starts an HTTP server on port 8080 with three registered servlets:</p>
 * <ul>
 *   <li>{@code GET  /publish} — {@link TopicDisplayer}: publish a value to a topic
 *       and view all current topic values.</li>
 *   <li>{@code POST /upload}  — {@link ConfLoader}: upload and deploy a
 *       {@code .conf} file that wires up a new set of agents.</li>
 *   <li>{@code GET  /app/}    — {@link HtmlLoader}: serve the static UI files
 *       (HTML, CSS, JS) from the {@code html_files} directory.</li>
 * </ul>
 *
 * <p>The server automatically detects whether it is started from the repository
 * root (where the project lives inside {@code advancedProgramming_finalProject/})
 * or from the project directory itself, so no run-configuration changes are
 * needed in either case.</p>
 *
 * <p>Open <a href="http://localhost:8080/app/index.html">http://localhost:8080/app/index.html</a>
 * in a browser after starting.</p>
 */
public class Main {

    /**
     * Starts the server, waits for Enter to be pressed, then shuts down cleanly.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if the server fails to start
     */
    public static void main(String[] args) throws Exception {
        HTTPServer server = new MyHTTPServer(8080, 5);

        // Detect whether html_files/ is in the working directory or one level down
        String base = new java.io.File("html_files").isDirectory()
                ? ""
                : "advancedProgramming_finalProject/";

        server.addServlet("GET",  "/publish", new TopicDisplayer());
        server.addServlet("POST", "/upload",  new ConfLoader(base + "config_files"));
        server.addServlet("GET",  "/app/",    new HtmlLoader(base + "html_files"));

        server.start();
        System.out.println("Server started -> http://localhost:8080/app/index.html");
        System.out.println("Press Enter to stop...");
        System.in.read();

        server.close();
        System.out.println("done");
    }
}
