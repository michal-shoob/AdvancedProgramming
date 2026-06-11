package server;

import servlets.Servlet;
import server.RequestParser.RequestInfo;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Concrete HTTP server implementation that handles GET, POST, and DELETE requests.
 *
 * <p>Extends {@link Thread} and implements {@link HTTPServer}.  On {@link #start()}
 * the server begins listening on the configured port using a fixed-size thread pool.
 * Each accepted connection is dispatched to a worker thread that parses the request
 * and delegates to the best-matching registered {@link Servlet}.</p>
 *
 * <p>Servlet matching is prefix-based: the longest registered URI prefix that
 * matches the incoming request path wins.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   HTTPServer server = new MyHTTPServer(8080, 5);
 *   server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
 *   server.start();
 *   // ... later ...
 *   server.close();
 * </pre>
 */
public class MyHTTPServer extends Thread implements HTTPServer {

    /** Port the server listens on. */
    private final int port;

    /** Number of worker threads in the pool. */
    private final int nThreads;

    private final ConcurrentHashMap<String, Servlet> getServlets    = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Servlet> postServlets   = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Servlet> deleteServlets = new ConcurrentHashMap<>();

    /** {@code false} once {@link #close()} is called, causes the accept loop to stop. */
    private volatile boolean running = false;

    private ExecutorService threadPool;
    private ServerSocket serverSocket;

    /**
     * Creates a new server that will listen on the given port with the given
     * number of worker threads.
     *
     * @param port     TCP port to bind (e.g. 8080)
     * @param nThreads size of the worker-thread pool
     */
    public MyHTTPServer(int port, int nThreads) {
        this.port     = port;
        this.nThreads = nThreads;
    }

    /**
     * Returns the servlet map for the given HTTP method, or {@code null} if the
     * method is not supported.
     *
     * @param httpCommand HTTP method string (case-insensitive)
     * @return the corresponding map, or {@code null}
     */
    private ConcurrentHashMap<String, Servlet> getMap(String httpCommand) {
        String cmd = httpCommand.toLowerCase();
        if (cmd.equals("get"))    return getServlets;
        if (cmd.equals("post"))   return postServlets;
        if (cmd.equals("delete")) return deleteServlets;
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void addServlet(String httpCommand, String uri, Servlet s) {
        ConcurrentHashMap<String, Servlet> map = getMap(httpCommand);
        if (map != null) map.put(uri, s);
    }

    /** {@inheritDoc} */
    @Override
    public void removeServlet(String httpCommand, String uri) {
        ConcurrentHashMap<String, Servlet> map = getMap(httpCommand);
        if (map != null) map.remove(uri);
    }

    /**
     * Finds the servlet whose registered URI is the longest prefix of the
     * given request URI (after stripping the query string).
     *
     * @param map        the method-specific servlet map
     * @param requestUri the full request URI including query string
     * @return the best-matching servlet, or {@code null} if none matches
     */
    private Servlet findServlet(ConcurrentHashMap<String, Servlet> map, String requestUri) {
        if (map == null) return null;
        String path = requestUri.split("\\?")[0];
        String bestMatch = null;
        for (String registeredUri : map.keySet()) {
            if (path.startsWith(registeredUri)) {
                if (bestMatch == null || registeredUri.length() > bestMatch.length())
                    bestMatch = registeredUri;
            }
        }
        return bestMatch != null ? map.get(bestMatch) : null;
    }

    /**
     * Starts the server: creates the thread pool, binds the server socket, and
     * begins accepting connections on this thread.
     */
    @Override
    public void start() {
        running = true;
        threadPool = Executors.newFixedThreadPool(nThreads);
        super.start();
    }

    /**
     * Stops the server by setting the running flag, closing the server socket,
     * and shutting down the thread pool (waits up to 5 seconds for tasks to finish).
     */
    @Override
    public void close() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException ignored) {}
        if (threadPool != null) {
            threadPool.shutdown();
            try { threadPool.awaitTermination(5, TimeUnit.SECONDS); }
            catch (InterruptedException ignored) {}
        }
    }

    /**
     * Main accept loop: waits for client connections and submits each one to
     * the thread pool for processing.
     */
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000);
            while (running) {
                try {
                    final Socket clientSocket = serverSocket.accept();
                    threadPool.submit(() -> handleClient(clientSocket));
                } catch (SocketTimeoutException e) {
                    // normal — loop and re-check running flag
                }
            }
        } catch (IOException e) {
            if (running) e.printStackTrace();
        }
    }

    /**
     * Parses one HTTP request from a client socket, dispatches it to the
     * appropriate servlet, and ensures the socket is closed afterwards.
     *
     * @param clientSocket the accepted client connection
     */
    private void handleClient(Socket clientSocket) {
        BufferedReader reader = null;
        OutputStream out = null;
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out    = clientSocket.getOutputStream();

            RequestInfo ri = RequestParser.parseRequest(reader);
            if (ri == null) return;

            ConcurrentHashMap<String, Servlet> map = getMap(ri.getHttpCommand());
            Servlet servlet = findServlet(map, ri.getUri());

            if (servlet != null) {
                servlet.handle(ri, out);
            } else {
                out.write(("HTTP/1.1 404 Not Found\r\n\r\nNo servlet found for: " + ri.getUri()).getBytes());
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { if (reader != null) reader.close(); } catch (IOException ignored) {}
            try { if (out    != null) out.close();    } catch (IOException ignored) {}
            try { clientSocket.close(); }              catch (IOException ignored) {}
        }
    }
}
