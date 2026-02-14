package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;
import java.time.LocalDate;
import java.util.List;

public class ExpiryAlertService {

    private static final int ALERT_DAYS = 7;
    private ProductDAO dao = new ProductDAO();

    public void checkExpiry() {

        List<Product> products = dao.getAllProducts();
        LocalDate today = LocalDate.now();

        for (Product p : products) {
            LocalDate expiry = p.getExpiryDate();

            // Skip products without expiry date
            if (expiry == null) {
                continue;
            }

            if (expiry.isBefore(today)) {
                System.out.println("❌ EXPIRED PRODUCT: " + p.getName());
            } else if (expiry.minusDays(ALERT_DAYS).isBefore(today)) {
                System.out.println("⚠ EXPIRY SOON: "
                        + p.getName() + " (Expiry: " + expiry + ")");
            }
        }
    }
    
    public List<Product> getExpiringProducts() {
        return dao.getNearExpiryProducts(ALERT_DAYS);
    }
}

