package com.myapi;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException {
        // 1. Get PORT from Render environment, or default to 8080 if running locally
        String envPort = System.getenv("PORT");
        int port = envPort != null ? Integer.parseInt(envPort) : 8080;
        
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api", new RequestHandler());
        server.setExecutor(Executors.newCachedThreadPool());

        System.out.println("✅ Server started on Port " + port);
        server.start();
    }
}