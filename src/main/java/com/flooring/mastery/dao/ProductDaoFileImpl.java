package com.flooring.mastery.dao;

import com.flooring.mastery.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Reads products from a text file (default: Data/Products.txt).
// The file is read again on every call, so a product added to the file works without a code change.
@Repository
public class ProductDaoFileImpl implements ProductDao {

    private static final String DEFAULT_PRODUCT_FILE = "Data/Products.txt";
    private static final String DELIMITER = ",";

    private final String productFile;

    // Spring uses this one: it points at the real file
    @Autowired
    public ProductDaoFileImpl() {
        this(DEFAULT_PRODUCT_FILE);
    }

    // tests use this one: they point at a file in TestData/
    public ProductDaoFileImpl(String productFile) {
        this.productFile = productFile;
    }

    // reads the whole file and returns one Product per line
    @Override
    public List<Product> getAllProducts() throws FlooringPersistenceException {
        List<Product> products = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(productFile))) {
            reader.readLine(); // skip the header row

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue; // ignore empty lines
                }
                products.add(unmarshallProduct(line));
            }
        } catch (IOException e) {
            throw new FlooringPersistenceException("Could not load product data.", e);
        }

        return products;
    }

    // finds one product by type, ignoring case ("tile" matches "Tile"); null if not found
    @Override
    public Product getProduct(String productType) throws FlooringPersistenceException {
        return getAllProducts().stream()
                .filter(product -> product.getProductType().equalsIgnoreCase(productType))
                .findFirst()
                .orElse(null);
    }

    // turns one text line like "Tile,3.50,4.15" into a Product object
    private Product unmarshallProduct(String line) throws FlooringPersistenceException {
        String[] tokens = line.split(DELIMITER);

        try {
            String productType = tokens[0].trim();
            BigDecimal costPerSquareFoot = new BigDecimal(tokens[1].trim());
            BigDecimal laborCostPerSquareFoot = new BigDecimal(tokens[2].trim());
            return new Product(productType, costPerSquareFoot, laborCostPerSquareFoot);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // bad number, or a line with fewer than 3 columns
            throw new FlooringPersistenceException("Bad product data in line: " + line, e);
        }
    }
}
