package com.flooring.mastery.dao;

import com.flooring.mastery.model.Tax;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Reads state tax rates from a text file (default: Data/Taxes.txt).
// The file is read again on every call, so a state added to the file works without a code change.
@Repository
public class TaxDaoFileImpl implements TaxDao {

    private static final String DEFAULT_TAX_FILE = "Data/Taxes.txt";
    private static final String DELIMITER = ",";

    private final String taxFile;

    // Spring uses this one: it points at the real file
    @Autowired
    public TaxDaoFileImpl() {
        this(DEFAULT_TAX_FILE);
    }

    // tests use this one: they point at a file in TestData/
    public TaxDaoFileImpl(String taxFile) {
        this.taxFile = taxFile;
    }

    // reads the whole file and returns one Tax per line
    @Override
    public List<Tax> getAllTaxes() throws FlooringPersistenceException {
        List<Tax> taxes = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(taxFile))) {
            reader.readLine(); // skip the header row

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue; // ignore empty lines
                }
                taxes.add(unmarshallTax(line));
            }
        } catch (IOException e) {
            throw new FlooringPersistenceException("Could not load tax data.", e);
        }

        return taxes;
    }

    // finds one tax by state abbreviation, ignoring case ("ca" matches "CA"); null if not found
    @Override
    public Tax getTax(String stateAbbreviation) throws FlooringPersistenceException {
        return getAllTaxes().stream()
                .filter(tax -> tax.getStateAbbreviation().equalsIgnoreCase(stateAbbreviation))
                .findFirst()
                .orElse(null);
    }

    // turns one text line like "TX,Texas,4.45" into a Tax object
    private Tax unmarshallTax(String line) throws FlooringPersistenceException {
        String[] tokens = line.split(DELIMITER);

        try {
            String stateAbbreviation = tokens[0].trim();
            String stateName = tokens[1].trim();
            BigDecimal taxRate = new BigDecimal(tokens[2].trim());
            return new Tax(stateAbbreviation, stateName, taxRate);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // bad number, or a line with fewer than 3 columns
            throw new FlooringPersistenceException("Bad tax data in line: " + line, e);
        }
    }
}
