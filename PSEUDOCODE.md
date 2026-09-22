# Flooring Mastery — Pseudocode

The app is layered: **View → Controller → Service → DAO**. The controller
runs the menu loop, the view handles all screen input/output, the service
holds the business rules, and the DAOs read/write the data files.

```
START PROGRAM
    LOOP forever
        show menu: 1) Display Orders  2) Add Order  3) Edit Order
                   4) Remove Order    5) Export All Data  6) Quit
        selection = ask user for a number 1-6

        TRY
            SWITCH selection
                CASE 1: DISPLAY ORDERS
                CASE 2: ADD ORDER
                CASE 3: EDIT ORDER
                CASE 4: REMOVE ORDER
                CASE 5: EXPORT ALL DATA
                CASE 6: STOP LOOP
                DEFAULT: show "unknown command"
        CATCH file/storage error
            show error message, go back to menu
    END LOOP
    show "Goodbye!"
END PROGRAM


──────────────────────────────
DISPLAY ORDERS
──────────────────────────────
    ask user for a date
    orders = look up all orders for that date
    IF no orders found
        show "No orders exist for this date"
    ELSE
        print a table: order#, customer, state, product, area,
                        material, labor, tax, total


──────────────────────────────
ADD ORDER
──────────────────────────────
    load list of taxable states and list of products (to show as choices)

    REPEAT ask for order date UNTIL it is a valid future date
    REPEAT ask for customer name UNTIL it is non-blank and only letters/numbers/spaces/.,
    REPEAT ask for state UNTIL it matches a state we sell in
    REPEAT ask for product UNTIL it matches a product we sell
    REPEAT ask for area UNTIL it is a number >= 100 sq ft

    CALCULATE ORDER:
        material cost = area × product's cost per sq ft
        labor cost    = area × product's labor cost per sq ft
        tax           = (material + labor) × state tax rate / 100
        total         = material + labor + tax

    show full order summary
    ask "Place order? (Y/N)"
    IF yes
        assign next order number (highest existing + 1)
        save order to file
        show "Order placed"
    ELSE
        show "Order was not placed"


──────────────────────────────
FIND ORDER (used by Edit and Remove)
──────────────────────────────
    ask for date and order number
    look up the order
    IF not found
        show error, RETURN nothing
    ELSE
        RETURN the order


──────────────────────────────
EDIT ORDER
──────────────────────────────
    order = FIND ORDER
    IF order not found, STOP here

    make a COPY of the order (so nothing changes unless confirmed)

    ask for new customer name (Enter = keep current)   → validate
    ask for new state         (Enter = keep current)   → validate
    ask for new product       (Enter = keep current)   → validate
    ask for new area          (Enter = keep current)   → validate

    IF state OR product OR area changed
        RECALCULATE the copy's material/labor/tax/total (same as Add)

    show updated order summary
    ask "Save changes? (Y/N)"
    IF yes
        save the copy over the original
        show "Order updated"
    ELSE
        show "Changes were not saved"


──────────────────────────────
REMOVE ORDER
──────────────────────────────
    order = FIND ORDER
    IF order not found, STOP here

    show order summary
    ask "Are you sure you want to remove this order? (Y/N)"
    IF yes
        delete the order from file
        show "Order removed"
    ELSE
        show "Order was not removed"


──────────────────────────────
EXPORT ALL DATA
──────────────────────────────
    gather every order from every date file
    write them all into one backup file (Backup/DataExport.txt)
    show "All orders exported"
```

## Key business rules

- Order date must be **after** today (today itself is rejected).
- Area must be **≥ 100 sq ft**.
- Customer name: non-blank, letters/digits/spaces/`.`/`,` only.
- State and product must exist in `Taxes.txt` / `Products.txt`.
- Order numbers are only assigned when an order is actually **saved** (so
  cancelled orders don't burn a number).
