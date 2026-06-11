package servlets;

import server.RequestParser.RequestInfo;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Contract for a request handler registered with {@link server.MyHTTPServer}.
 *
 * <p>Each servlet is mapped to an HTTP method and a URI prefix.  When an
 * incoming request matches, the server calls {@link #handle(RequestInfo, OutputStream)}
 * on the appropriate servlet instance.</p>
 *
 * <p>Servlets must be thread-safe: the server may invoke {@code handle} from
 * different worker threads concurrently.</p>
 */
public interface Servlet {

    /**
     * Handles a single HTTP request and writes the full HTTP response
     * (status line, headers, and body) to {@code toClient}.
     *
     * @param ri       parsed request data (method, URI, parameters, body)
     * @param toClient output stream connected to the client socket
     * @throws IOException if writing to the client fails
     */
    void handle(RequestInfo ri, OutputStream toClient) throws IOException;

    /**
     * Releases any resources held by this servlet (e.g. active agents, open files).
     * Called when the server is shut down.
     *
     * @throws IOException if cleanup fails
     */
    void close() throws IOException;
}
