package labs.pm.app;


import labs.pm.data.*;


import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO: page 303, lesson 15-2


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
        ProductManager pm = ProductManager.getInstance();
        //check by lambda if product1 rating > product2 rating AVERAGE!!!  from the heighest value to the lowest one
        Comparator <Product> ratingSorter = ((p1, p2) -> p2.getRating().ordinal() - p1.getRating().ordinal());

        //sort by price - BigDecimal, so comapreTo method
        Comparator <Product> priceSorter = ((p1, p2)-> p2.getPrice().compareTo(p1.getPrice()));

//        pm.changeLocale("pl-PL");
//        pm.createProduct(101, "Tea", BigDecimal.valueOf(1.99), Rating.NOT__RATED);
//        pm.printProductReport(101);
//
//        pm.reviewProduct(101, Rating.FOUR_STARS, "My favourites tea in this place");
//        pm.reviewProduct(101, Rating.THREE_STARS, "It's ok but I drink some better tea");
//        pm.reviewProduct(101, Rating.TWO_STARS, "It's not a hot tea!");
//        pm.reviewProduct(101, Rating.FIVE_STARS, "The best tea whenever you drink");
//        pm.reviewProduct(101, Rating.THREE_STARS, "Just add some lemon");
//        pm.reviewProduct(101, Rating.FOUR_STARS, "Fine tea");
//
//        pm.printProductReport(101);
//
//        pm.changeLocale("en-GB");
//
//        pm.createProduct(102, "Coffee", BigDecimal.valueOf(1.99), Rating.NOT__RATED);
//        pm.reviewProduct(102, Rating.THREE_STARS, "It was without milk");
//        pm.reviewProduct(102, Rating.FIVE_STARS, "Ideal mix for me and this biscuit...");
//        pm.reviewProduct(102, Rating.TWO_STARS, "Drink only with half a cup of sugar");
//
//        pm.printProductReport(102);
//
//        pm.changeLocale("ru-RU");
//
//        pm.createProduct(103, "Cake", BigDecimal.valueOf(3.99), Rating.NOT__RATED);
//        pm.reviewProduct(103, Rating.THREE_STARS, "Too much cream");
//        pm.reviewProduct(103, Rating.FIVE_STARS, "This strawberry is so cute");
//        pm.reviewProduct(103, Rating.TWO_STARS, "It's awful");
//        pm.reviewProduct(103, Rating.ONE_STAR, "I don't get it!");
//
//        pm.printProductReport(103);
//
//        pm.printProductReport(104);
//        pm.printProductReport(105);
//        pm.printProducts(p -> p.getPrice().floatValue() < 2, ratingSorter);
//        pm.getDiscounts().forEach((rating, discount) -> System.out.println(rating +'\t' + discount));
//
//        pm.parseReview("101, 4, Nice hot cup of tea");
//        pm.printProductReport(101);
//
//        pm.parseProduct("F, 103, Cake, 3.99, 0, 2019-09-09");
//        pm.printProductReport(101);
//
//        pm.parseProduct("D, 101, Tea, 1.99, 0, 2019-09-19");
//        pm.parseReview("101, 4, Nice to hot cup of tea");
//        pm.parseReview("101, 2, Rather weak tea");
//        pm.parseReview("101, 4, Good tea");
//        pm.parseReview("101, 5, Perfect tea");
//        pm.parseReview("101, 3, Just add some lemon");
//        pm.printProductReport(101);
//        pm.parseProduct("F, 103, Cake, 3.99, 0, 2019-09-19");
//        pm.printProductReport(103);

//        pm.createProduct(164, "Kombucha", BigDecimal.valueOf(1.99), Rating.NOT__RATED);
//        pm.reviewProduct(164, Rating.TWO_STARS, "Looks like tea but is it?");
//        pm.reviewProduct(164, Rating.FOUR_STARS, "Fine tea");
//        pm.reviewProduct(164, Rating.FOUR_STARS, "This is not tea");
//        pm.reviewProduct(164, Rating.FIVE_STARS, "Perfect!");


//        pm.printProductReport(164);
//        pm.printProducts(p -> p.getPrice().floatValue() < 2, (p1, p2) -> p2.getRating().ordinal() - p1.getRating().ordinal());
//        pm.getDiscounts().forEach(
//                (rating, discount) -> System.out.println(rating + "\t" + discount));


        AtomicInteger clientCount = new AtomicInteger(0);
        Callable <String> client = () -> {
            String clientId = "Client" + clientCount.incrementAndGet();
            String threadName = Thread.currentThread().getName();
            int productId = ThreadLocalRandom.current().nextInt(9) + 101;
            String languageTag = ProductManager.getSupportedLocales().stream()
                    .skip(ThreadLocalRandom.current().nextInt(5)).findFirst().get();
            StringBuilder log = new StringBuilder();
            log.append(clientId).append(" ").append(threadName).append("\n-\tstart of log\t-\n");

            log.append(pm.getDiscounts(languageTag).entrySet().stream()
                    .map(entry -> entry.getKey() + "\t" + entry.getValue()).collect(Collectors.joining("\n")));
            Product product = pm.reviewProduct(productId, Rating.FOUR_STARS, "Yet another review");
            log.append((product != null) ? "\nProduct " + productId + " reviewed\n" :
                    "\nProduct " + productId + " not reviewed\n");
            pm.printProductReport(productId, languageTag, clientId);
            log.append(clientId).append(" generated report for ").append(productId).append(" product\n");

            log.append("\n-\tend of log\t-\n");
            return log.toString();
        };

        List <Callable<String>> clients = Stream.generate(() -> client).limit(5).collect(Collectors.toList());
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        try {
            List<Future<String>> results = executorService.invokeAll(clients);
            executorService.shutdown();

            results.stream().forEach(result -> {
                try {
                    System.out.println(result.get());
                } catch (InterruptedException | ExecutionException e) {
                    Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, "Error retrieving client log", e);
                }
            });
        } catch (InterruptedException e) {
            Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, "Error invoking clients", e);
        }

    }
}
