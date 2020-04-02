package labs.pm.app;


import labs.pm.data.*;

import java.lang.ref.PhantomReference;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Locale;

//TODO: slajd 202, lekcja 11

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
        ProductManager pm = new ProductManager("en-GB");
        //check by lambda if product1 rating > product2 rating AVERAGE!!!  from the heighest value to the lowest one
        Comparator <Product> ratingSorter = ((p1, p2) -> p2.getRating().ordinal() - p1.getRating().ordinal());

        //sort by price - BigDecimal, so comapreTo method
        Comparator <Product> priceSorter = ((p1, p2)-> p2.getPrice().compareTo(p1.getPrice()));

        pm.changeLocale("pl-PL");
        pm.createProduct(101, "Tea", BigDecimal.valueOf(1.99), Rating.NOT__RATED);
//        pm.printProductReport(101);

        pm.reviewProduct(101, Rating.FOUR_STARS, "My favourites tea in this place");
        pm.reviewProduct(101, Rating.THREE_STARS, "It's ok but I drink some better tea");
        pm.reviewProduct(101, Rating.TWO_STARS, "It's not a hot tea!");
        pm.reviewProduct(101, Rating.FIVE_STARS, "The best tea whenever you drink");
        pm.reviewProduct(101, Rating.THREE_STARS, "Just add some lemon");
        pm.reviewProduct(101, Rating.FOUR_STARS, "Fine tea");

//        pm.printProductReport(101);

        pm.changeLocale("en-GB");

        pm.createProduct(102, "Coffee", BigDecimal.valueOf(1.99), Rating.NOT__RATED);
        pm.reviewProduct(102, Rating.THREE_STARS, "It was without milk");
        pm.reviewProduct(102, Rating.FIVE_STARS, "Ideal mix for me and this biscuit...");
        pm.reviewProduct(102, Rating.TWO_STARS, "Drink only with half a cup of sugar");

//        pm.printProductReport(102);

        pm.changeLocale("ru-RU");

        pm.createProduct(103, "Cake", BigDecimal.valueOf(3.99), Rating.NOT__RATED);
        pm.reviewProduct(103, Rating.THREE_STARS, "Too much cream");
        pm.reviewProduct(103, Rating.FIVE_STARS, "This strawberry is so cute");
        pm.reviewProduct(103, Rating.TWO_STARS, "It's awful");
        pm.reviewProduct(103, Rating.ONE_STAR, "I don't get it!");

//        pm.printProductReport(103);

        pm.printProducts(ratingSorter.thenComparing(priceSorter).reversed());
    }
}
