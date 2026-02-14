package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;
import java.util.List;

public class ProductService {

    private ProductDAO dao = new ProductDAO();

    public void addProduct(Product p) {
        dao.addProduct(p);
    }

    public List<Product> getProducts() {
        return dao.getAllProducts();
    }

    public boolean updateStock(int productId, int qty) {
        return dao.updateStock(productId, qty);
    }

    public void updateProduct(Product p) {
        dao.updateProduct(p);
    }

    public boolean deleteProduct(int productId) {
        return dao.deleteProduct(productId);
    }

    public Product getProductById(int productId) {
        return dao.getProductById(productId);
    }
}
