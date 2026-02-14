package com.inventory.dao;

import com.inventory.model.Sale;
import com.inventory.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    // ---------------- ADD SALE ----------------
    public boolean addSale(Sale sale) {
        try (Connection con = DBConnection.getConnection()) {
            return addSale(con, sale);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addSale(Connection con, Sale sale) throws SQLException {
        String sql = "INSERT INTO sales(product_id, quantity_sold, total_price, user_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sale.getProductId());
            ps.setInt(2, sale.getQuantitySold());
            ps.setDouble(3, sale.getTotalPrice());
            ps.setInt(4, sale.getUserId());

            ps.executeUpdate();
            return true;

        }
    }

    // ---------------- GET ALL SALES ----------------
    public List<Sale> getAllSales() {

        List<Sale> list = new ArrayList<>();
        String sql = """
            SELECT s.sale_id, s.product_id, s.quantity_sold, s.total_price, 
                   s.sale_date, s.user_id, p.name AS product_name
            FROM sales s
            JOIN products p ON s.product_id = p.product_id
            ORDER BY s.sale_date DESC
        """;

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Sale s = new Sale();
                s.setSaleId(rs.getInt("sale_id"));
                s.setProductId(rs.getInt("product_id"));
                s.setQuantitySold(rs.getInt("quantity_sold"));
                s.setTotalPrice(rs.getDouble("total_price"));
                s.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
                s.setUserId(rs.getInt("user_id"));
                s.setProductName(rs.getString("product_name"));

                list.add(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ---------------- GET SALES BY DATE RANGE ----------------
    public List<Sale> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {

        List<Sale> list = new ArrayList<>();
        String sql = """
            SELECT s.sale_id, s.product_id, s.quantity_sold, s.total_price, 
                   s.sale_date, s.user_id, p.name AS product_name
            FROM sales s
            JOIN products p ON s.product_id = p.product_id
            WHERE s.sale_date BETWEEN ? AND ?
            ORDER BY s.sale_date DESC
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(startDate));
            ps.setTimestamp(2, Timestamp.valueOf(endDate));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Sale s = new Sale();
                s.setSaleId(rs.getInt("sale_id"));
                s.setProductId(rs.getInt("product_id"));
                s.setQuantitySold(rs.getInt("quantity_sold"));
                s.setTotalPrice(rs.getDouble("total_price"));
                s.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
                s.setUserId(rs.getInt("user_id"));
                s.setProductName(rs.getString("product_name"));

                list.add(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ---------------- GET TOTAL SALES AMOUNT ----------------
    public double getTotalSalesAmount() {

        String sql = "SELECT COALESCE(SUM(total_price), 0) AS total FROM sales";
        double total = 0;

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                total = rs.getDouble("total");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return total;
    }
}
