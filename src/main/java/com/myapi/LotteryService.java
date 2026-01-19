package com.myapi;

import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LotteryService {
    private final Gson gson = new Gson();

    public String getLotteries(Connection conn, Map<String, String> params) throws SQLException {
        String expiryCondition = "(stock > 0) AND (lottery_date > CURDATE() OR (lottery_date = CURDATE() AND draw_time > CURTIME()))";
        String sql = "SELECT * FROM lotteries WHERE " + expiryCondition;

        // Determine if we need to filter by specific ID
        boolean hasId = params.containsKey("id");
        boolean hasSellerId = params.containsKey("seller_id");

        if (hasId) {
            sql += " AND id = ?";
        } else if (hasSellerId) {
            sql += " AND seller_id = ?";
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (hasId) {
                stmt.setInt(1, Integer.parseInt(params.get("id")));
            } else if (hasSellerId) {
                stmt.setInt(1, Integer.parseInt(params.get("seller_id")));
            }
            return resultSetToJson(stmt.executeQuery());
        }
    }

    private String resultSetToJson(ResultSet rs) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        var meta = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            list.add(row);
        }
        return gson.toJson(list);
    }
}