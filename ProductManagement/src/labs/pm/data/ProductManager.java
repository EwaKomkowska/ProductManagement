package labs.pm.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public interface ProductManager {

    Product createProduct(int id, String name, BigDecimal price, Rating rating) throws ProductManagerException;

    Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) throws ProductManagerException;

    Product reviewProduct(int id, Rating rating, String comments) throws ProductManagerException;

    Product findProduct(int id) throws ProductManagerException;

    List<Product> findProducts(Predicate<Product> filter) throws ProductManagerException;

    List<Review> findReviews(int id) throws ProductManagerException;

    Map<Rating, BigDecimal> getDiscounts() throws ProductManagerException;
}
