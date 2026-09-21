package com.flooring.mastery.service;

import com.flooring.mastery.dao.ProductDao;
import com.flooring.mastery.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// A fake ProductDao for service tests. It only knows one product: Tile (3.50 / 4.15).
public class ProductDaoStubImpl implements ProductDao {

    private final Product tile = new Product("Tile", new BigDecimal("3.50"), new BigDecimal("4.15"));

    @Override
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        products.add(tile);
        return products;
    }

    // ignores upper/lower case like the real DAO; any other product is "not found" (null)
    @Override
    public Product getProduct(String productType) {
        if (tile.getProductType().equalsIgnoreCase(productType)) {
            return tile;
        }
        return null;
    }
}
