import server.HTTPServer;
import server.MyHTTPServer;
import servlets.ConfLoader;
import servlets.HtmlLoader;
import servlets.TopicDisplayer;

/**
 * Main entry point for the Computational Graph server.
 * Open http://localhost:8080/app/index.html in your browser.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        HTTPServer server = new MyHTTPServer(8080, 5);

        server.addServlet("GET",  "/publish", new TopicDisplayer());
        server.addServlet("POST", "/upload",  new ConfLoader());
        server.addServlet("GET",  "/app/",    new HtmlLoader("html_files"));

        server.start();
        System.out.println("Server started -> http://localhost:8080/app/index.html");
        System.out.println("Press Enter to stop...");
        System.in.read();

        server.close();
        System.out.println("done");
    }
}
