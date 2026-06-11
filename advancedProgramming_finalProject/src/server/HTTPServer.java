package server;

import servlets.Servlet;

/**
 * Minimal interface for an HTTP server that dispatches requests to registered servlets.
 *
 * <p>Servlets are mapped by HTTP method (GET, POST, DELETE) and URI prefix.
 * The server runs on its own thread and processes each incoming connection in a
 * worker-thread pool.</p>
 *
 * <p>Typical usage:</p>
 * <pre>
 *   HTTPServer server = new MyHTTPServer(8080, 5);
 *   server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
 *   server.start();
 * </pre>
 */
public interface HTTPServer extends Runnable {

    /**
     * Registers a servlet for the given HTTP method and URI prefix.
     * If a servlet is already registered for the same method and URI it is replaced.
     *
     * @param httpCommand HTTP method in any case (e.g. {@code "GET"}, {@code "POST"})
     * @param uri         URI prefix to match (e.g. {@code "/app/"})
     * @param s           servlet instance that will handle matching requests
     */
    void addServlet(String httpCommand, String uri, Servlet s);

    /**
     * Removes the servlet registered for the given HTTP method and URI prefix.
     * Does nothing if no such mapping exists.
     *
     * @param httpCommand HTTP method (e.g. {@code "GET"})
     * @param uri         URI prefix previously passed to {@link #addServlet}
     */
    void removeServlet(String httpCommand, String uri);

    /**
     * Starts the server thread and begins accepting client connections.
     * Must be called after all servlets have been registered.
     */
    void start();

    /**
     * Stops the server, closes the listening socket, and shuts down the thread pool.
     * Waits up to 5 seconds for in-progress requests to finish.
     */
    void close();
}
