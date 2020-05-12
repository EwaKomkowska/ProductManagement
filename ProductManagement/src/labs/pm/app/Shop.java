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
        ProductFileManager pm = ProductFileManager.getInstance();
        Comparator <Product> ratingSorter = ((p1, p2) -> p2.getRating().ordinal() - p1.getRating().ordinal());

        Comparator <Product> priceSorter = ((p1, p2)-> p2.getPrice().compareTo(p1.getPrice()));

        AtomicInteger clientCount = new AtomicInteger(0);
        Callable <String> client = () -> {
            String clientId = "Client" + clientCount.incrementAndGet();
            String threadName = Thread.currentThread().getName();
            int productId = ThreadLocalRandom.current().nextInt(9) + 101;
            String languageTag = ProductFileManager.getSupportedLocales().stream()
                    .skip(ThreadLocalRandom.current().nextInt(5)).findFirst().get();
            StringBuilder log = new StringBuilder();
            log.append(clientId).append(" ").append(threadName).append("\n-\tstart of log\t-\n");

            log.append(pm.getDiscount(languageTag).entrySet().stream()
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
