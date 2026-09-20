package com.flooring.mastery.dao;

import com.flooring.mastery.model.Product;

import java.util.List;

// The service layer only knows this interface, so the storage can be swapped (e.g. a stub in tests).
public interface ProductDao {

    // returns every product in the data source
    List<Product> getAllProducts() throws FlooringPersistenceException;

    // returns the product with this type (ignoring upper/lower case), or null if there is none
    Product getProduct(String productType) throws FlooringPersistenceException;
}
