package com.myapi;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8000;
        
        // 1. Create Server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // 2. Register the Handler (Router)
        server.createContext("/api", new RequestHandler());

        // 3. ENABLE MULTITHREADING
        // newCachedThreadPool creates threads as needed and reuses them.
        server.setExecutor(Executors.newCachedThreadPool());

        System.out.println("✅ Server started on port " + port);
        server.start();
    }
}