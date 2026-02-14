package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;

import java.util.List;

public class AlertService {

    private static final int LOW_STOCK_LIMIT = 10;
    private ProductDAO dao = new ProductDAO();

    public void checkLowStock() {

        List<Product> products = dao.getAllProducts();

        for (Product p : products) {
            if (p.getQuantity() <= LOW_STOCK_LIMIT) {
                System.out.println("⚠ LOW STOCK ALERT: "
                        + p.getName() + " (Qty: " + p.getQuantity() + ")");
            }
        }
    }
}
