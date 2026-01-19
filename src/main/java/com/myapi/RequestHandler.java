package com.myapi;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class RequestHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final LotteryService lotteryService = new LotteryService();
    private final StoreService storeService = new StoreService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");

        String method = exchange.getRequestMethod();
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQuery(query);
        
        String response = "";
        int statusCode = 200;

        try (Connection conn = Database.getConnection()) {
            
            // --- ROUTING LOGIC ---
            
            if ("GET".equalsIgnoreCase(method)) {
                if ("lotteries".equals(params.get("type"))) {
                    response = lotteryService.getLotteries(conn, params);
                } else {
                    response = storeService.getStores(conn, params);
                }
            } 
            else if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(exchange);
                response = storeService.createStore(conn, body);
            } 
            else if ("PATCH".equalsIgnoreCase(method)) {
                String body = readBody(exchange);
                response = storeService.updateStore(conn, params, body);
            } 
            else {
                statusCode = 405;
                response = gson.toJson(Map.of("error", "Method not allowed"));
            }

        } catch (Exception e) {
            statusCode = 500;
            e.printStackTrace(); // Log to console
            response = gson.toJson(Map.of("error", e.getMessage()));
        }

        // Send Response
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // -- Helpers --
    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) map.put(pair[0], pair[1]);
        }
        return map;
    }

    private String readBody(HttpExchange exchange) {
        try (Scanner s = new Scanner(exchange.getRequestBody(), "UTF-8").useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }
}