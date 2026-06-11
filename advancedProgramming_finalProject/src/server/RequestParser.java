package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class that parses a raw HTTP/1.1 request from a {@link BufferedReader}
 * into a structured {@link RequestInfo} object.
 *
 * <p>Supports GET, POST, and DELETE methods.  Query-string parameters are
 * extracted from the URI.  The body is read using {@code Content-Length} when
 * available (preserving blank lines inside multipart uploads), or with a
 * two-section heuristic for requests without {@code Content-Length}.</p>
 *
 * <p>This class is stateless — all methods are static.</p>
 */
public class RequestParser {

    private static boolean isBlankOrNull(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Parses {@code key=value} pairs from a query string and adds them to {@code parameters}.
     *
     * @param paramString the raw query string (e.g. {@code "topic=A&message=5"})
     * @param parameters  the map to populate
     */
    private static void parseParams(String paramString, Map<String, String> parameters) {
        if (isBlankOrNull(paramString)) return;
        for (String param : paramString.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2) parameters.put(kv[0].trim(), kv[1].trim());
            else if (kv.length == 1 && !kv[0].trim().isEmpty()) parameters.put(kv[0].trim(), "");
        }
    }

    /**
     * Reads lines until a blank line or no more ready data, joining them with {@code '\n'}.
     * Used as a fallback body reader when {@code Content-Length} is absent.
     *
     * @param reader the source of data
     * @return the joined section, or {@code null} if nothing was read
     * @throws IOException if reading fails
     */
    private static String readSection(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (reader.ready()) {
            reader.mark(8192);
            String line = reader.readLine();
            if (isBlankOrNull(line)) break;
            if (sb.length() > 0) sb.append("\n");
            sb.append(line.trim());
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * Parses one complete HTTP request from {@code reader}.
     *
     * <p>Body reading strategy:</p>
     * <ul>
     *   <li>If {@code Content-Length} is present and positive, exactly that many
     *       characters are read — blank lines in multipart bodies are preserved.</li>
     *   <li>Otherwise the two-section heuristic is used.</li>
     * </ul>
     *
     * @param reader positioned at the start of an HTTP request
     * @return the parsed {@link RequestInfo}, or {@code null} if the request line is blank
     * @throws IOException if reading fails
     */
    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
        if (isBlankOrNull(requestLine)) return null;

        String[] requestParts = requestLine.trim().split(" ");
        String httpCommand = requestParts[0];
        String fullUri     = requestParts.length > 1 ? requestParts[1] : "/";

        String[] uriAndQuery = fullUri.split("\\?", 2);
        String path        = uriAndQuery[0];
        String queryString = uriAndQuery.length > 1 ? uriAndQuery[1] : "";

        String[] rawSegments = path.split("/");
        int nonEmptyCount = 0;
        for (String s : rawSegments) if (!s.isEmpty()) nonEmptyCount++;
        String[] segments = new String[nonEmptyCount];
        int idx = 0;
        for (String s : rawSegments) if (!s.isEmpty()) segments[idx++] = s;

        Map<String, String> parameters = new HashMap<>();
        parseParams(queryString, parameters);

        int contentLength = 0;
        String line;
        while ((line = reader.readLine()) != null && !isBlankOrNull(line)) {
            if (line.toLowerCase().startsWith("content-length:")) {
                try {
                    contentLength = Integer.parseInt(line.split(":", 2)[1].trim());
                } catch (NumberFormatException ignored) {}
            }
        }

        byte[] content = new byte[0];

        if (contentLength > 0) {
            // Read the full body using Content-Length so blank lines inside
            // the body (e.g. multi-block config files) are preserved.
            char[] buf = new char[contentLength];
            int totalRead = 0;
            while (totalRead < contentLength) {
                int r = reader.read(buf, totalRead, contentLength - totalRead);
                if (r < 0) break;
                totalRead += r;
            }
            content = new String(buf, 0, totalRead).getBytes("UTF-8");
        } else {
            // No Content-Length — fall back to the two-section heuristic.
            String sectionA = readSection(reader);
            if (sectionA != null) {
                String sectionB = readSection(reader);
                if (sectionB != null && !sectionB.isEmpty()) {
                    content = (sectionB + "\n").getBytes();
                } else {
                    content = (sectionA + "\n").getBytes();
                }
            }
        }

        return new RequestInfo(httpCommand, fullUri, segments, parameters, content);
    }

    // -------------------------------------------------------------------------

    /**
     * Immutable value object holding the parsed components of one HTTP request.
     */
    public static class RequestInfo {

        private final String httpCommand;
        private final String uri;
        private final String[] uriSegments;
        private final Map<String, String> parameters;
        private final byte[] content;

        /**
         * @param httpCommand the HTTP method (e.g. {@code "GET"})
         * @param uri         the full URI including query string
         * @param uriSegments the non-empty path segments
         * @param parameters  query-string parameters
         * @param content     the raw request body bytes
         */
        public RequestInfo(String httpCommand, String uri, String[] uriSegments,
                           Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri         = uri;
            this.uriSegments = uriSegments;
            this.parameters  = parameters;
            this.content     = content;
        }

        /** @return the HTTP method string */
        public String getHttpCommand()             { return httpCommand; }

        /** @return the full request URI */
        public String getUri()                     { return uri; }

        /** @return non-empty path segments (e.g. {@code ["app","index.html"]}) */
        public String[] getUriSegments()           { return uriSegments; }

        /** @return query-string parameters as a name→value map */
        public Map<String, String> getParameters() { return parameters; }

        /** @return raw body bytes; empty array if no body */
        public byte[] getContent()                 { return content; }

        @Override
        public String toString() {
            return "RequestInfo{httpCommand='" + httpCommand + "', uri='" + uri
                    + "', uriSegments=" + Arrays.toString(uriSegments)
                    + ", parameters=" + parameters
                    + ", content='" + new String(content) + "'}";
        }
    }
}
