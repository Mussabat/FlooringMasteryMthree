package com.flooring.mastery.model;

import java.math.BigDecimal;
import java.util.Objects;

// One line of Data/Taxes.txt: a state we sell in and its tax rate.
// taxRate is a whole-number percentage: 4.45 means 4.45%.
// Plain data holder: fields + getters/setters, no business logic.
public class Tax {

    private String stateAbbreviation;
    private String stateName;
    private BigDecimal taxRate;

    public Tax(String stateAbbreviation, String stateName, BigDecimal taxRate) {
        this.stateAbbreviation = stateAbbreviation;
        this.stateName = stateName;
        this.taxRate = taxRate;
    }

    public String getStateAbbreviation() {
        return stateAbbreviation;
    }

    public void setStateAbbreviation(String stateAbbreviation) {
        this.stateAbbreviation = stateAbbreviation;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    // two taxes are equal when all their fields are equal
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tax other = (Tax) o;
        return Objects.equals(stateAbbreviation, other.stateAbbreviation)
                && Objects.equals(stateName, other.stateName)
                && Objects.equals(taxRate, other.taxRate);
    }

    // must use the same fields as equals
    @Override
    public int hashCode() {
        return Objects.hash(stateAbbreviation, stateName, taxRate);
    }

    @Override
    public String toString() {
        return "Tax{stateAbbreviation='" + stateAbbreviation + "', stateName='" + stateName
                + "', taxRate=" + taxRate + "}";
    }
}
