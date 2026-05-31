package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {

    private static boolean isBlankOrNull(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static void parseParams(String paramString, Map<String, String> parameters) {
        if (isBlankOrNull(paramString)) return;
        for (String param : paramString.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2) parameters.put(kv[0].trim(), kv[1].trim());
            else if (kv.length == 1 && !kv[0].trim().isEmpty()) parameters.put(kv[0].trim(), "");
        }
    }

    /**
     * Read lines until a blank line or until the reader has no more ready data.
     * Returns the joined content, or null if nothing was read.
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

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        // --- Request line ---
        String requestLine = reader.readLine();
        if (isBlankOrNull(requestLine)) return null;

        String[] requestParts = requestLine.trim().split(" ");
        String httpCommand = requestParts[0];
        String fullUri     = requestParts.length > 1 ? requestParts[1] : "/";

        // URI path and query string
        String[] uriAndQuery = fullUri.split("\\?", 2);
        String path        = uriAndQuery[0];
        String queryString = uriAndQuery.length > 1 ? uriAndQuery[1] : "";

        // URI segments
        String[] rawSegments = path.split("/");
        int nonEmptyCount = 0;
        for (String s : rawSegments) if (!s.isEmpty()) nonEmptyCount++;
        String[] segments = new String[nonEmptyCount];
        int idx = 0;
        for (String s : rawSegments) if (!s.isEmpty()) segments[idx++] = s;

        // Parameters from URI query string ONLY (per spec)
        Map<String, String> parameters = new HashMap<String, String>();
        parseParams(queryString, parameters);

        // --- Headers: read until blank line ---
        int contentLength = 0;
        String line;
        while ((line = reader.readLine()) != null && !isBlankOrNull(line)) {
            if (line.toLowerCase().startsWith("content-length:")) {
                try {
                    contentLength = Integer.parseInt(line.split(":", 2)[1].trim());
                } catch (NumberFormatException ignored) {}
            }
        }
        // blank line consumed — headers done

        // --- Body ---
        // Per PDF structure:
        //   [Section A: optional metadata e.g. filename="hello.txt"]
        //   [blank line]
        //   [Section B: actual content]
        //   [blank line]
        //
        // If only one section, it is the content.
        byte[] content = new byte[0];

        String sectionA = readSection(reader);
        if (sectionA != null) {
            String sectionB = readSection(reader);
            if (sectionB != null && !sectionB.isEmpty()) {
                // A=metadata, B=content
                content = (sectionB + "\n").getBytes();
            } else {
                // Only A — it is the content
                content = (sectionA + "\n").getBytes();
            }
        }

        return new RequestInfo(httpCommand, fullUri, segments, parameters, content);
    }

    // -------------------------------------------------------------------------
    public static class RequestInfo {
        private final String httpCommand;
        private final String uri;
        private final String[] uriSegments;
        private final Map<String, String> parameters;
        private final byte[] content;

        public RequestInfo(String httpCommand, String uri, String[] uriSegments,
                           Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri         = uri;
            this.uriSegments = uriSegments;
            this.parameters  = parameters;
            this.content     = content;
        }

        public String getHttpCommand()             { return httpCommand; }
        public String getUri()                     { return uri; }
        public String[] getUriSegments()           { return uriSegments; }
        public Map<String, String> getParameters() { return parameters; }
        public byte[] getContent()                 { return content; }

        @Override
        public String toString() {
            return "RequestInfo{" +
                    "httpCommand='" + httpCommand + '\'' +
                    ", uri='" + uri + '\'' +
                    ", uriSegments=" + Arrays.toString(uriSegments) +
                    ", parameters=" + parameters +
                    ", content='" + new String(content) + '\'' +
                    '}';
        }
    }
}
