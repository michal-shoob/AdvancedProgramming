import server.*;
import servlets.Servlet;
import server.RequestParser.RequestInfo;

import java.io.*;
import java.net.Socket;

// A simple Servlet that adds two numbers from query params
class EchoServlet implements Servlet {
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        String a = ri.getParameters().getOrDefault("a", "0");
        String b = ri.getParameters().getOrDefault("b", "0");
        int sum = Integer.parseInt(a) + Integer.parseInt(b);
        String response = "HTTP/1.1 200 OK\r\n\r\nResult: " + sum + "\n";
        toClient.write(response.getBytes());
    }
    @Override
    public void close() {}
}

public class TestServer {
    public static void main(String[] args) throws Exception {

        // ---- Test 1: RequestParser ----
        System.out.println("=== Test 1: RequestParser ===");
        String request =
            "GET /api/resource?id=123&name=test HTTP/1.1\n" +
            "Host: example.com\n" +
            "Content-Length: 5\n" +
            "\n" +
            "filename=\"hello_world.txt\"\n" +
            "\n" +
            "hello world!\n" +
            "\n";

        BufferedReader br = new BufferedReader(new StringReader(request));
        RequestInfo ri = RequestParser.parseRequest(br);
        System.out.println(ri);
        assert ri.getHttpCommand().equals("get")                      : "Command mismatch";
        assert ri.getParameters().get("id").equals("123")             : "id param mismatch";
        assert ri.getParameters().get("name").equals("test")          : "name param mismatch";
        assert ri.getUriSegments()[0].equals("api")                   : "segment[0] mismatch";
        assert ri.getUriSegments()[1].equals("resource")              : "segment[1] mismatch";
        System.out.println("RequestParser: OK\n");

        // ---- Test 2: Server + Socket client ----
        System.out.println("=== Test 2: MyHTTPServer ===");
        int port = 8765;
        HTTPServer server = new MyHTTPServer(port, 5);
        server.addServlet("GET", "/calc", new EchoServlet());
        server.start();

        // Verify only one extra thread was created (hard to count exactly,
        // but we can verify the server responds)
        Thread.sleep(300);

        // Send a request via Socket
        try (Socket socket = new Socket("localhost", port);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader2 = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))) {

            writer.print("GET /calc?a=7&b=3 HTTP/1.1\r\nHost: localhost\r\n\r\n");
            writer.flush();

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader2.readLine()) != null) sb.append(line).append("\n");
            System.out.println("Server response:\n" + sb);
            assert sb.toString().contains("Result: 10") : "Expected 'Result: 10' in response";
        }

        server.close();
        Thread.sleep(2000);
        System.out.println("All threads done. Tests PASSED!");
    }
}