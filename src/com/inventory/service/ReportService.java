package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;

import java.util.List;

public class ReportService {

    private ProductDAO dao = new ProductDAO();

    public void inventoryReport() {

        List<Product> products = dao.getAllProducts();

        System.out.println("\n---- INVENTORY REPORT ----");
        for (Product p : products) {
            System.out.println(
                p.getProductId() + " | " +
                p.getName() + " | Qty: " +
                p.getQuantity() + " | Expiry: " +
                p.getExpiryDate()
            );
        }
    }
}
