package com.flooring.mastery.dao;

import com.flooring.mastery.model.Tax;
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

// Stateful tests for TaxDaoFileImpl: they use a real file in TestData/, never the real Data/ folder.
public class TaxDaoFileImplTest {

    private static final String TEST_FOLDER = "TestData";
    private static final String TEST_FILE = "TestData/Taxes.txt";

    private TaxDao taxDao;

    // runs before EVERY test: writes the same known file again, so tests can't affect each other
    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Paths.get(TEST_FOLDER));

        String content = "State,StateName,TaxRate\n"
                + "CA,California,25.00\n"
                + "TX,Texas,4.45\n";
        Files.writeString(Paths.get(TEST_FILE), content); // overwrites any old version

        taxDao = new TaxDaoFileImpl(TEST_FILE);
    }

    @Test
    void getAllTaxesReturnsBothStates() throws Exception {
        List<Tax> taxes = taxDao.getAllTaxes();

        assertEquals(2, taxes.size());
    }

    @Test
    void getTaxIgnoresCase() throws Exception {
        Tax tax = taxDao.getTax("ca");

        assertNotNull(tax);
        assertEquals(new Tax("CA", "California", new BigDecimal("25.00")), tax);
    }

    @Test
    void getTaxReturnsNullWhenStateNotSold() throws Exception {
        assertNull(taxDao.getTax("ZZ"));
    }

    // proves "no code change needed": the DAO re-reads the file on every call
    @Test
    void stateAddedToFileIsFound() throws Exception {
        assertNull(taxDao.getTax("WA"));

        Files.writeString(Path.of(TEST_FILE), "WA,Washington,9.25\n", StandardOpenOption.APPEND);

        Tax tax = taxDao.getTax("WA");
        assertNotNull(tax);
        assertEquals(new BigDecimal("9.25"), tax.getTaxRate());
        assertEquals(3, taxDao.getAllTaxes().size());
    }

    @Test
    void missingFileThrowsPersistenceException() {
        TaxDao daoWithMissingFile = new TaxDaoFileImpl("TestData/DoesNotExist.txt");

        assertThrows(FlooringPersistenceException.class, () -> daoWithMissingFile.getAllTaxes());
    }

    @Test
    void badNumberInFileThrowsPersistenceException() throws Exception {
        Files.writeString(Path.of(TEST_FILE), "NY,New York,abc\n", StandardOpenOption.APPEND);

        assertThrows(FlooringPersistenceException.class, () -> taxDao.getAllTaxes());
    }
}
