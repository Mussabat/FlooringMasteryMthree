package com.flooring.mastery.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

// One flooring order. It has the 12 fields stored on a line of an orders file,
// plus orderDate. The date is NOT written in the line: it comes from the file name.
// Plain data holder: fields + getters/setters, no business logic.
public class Order {

    private int orderNumber;
    private String customerName;
    private String state;
    private BigDecimal taxRate;
    private String productType;
    private BigDecimal area;
    private BigDecimal costPerSquareFoot;
    private BigDecimal laborCostPerSquareFoot;
    private BigDecimal materialCost;
    private BigDecimal laborCost;
    private BigDecimal tax;
    private BigDecimal total;
    private LocalDate orderDate;

    // empty constructor: the order is filled in step by step with the setters
    public Order() {
    }

    // copy constructor: builds a new Order with the same values as "order".
    // Used by Edit, so we change the copy and the original stays untouched until the user confirms.
    // (BigDecimal, String and LocalDate are immutable, so sharing them between both orders is safe.)
    public Order(Order order) {
        this.orderNumber = order.orderNumber;
        this.customerName = order.customerName;
        this.state = order.state;
        this.taxRate = order.taxRate;
        this.productType = order.productType;
        this.area = order.area;
        this.costPerSquareFoot = order.costPerSquareFoot;
        this.laborCostPerSquareFoot = order.laborCostPerSquareFoot;
        this.materialCost = order.materialCost;
        this.laborCost = order.laborCost;
        this.tax = order.tax;
        this.total = order.total;
        this.orderDate = order.orderDate;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
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

    public BigDecimal getMaterialCost() {
        return materialCost;
    }

    public void setMaterialCost(BigDecimal materialCost) {
        this.materialCost = materialCost;
    }

    public BigDecimal getLaborCost() {
        return laborCost;
    }

    public void setLaborCost(BigDecimal laborCost) {
        this.laborCost = laborCost;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    // two orders are equal when all their fields are equal.
    // BigDecimal.equals also compares the scale, so 2.5 is NOT equal to 2.50.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Order other = (Order) o;
        return orderNumber == other.orderNumber
                && Objects.equals(customerName, other.customerName)
                && Objects.equals(state, other.state)
                && Objects.equals(taxRate, other.taxRate)
                && Objects.equals(productType, other.productType)
                && Objects.equals(area, other.area)
                && Objects.equals(costPerSquareFoot, other.costPerSquareFoot)
                && Objects.equals(laborCostPerSquareFoot, other.laborCostPerSquareFoot)
                && Objects.equals(materialCost, other.materialCost)
                && Objects.equals(laborCost, other.laborCost)
                && Objects.equals(tax, other.tax)
                && Objects.equals(total, other.total)
                && Objects.equals(orderDate, other.orderDate);
    }

    // must use the same fields as equals
    @Override
    public int hashCode() {
        return Objects.hash(orderNumber, customerName, state, taxRate, productType, area,
                costPerSquareFoot, laborCostPerSquareFoot, materialCost, laborCost, tax, total, orderDate);
    }

    @Override
    public String toString() {
        return "Order{orderNumber=" + orderNumber
                + ", customerName='" + customerName + "'"
                + ", state='" + state + "'"
                + ", taxRate=" + taxRate
                + ", productType='" + productType + "'"
                + ", area=" + area
                + ", costPerSquareFoot=" + costPerSquareFoot
                + ", laborCostPerSquareFoot=" + laborCostPerSquareFoot
                + ", materialCost=" + materialCost
                + ", laborCost=" + laborCost
                + ", tax=" + tax
                + ", total=" + total
                + ", orderDate=" + orderDate + "}";
    }
}
