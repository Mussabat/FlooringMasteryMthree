package com.flooring.mastery.service;

import com.flooring.mastery.model.Order;
import com.flooring.mastery.model.Product;
import com.flooring.mastery.model.Tax;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Stateless tests for FlooringServiceImpl: the stubs live in memory, so no files are used
// and every run gives the same result.
public class FlooringServiceImplTest {

    private FlooringService service;

    // runs before EVERY test: a fresh service that is wired to the three stubs
    @BeforeEach
    void setUp() {
        service = new FlooringServiceImpl(new OrderDaoStubImpl(), new ProductDaoStubImpl(), new TaxDaoStubImpl());
    }

    @Test
    void getOrdersForKnownDateReturnsOneOrder() throws Exception {
        List<Order> orders = service.getOrdersForDate(OrderDaoStubImpl.KNOWN_DATE);

        assertEquals(1, orders.size());
        assertEquals(OrderDaoStubImpl.KNOWN_ORDER_NUMBER, orders.get(0).getOrderNumber());
    }

    // the stub returns an empty list for this date, so the service must turn it into an exception
    @Test
    void getOrdersForUnknownDateThrowsNoSuchOrderException() {
        LocalDate unknownDate = LocalDate.of(1999, 1, 1);

        assertThrows(NoSuchOrderException.class, () -> service.getOrdersForDate(unknownDate));
    }

    @Test
    void getKnownOrderReturnsIt() throws Exception {
        Order order = service.getOrder(OrderDaoStubImpl.KNOWN_DATE, OrderDaoStubImpl.KNOWN_ORDER_NUMBER);

        assertEquals("Ada Lovelace", order.getCustomerName());
        assertEquals(OrderDaoStubImpl.KNOWN_ORDER_NUMBER, order.getOrderNumber());
    }

    // the stub returns null for this order number, so the service must turn it into an exception
    @Test
    void getMissingOrderThrowsNoSuchOrderException() {
        assertThrows(NoSuchOrderException.class, () -> service.getOrder(OrderDaoStubImpl.KNOWN_DATE, 99));
    }

    // right order number but wrong date is also "not found"
    @Test
    void getOrderOnWrongDateThrowsNoSuchOrderException() {
        LocalDate wrongDate = LocalDate.of(2013, 6, 2);

        assertThrows(NoSuchOrderException.class,
                () -> service.getOrder(wrongDate, OrderDaoStubImpl.KNOWN_ORDER_NUMBER));
    }

    // ---------- edit ----------

    // the stub gives the order back, so we can check that the service returns the edited order
    @Test
    void editExistingOrderReturnsTheEditedOrder() throws Exception {
        Order edited = new Order(service.getOrder(OrderDaoStubImpl.KNOWN_DATE, OrderDaoStubImpl.KNOWN_ORDER_NUMBER));
        edited.setCustomerName("Grace Hopper");

        Order saved = service.editOrder(edited);

        assertEquals("Grace Hopper", saved.getCustomerName());
    }

    // the stub has no order 99, so the service must refuse to edit it
    @Test
    void editMissingOrderThrowsNoSuchOrderException() {
        Order missing = new Order();
        missing.setOrderDate(OrderDaoStubImpl.KNOWN_DATE);
        missing.setOrderNumber(99);

        assertThrows(NoSuchOrderException.class, () -> service.editOrder(missing));
    }

    // ---------- remove ----------

    @Test
    void removeExistingOrderReturnsIt() throws Exception {
        Order removed = service.removeOrder(OrderDaoStubImpl.KNOWN_DATE, OrderDaoStubImpl.KNOWN_ORDER_NUMBER);

        assertEquals("Ada Lovelace", removed.getCustomerName());
    }

    // the service checks with getOrder first, so a missing order becomes an exception
    @Test
    void removeMissingOrderThrowsNoSuchOrderException() {
        assertThrows(NoSuchOrderException.class, () -> service.removeOrder(OrderDaoStubImpl.KNOWN_DATE, 99));
    }

    // ---------- order date ----------

    @Test
    void futureDateIsValid() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        assertEquals(tomorrow, service.validateOrderDate(tomorrow));
    }

    // the rule says "after today", so today itself is NOT allowed
    @Test
    void todayIsNotValid() {
        assertThrows(OrderValidationException.class, () -> service.validateOrderDate(LocalDate.now()));
    }

    @Test
    void pastDateIsNotValid() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        assertThrows(OrderValidationException.class, () -> service.validateOrderDate(yesterday));
    }

    // ---------- customer name ----------

    // commas and periods are allowed, and the spaces around the name are removed
    @Test
    void nameWithCommaAndPeriodIsValidAndTrimmed() throws Exception {
        assertEquals("Acme, Inc.", service.validateCustomerName("  Acme, Inc.  "));
    }

    @Test
    void blankNameIsNotValid() {
        assertThrows(OrderValidationException.class, () -> service.validateCustomerName("   "));
    }

    @Test
    void nameWithBadCharactersIsNotValid() {
        assertThrows(OrderValidationException.class, () -> service.validateCustomerName("Bob & Sons!"));
    }

    // ---------- state ----------

    // lower case "ca" finds the CA tax (the stub matches ignoring case, like the real DAO)
    @Test
    void knownStateIgnoringCaseReturnsItsTax() throws Exception {
        Tax tax = service.validateState("ca");

        assertEquals("CA", tax.getStateAbbreviation());
    }

    @Test
    void unknownStateIsNotValid() {
        assertThrows(OrderValidationException.class, () -> service.validateState("ZZ"));
    }

    // ---------- product ----------

    @Test
    void knownProductIgnoringCaseReturnsIt() throws Exception {
        Product product = service.validateProductType("tile");

        assertEquals("Tile", product.getProductType());
    }

    @Test
    void unknownProductIsNotValid() {
        assertThrows(OrderValidationException.class, () -> service.validateProductType("Marble"));
    }

    // ---------- area ----------

    // 100 is allowed, and it comes back with 2 decimal places
    @Test
    void areaOf100IsValidWithTwoDecimals() throws Exception {
        assertEquals(new BigDecimal("100.00"), service.validateArea(new BigDecimal("100")));
    }

    @Test
    void areaBelow100IsNotValid() {
        assertThrows(OrderValidationException.class, () -> service.validateArea(new BigDecimal("99.99")));
    }

    @Test
    void negativeAreaIsNotValid() {
        assertThrows(OrderValidationException.class, () -> service.validateArea(new BigDecimal("-150")));
    }

    // ---------- calculation ----------

    // the check values from the plan: Area 249, Tile, CA
    @Test
    void calculateOrderMatchesTheSampleValues() throws Exception {
        Order order = new Order();
        order.setArea(new BigDecimal("249.00"));

        service.calculateOrder(order, service.validateState("CA"), service.validateProductType("Tile"));

        assertEquals(new BigDecimal("871.50"), order.getMaterialCost());
        assertEquals(new BigDecimal("1033.35"), order.getLaborCost());
        assertEquals(new BigDecimal("476.21"), order.getTax());
        assertEquals(new BigDecimal("2381.06"), order.getTotal());
    }

    // the state, tax rate, product and prices are copied from the Tax and Product objects
    @Test
    void calculateOrderCopiesStateAndProductValues() throws Exception {
        Order order = new Order();
        order.setArea(new BigDecimal("249.00"));

        service.calculateOrder(order, service.validateState("ca"), service.validateProductType("tile"));

        assertEquals("CA", order.getState());
        assertEquals(new BigDecimal("25.00"), order.getTaxRate());
        assertEquals("Tile", order.getProductType());
        assertEquals(new BigDecimal("3.50"), order.getCostPerSquareFoot());
        assertEquals(new BigDecimal("4.15"), order.getLaborCostPerSquareFoot());
    }

    // ---------- add ----------

    // the stub's highest order number is 4, so the new order gets 5
    @Test
    void addOrderGivesTheNextOrderNumber() throws Exception {
        Order order = new Order();
        order.setOrderDate(LocalDate.now().plusDays(1));

        Order saved = service.addOrder(order);

        assertEquals(5, saved.getOrderNumber());
    }
}
