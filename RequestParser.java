package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {

    private static boolean isBlankOrNull(String s) {
        return s == null || s.trim().isEmpty();
    }

    // Parse key=value&key=value style string into the given map
    private static void parseParams(String paramString, Map<String, String> parameters) {
        if (paramString == null || paramString.trim().isEmpty()) return;
        for (String param : paramString.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2) parameters.put(kv[0].trim(), kv[1].trim());
            else if (kv.length == 1 && !kv[0].trim().isEmpty()) parameters.put(kv[0].trim(), "");
        }
    }

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        // --- Request line ---
        String requestLine = reader.readLine();
        if (isBlankOrNull(requestLine)) return null;

        String[] requestParts = requestLine.trim().split(" ");
        String httpCommand = requestParts[0];           // keep original case e.g. "GET"
        String fullUri = requestParts.length > 1 ? requestParts[1] : "/";

        // Split URI into path and query string
        String[] uriAndQuery = fullUri.split("\\?", 2);
        String path = uriAndQuery[0];
        String queryString = uriAndQuery.length > 1 ? uriAndQuery[1] : "";

        // URI segments (non-empty parts of path)
        String[] rawSegments = path.split("/");
        int nonEmptyCount = 0;
        for (String s : rawSegments) if (!s.isEmpty()) nonEmptyCount++;
        String[] segments = new String[nonEmptyCount];
        int idx = 0;
        for (String s : rawSegments) if (!s.isEmpty()) segments[idx++] = s;

        // Parameters from query string
        Map<String, String> parameters = new HashMap<String, String>();
        parseParams(queryString, parameters);

        // --- Headers ---
        int contentLength = 0;
        String line;
        while ((line = reader.readLine()) != null && !isBlankOrNull(line)) {
            String lower = line.toLowerCase();
            if (lower.startsWith("content-length:")) {
                try {
                    contentLength = Integer.parseInt(line.split(":", 2)[1].trim());
                } catch (NumberFormatException ignored) {}
            }
        }
        // line is now null or blank — headers done

        // --- Body ---
        byte[] content = new byte[0];

        if (reader.ready()) {
            // Peek at next non-blank section
            StringBuilder section = new StringBuilder();
            while (reader.ready()) {
                reader.mark(4096);
                String bodyLine = reader.readLine();
                if (isBlankOrNull(bodyLine)) break;
                section.append(bodyLine).append("\n");
            }

            String sectionStr = section.toString().trim();

            if (!sectionStr.isEmpty()) {
                // Is this a "filename=..." style metadata line or actual content/params?
                if (sectionStr.startsWith("filename=") || sectionStr.startsWith("name=")) {
                    // extra metadata section — skip it, read the real content next
                    if (reader.ready()) {
                        StringBuilder contentBuilder = new StringBuilder();
                        while (reader.ready()) {
                            String contentLine = reader.readLine();
                            if (isBlankOrNull(contentLine)) break;
                            contentBuilder.append(contentLine).append("\n");
                        }
                        content = contentBuilder.toString().getBytes();
                    }
                } else if (sectionStr.contains("=") && !sectionStr.contains(" ")) {
                    // Looks like key=value pairs (POST body params) — parse into parameters
                    parseParams(sectionStr, parameters);
                } else {
                    // Plain content
                    content = (sectionStr + "\n").getBytes();
                }
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
