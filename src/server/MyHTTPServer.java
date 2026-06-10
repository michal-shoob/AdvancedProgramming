package server;

import servlets.Servlet;
import server.RequestParser.RequestInfo;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class MyHTTPServer extends Thread implements HTTPServer {

    private final int port;
    private final int nThreads;

    private final ConcurrentHashMap<String, Servlet> getServlets    = new ConcurrentHashMap<String, Servlet>();
    private final ConcurrentHashMap<String, Servlet> postServlets   = new ConcurrentHashMap<String, Servlet>();
    private final ConcurrentHashMap<String, Servlet> deleteServlets = new ConcurrentHashMap<String, Servlet>();

    private volatile boolean running = false;
    private ExecutorService threadPool;
    private ServerSocket serverSocket;

    public MyHTTPServer(int port, int nThreads) {
        this.port     = port;
        this.nThreads = nThreads;
    }

    private ConcurrentHashMap<String, Servlet> getMap(String httpCommand) {
        String cmd = httpCommand.toLowerCase();
        if (cmd.equals("get"))    return getServlets;
        if (cmd.equals("post"))   return postServlets;
        if (cmd.equals("delete")) return deleteServlets;
        return null;
    }

    @Override
    public void addServlet(String httpCommand, String uri, Servlet s) {
        ConcurrentHashMap<String, Servlet> map = getMap(httpCommand);
        if (map != null) map.put(uri, s);
    }

    @Override
    public void removeServlet(String httpCommand, String uri) {
        ConcurrentHashMap<String, Servlet> map = getMap(httpCommand);
        if (map != null) map.remove(uri);
    }

    private Servlet findServlet(ConcurrentHashMap<String, Servlet> map, String requestUri) {
        if (map == null) return null;
        // Strip query string for matching
        String path = requestUri.split("\\?")[0];
        String bestMatch = null;
        for (String registeredUri : map.keySet()) {
            if (path.startsWith(registeredUri)) {
                if (bestMatch == null || registeredUri.length() > bestMatch.length()) {
                    bestMatch = registeredUri;
                }
            }
        }
        return bestMatch != null ? map.get(bestMatch) : null;
    }

    @Override
    public void start() {
        running = true;
        threadPool = Executors.newFixedThreadPool(nThreads);
        super.start();
    }

    @Override
    public void close() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed())
                serverSocket.close();
        } catch (IOException ignored) {}
        if (threadPool != null) {
            threadPool.shutdown();
            try { threadPool.awaitTermination(5, TimeUnit.SECONDS); }
            catch (InterruptedException ignored) {}
        }
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000);
            while (running) {
                try {
                    final Socket clientSocket = serverSocket.accept();
                    threadPool.submit(new Runnable() {
                        public void run() { handleClient(clientSocket); }
                    });
                } catch (SocketTimeoutException e) {
                    // loop and check running
                }
            }
        } catch (IOException e) {
            if (running) e.printStackTrace();
        }
    }

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
