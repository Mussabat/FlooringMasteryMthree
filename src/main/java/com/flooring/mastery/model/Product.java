package com.flooring.mastery.model;

import java.math.BigDecimal;
import java.util.Objects;

// One line of Data/Products.txt: a type of flooring and what it costs per square foot.
// Plain data holder: fields + getters/setters, no business logic.
public class Product {

    private String productType;
    private BigDecimal costPerSquareFoot;
    private BigDecimal laborCostPerSquareFoot;

    public Product(String productType, BigDecimal costPerSquareFoot, BigDecimal laborCostPerSquareFoot) {
        this.productType = productType;
        this.costPerSquareFoot = costPerSquareFoot;
        this.laborCostPerSquareFoot = laborCostPerSquareFoot;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public BigDecimal getCostPerSquareFoot() {
        return costPerSquareFoot;
    }

    public void setCostPerSquareFoot(BigDecimal costPerSquareFoot) {
        this.costPerSquareFoot = costPerSquareFoot;
    }

    public BigDecimal getLaborCostPerSquareFoot() {
        return laborCostPerSquareFoot;
    }

    public void setLaborCostPerSquareFoot(BigDecimal laborCostPerSquareFoot) {
        this.laborCostPerSquareFoot = laborCostPerSquareFoot;
    }

    // two products are equal when all their fields are equal
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Product other = (Product) o;
        return Objects.equals(productType, other.productType)
                && Objects.equals(costPerSquareFoot, other.costPerSquareFoot)
                && Objects.equals(laborCostPerSquareFoot, other.laborCostPerSquareFoot);
    }

    // must use the same fields as equals
    @Override
    public int hashCode() {
        return Objects.hash(productType, costPerSquareFoot, laborCostPerSquareFoot);
    }

    @Override
    public String toString() {
        return "Product{productType='" + productType + "', costPerSquareFoot=" + costPerSquareFoot
                + ", laborCostPerSquareFoot=" + laborCostPerSquareFoot + "}";
    }
}
