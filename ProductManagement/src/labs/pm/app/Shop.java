package labs.pm.app;


import labs.pm.data.*;

import java.math.BigDecimal;
import java.time.LocalDate;

//TODO: slajd ..., lekcja 7: ...

/**
 *
 * {@code Shop} class represents an application that manages Products
 * @version 1.0
 * @author Ewa
 */
public class Shop {

    /**
     * @param args witch is not used yet
     */
    public static void main(String[] args) {
        ProductManager pm = new ProductManager();
        Product p1 = pm.createProduct(101, "Tea", BigDecimal.valueOf(1.99), Rating.THREE_STARS);

        System.out.println(p1);

        Product p2 = pm.createProduct(102, "Coffee", BigDecimal.valueOf(1.99), Rating.FOUR_STARS);
        Product p3 = pm.createProduct(103, "Cake", BigDecimal.valueOf(3.99), Rating.FIVE_STARS, LocalDate.now().plusDays(2));

        System.out.println(p2);
        System.out.println(p3);

        Product p4 = pm.createProduct(105, "Cookie", BigDecimal.valueOf(3.99), Rating.TWO_STARS, LocalDate.now());
        System.out.println(p4);

        Product p5 = p3.applyRating(Rating.THREE_STARS);
        System.out.println(p5);

        Product p8 = p4.applyRating(Rating.TWO_STARS);
        Product p9 = p1.applyRating(Rating.TWO_STARS);
        System.out.println(p8);
        System.out.println(p9);

        Product p6 = pm.createProduct(104, "Chocolate", BigDecimal.valueOf(2.99), Rating.FIVE_STARS);
        Product p7 = pm.createProduct(104, "Chocolate", BigDecimal.valueOf(2.99), Rating.FIVE_STARS, LocalDate.now().plusDays(2));
        System.out.println(p6.equals(p7));

        System.out.println(p3.getBestBefore());
        System.out.println(p1.getBestBefore());
    }
}
