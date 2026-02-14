package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.SaleDAO;
import com.inventory.model.Product;
import com.inventory.model.Sale;
import com.inventory.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class SaleService {

    private final SaleDAO saleDAO = new SaleDAO();
    private final ProductDAO productDAO = new ProductDAO();

    public boolean processSale(int productId, int quantity, int userId) {
        return processSale(productId, quantity, userId, -1);
    }

    public boolean processSale(int productId, int quantity, int userId, double billedTotalPrice) {
        if (quantity <= 0) {
            System.out.println("Invalid quantity!");
            return false;
        }

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            Product product = productDAO.getProductById(con, productId);
            if (product == null) {
                con.rollback();
                System.out.println("Product not found!");
                return false;
            }

            if (product.getQuantity() < quantity) {
                con.rollback();
                System.out.println("Insufficient stock! Available: " + product.getQuantity());
                return false;
            }

            double baseTotalPrice = product.getPrice() * quantity;
            double finalTotalPrice = billedTotalPrice < 0 ? baseTotalPrice : billedTotalPrice;

            if (finalTotalPrice < 0 || finalTotalPrice > baseTotalPrice) {
                con.rollback();
                System.out.println("Invalid billed total price: " + finalTotalPrice);
                return false;
            }

            Sale sale = new Sale();
            sale.setProductId(productId);
            sale.setQuantitySold(quantity);
            sale.setTotalPrice(finalTotalPrice);
            sale.setUserId(userId);

            boolean saleAdded = saleDAO.addSale(con, sale);
            if (!saleAdded) {
                con.rollback();
                return false;
            }

            int newQuantity = product.getQuantity() - quantity;
            boolean stockUpdated = productDAO.updateStock(con, productId, newQuantity);
            if (!stockUpdated) {
                con.rollback();
                return false;
            }

            con.commit();
            return true;
        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    rollbackError.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException closeError) {
                    closeError.printStackTrace();
                }
            }
        }
    }

    public List<Sale> getAllSales() {
        return saleDAO.getAllSales();
    }

    public List<Sale> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return saleDAO.getSalesByDateRange(startDate, endDate);
    }

    public double getTotalSalesAmount() {
        return saleDAO.getTotalSalesAmount();
    }
}
