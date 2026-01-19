package com.myapi;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreService {
    private final Gson gson = new Gson();

    // GET Stores
    public String getStores(Connection conn, Map<String, String> params) throws SQLException {
        String sql = "SELECT * FROM store";
        boolean hasId = params.containsKey("id");
        
        if (hasId) sql += " WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (hasId) stmt.setInt(1, Integer.parseInt(params.get("id")));
            return resultSetToJson(stmt.executeQuery());
        }
    }

    // POST Store
    public String createStore(Connection conn, String jsonBody) throws SQLException {
        JsonObject json = JsonParser.parseString(jsonBody).getAsJsonObject();
        String sql = "INSERT INTO store (store_name, lottery_id, latitude, longitude, place_name, seller_name) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, getJsonStr(json, "store_name"));
            stmt.setString(2, getJsonStr(json, "lottery_id"));
            stmt.setString(3, getJsonStr(json, "latitude"));
            stmt.setString(4, getJsonStr(json, "longitude"));
            stmt.setString(5, getJsonStr(json, "place_name"));
            stmt.setString(6, getJsonStr(json, "seller_name"));

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return gson.toJson(Map.of("message", "Store inserted", "id", keys.getLong(1)));
                }
            }
        }
        throw new SQLException("Insert failed");
    }

    // PATCH Store
    public String updateStore(Connection conn, Map<String, String> params, String jsonBody) throws SQLException {
        if (!params.containsKey("id")) throw new SQLException("Store ID required");
        int id = Integer.parseInt(params.get("id"));
        
        JsonObject json = JsonParser.parseString(jsonBody).getAsJsonObject();
        String[] allowed = {"store_name", "lottery_id", "latitude", "longitude", "place_name", "seller_name"};
        
        List<String> setClauses = new ArrayList<>();
        List<String> values = new ArrayList<>();

        for (String field : allowed) {
            if (json.has(field)) {
                setClauses.add(field + " = ?");
                values.add(json.get(field).getAsString());
            }
        }

        if (setClauses.isEmpty()) return gson.toJson(Map.of("error", "No valid fields"));

        String sql = "UPDATE store SET " + String.join(", ", setClauses) + " WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int i = 1;
            for (String val : values) stmt.setString(i++, val);
            stmt.setInt(i, id);
            stmt.executeUpdate();
            return gson.toJson(Map.of("message", "Store updated"));
        }
    }

    // Helpers
    private String resultSetToJson(ResultSet rs) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            list.add(row);
        }
        return gson.toJson(list);
    }

    private String getJsonStr(JsonObject json, String key) {
        return (json.has(key) && !json.get(key).isJsonNull()) ? json.get(key).getAsString() : "";
    }
}