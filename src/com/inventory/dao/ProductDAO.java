package com.inventory.dao;

import com.inventory.model.Product;
import com.inventory.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // ---------------- ADD PRODUCT ----------------
    public void addProduct(Product p) {

        String sql = "INSERT INTO products(name, category, quantity, price, expiry_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setInt(3, p.getQuantity());
            ps.setDouble(4, p.getPrice());

            // Convert LocalDate → SQL Date
            if (p.getExpiryDate() != null) {
                ps.setDate(5, Date.valueOf(p.getExpiryDate()));
            } else {
                ps.setNull(5, Types.DATE);
            }

            ps.executeUpdate();
            System.out.println("Product added successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- GET ALL PRODUCTS ----------------
    public List<Product> getAllProducts() {

        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setCategory(rs.getString("category"));
                p.setQuantity(rs.getInt("quantity"));
                p.setPrice(rs.getDouble("price"));

                Date exp = rs.getDate("expiry_date");
                if (exp != null) {
                    p.setExpiryDate(exp.toLocalDate());
                }

                list.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ---------------- UPDATE STOCK ----------------
    public boolean updateStock(int productId, int qty) {
        try (Connection con = DBConnection.getConnection()) {
            return updateStock(con, productId, qty);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStock(Connection con, int productId, int qty) throws SQLException {

        String sql = "UPDATE products SET quantity = ? WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------------- NEAR EXPIRY PRODUCTS ----------------
    public List<Product> getNearExpiryProducts(int days) {

        List<Product> list = new ArrayList<>();

        String sql = """
            SELECT product_id, name, category, quantity, price, expiry_date
            FROM products
            WHERE expiry_date IS NOT NULL
            AND expiry_date <= CURDATE() + INTERVAL ? DAY
            ORDER BY expiry_date ASC
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, days);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setCategory(rs.getString("category"));
                p.setQuantity(rs.getInt("quantity"));
                p.setPrice(rs.getDouble("price"));
                p.setExpiryDate(rs.getDate("expiry_date").toLocalDate());

                list.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ---------------- UPDATE PRODUCT ----------------
    public void updateProduct(Product p) {

        String sql = "UPDATE products SET name = ?, category = ?, quantity = ?, price = ?, expiry_date = ? WHERE product_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setInt(3, p.getQuantity());
            ps.setDouble(4, p.getPrice());

            if (p.getExpiryDate() != null) {
                ps.setDate(5, Date.valueOf(p.getExpiryDate()));
            } else {
                ps.setNull(5, Types.DATE);
            }

            ps.setInt(6, p.getProductId());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- DELETE PRODUCT ----------------
    public boolean deleteProduct(int productId) {

        String sql = "DELETE FROM products WHERE product_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------- GET PRODUCT BY ID ----------------
    public Product getProductById(int productId) {
        try (Connection con = DBConnection.getConnection()) {
            return getProductById(con, productId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Product getProductById(Connection con, int productId) throws SQLException {

        String sql = "SELECT * FROM products WHERE product_id = ?";
        Product p = null;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setCategory(rs.getString("category"));
                p.setQuantity(rs.getInt("quantity"));
                p.setPrice(rs.getDouble("price"));

                Date exp = rs.getDate("expiry_date");
                if (exp != null) {
                    p.setExpiryDate(exp.toLocalDate());
                }
            }
        }
        return p;
    }

    // ---------------- SEARCH PRODUCTS ----------------
    public List<Product> searchProducts(String keyword) {

        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name LIKE ? OR category LIKE ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setCategory(rs.getString("category"));
                p.setQuantity(rs.getInt("quantity"));
                p.setPrice(rs.getDouble("price"));

                Date exp = rs.getDate("expiry_date");
                if (exp != null) {
                    p.setExpiryDate(exp.toLocalDate());
                }

                list.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ---------------- GET LOW STOCK PRODUCTS ----------------
    public List<Product> getLowStockProducts(int threshold) {

        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE quantity <= ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, threshold);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setCategory(rs.getString("category"));
                p.setQuantity(rs.getInt("quantity"));
                p.setPrice(rs.getDouble("price"));

                Date exp = rs.getDate("expiry_date");
                if (exp != null) {
                    p.setExpiryDate(exp.toLocalDate());
                }

                list.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
