package com.flooring.mastery.dao;

import com.flooring.mastery.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Stateful tests for ProductDaoFileImpl: they use a real file in TestData/, never the real Data/ folder.
public class ProductDaoFileImplTest {

    private static final String TEST_FOLDER = "TestData";
    private static final String TEST_FILE = "TestData/Products.txt";

    private ProductDao productDao;

    // runs before EVERY test: writes the same known file again, so tests can't affect each other
    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Paths.get(TEST_FOLDER));

        String content = "ProductType,CostPerSquareFoot,LaborCostPerSquareFoot\n"
                + "Tile,3.50,4.15\n"
                + "Wood,5.15,4.75\n";
        Files.writeString(Paths.get(TEST_FILE), content); // overwrites any old version

        productDao = new ProductDaoFileImpl(TEST_FILE);
    }

    @Test
    void getAllProductsReturnsBothProducts() throws Exception {
        List<Product> products = productDao.getAllProducts();

        assertEquals(2, products.size());
    }

    @Test
    void getProductIgnoresCase() throws Exception {
        Product product = productDao.getProduct("tILe");

        assertNotNull(product);
        assertEquals(new Product("Tile", new BigDecimal("3.50"), new BigDecimal("4.15")), product);
    }

    @Test
    void getProductReturnsNullWhenNotFound() throws Exception {
        assertNull(productDao.getProduct("Marble"));
    }

    // proves "no code change needed": the DAO re-reads the file on every call
    @Test
    void productAddedToFileIsFound() throws Exception {
        assertNull(productDao.getProduct("Carpet"));

        Files.writeString(Path.of(TEST_FILE), "Carpet,2.25,2.10\n", StandardOpenOption.APPEND);

        Product product = productDao.getProduct("Carpet");
        assertNotNull(product);
        assertEquals(new BigDecimal("2.25"), product.getCostPerSquareFoot());
        assertEquals(3, productDao.getAllProducts().size());
    }

    @Test
    void missingFileThrowsPersistenceException() {
        ProductDao daoWithMissingFile = new ProductDaoFileImpl("TestData/DoesNotExist.txt");

        assertThrows(FlooringPersistenceException.class, () -> daoWithMissingFile.getAllProducts());
    }

    @Test
    void badNumberInFileThrowsPersistenceException() throws Exception {
        Files.writeString(Path.of(TEST_FILE), "Marble,abc,4.00\n", StandardOpenOption.APPEND);

        assertThrows(FlooringPersistenceException.class, () -> productDao.getAllProducts());
    }
}
