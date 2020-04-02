package labs.pm.data;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class Food extends Product {    //prevent being extended
    private LocalDate bestBefore;

    Food (int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {         //visible only to members of the same package
        super(id, name, price, rating);
        this.bestBefore = bestBefore;
    }

    /**
     * Get the value of bestBefore
     *
     * @return the value of BestBefore
     */
    public LocalDate getBestBefore() {
        return bestBefore;
    }

    @Override
    public BigDecimal getDiscount() {
        return (bestBefore.equals(LocalDate.now())) ? super.getDiscount() : BigDecimal.ZERO;
    }

    @Override
    public Product applyRating(Rating newRating) {
        return new Food(getId(), getName(), getPrice(), newRating, getBestBefore());
    }
}
