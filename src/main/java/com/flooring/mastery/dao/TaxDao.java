package com.flooring.mastery.dao;

import com.flooring.mastery.model.Tax;

import java.util.List;

// The service layer only knows this interface, so the storage can be swapped (e.g. a stub in tests).
public interface TaxDao {

    // returns every state/tax rate in the data source
    List<Tax> getAllTaxes() throws FlooringPersistenceException;

    // returns the tax for this state abbreviation (ignoring upper/lower case), or null if there is none
    Tax getTax(String stateAbbreviation) throws FlooringPersistenceException;
}
