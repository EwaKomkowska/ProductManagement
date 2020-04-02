package labs.pm.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 *{@code Product} class represents properties and behaviours of
 * product objects in the Product Managment Syste.
 * <br>
 *      Each product has an id, name and price
 * <br>
 * {@link #DISCOUNT_RATE discount rate}
 * @version 1.0
 * @author Ewa
 */
public abstract class Product implements Rateable<Product> {
    /**
     * Properties:
     * id - unique int value for ALL products
     * {@link String}name
     * {@link BigDecimal}price
     * {@link Rating}rating - stars, min: 0, max:  5
     */
    private int id;
    private String name;
    private BigDecimal price;
    private Rating rating;

    /**
     * A constant that defines a
     * {@link java.math.BigDecimal BigDecimal} value of the discount rate
     * <br>
     *     Discount rate is 10%
     */
    public static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.1);        //Java name convention to constant


    Product (int id, String name, BigDecimal price, Rating rating) {            //visible only in this package
        this.id = id;
        this.name = name;
        this.price = price;
        this.rating = rating;
    }

    @Override
    public Rating getRating() {
        return rating;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }


    /**
     * Override method from class {@link String}
     * @return "Product: " with its properties
     */
    @Override
    public String toString() {
        return "Product: " + id + " " + name + " " + price + " "
                + getDiscount() + " " + rating.getStars() + " " + getBestBefore();
    }

    /**
     * Override method
     * New hash is 89 * 7 + id
     *
     * @return hash
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.id;
        return hash;
    }

    /**
     * override method from class {@link String}
     *
     * Check if objects have the same id and name or
     * they are the same objects
     *
     * @param obj to compare with
     * @return true or false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof Product) {
            final Product other = (Product) obj;
            return this.id == other.id;
        }
        return false;
    }

    /**
     * Calculates discount based on a product price and
     * {@link #DISCOUNT_RATE discount rate}
     * @return a {@link java.math.BigDecimal BigDecimal}
     * value of the discount
     */
    public BigDecimal getDiscount () {
        return price.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Assumes that the best before date is today
     *
     * @return the current date
     */
    public LocalDate getBestBefore() {
        return LocalDate.now();
    }

}
