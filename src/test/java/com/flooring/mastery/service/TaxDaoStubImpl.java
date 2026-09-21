package com.flooring.mastery.service;

import com.flooring.mastery.dao.TaxDao;
import com.flooring.mastery.model.Tax;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// A fake TaxDao for service tests. It only knows one state: CA (25.00).
public class TaxDaoStubImpl implements TaxDao {

    private final Tax california = new Tax("CA", "California", new BigDecimal("25.00"));

    @Override
    public List<Tax> getAllTaxes() {
        List<Tax> taxes = new ArrayList<>();
        taxes.add(california);
        return taxes;
    }

    // ignores upper/lower case like the real DAO; any other state is "not found" (null)
    @Override
    public Tax getTax(String stateAbbreviation) {
        if (california.getStateAbbreviation().equalsIgnoreCase(stateAbbreviation)) {
            return california;
        }
        return null;
    }
}
