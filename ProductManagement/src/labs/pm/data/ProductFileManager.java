package labs.pm.data;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProductFileManager implements ProductManager {
    private Map<Product, List<Review>> products = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();
    private final Lock readLock = lock.readLock();
    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());
    private final ResourceBundle config = ResourceBundle.getBundle("labs.pm.data.config");
    private final MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
    private final MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
    private final Path reportsFolder = Path.of(config.getString("reports.folder"));
    private final Path dataFolder = Path.of(config.getString("data.folder"));
    private final Path tempFolder = Path.of(config.getString("temp.folder"));
    private static final Map<String, ResourceFormatter> formatters =
            Map.of("en-GB", new ResourceFormatter(Locale.UK),
                    "en-US", new ResourceFormatter(Locale.US),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "ru-RU", new ResourceFormatter(new Locale("ru", "RU")),
                    "zh-CN", new ResourceFormatter(Locale.CHINA),
                    "pl-PL", new ResourceFormatter(new Locale("pl", "PL")) );

    private static final ProductFileManager pm = new ProductFileManager();


    private ProductFileManager() {
        loadAllData();
    }

    private List<Review> loadReviews (Product product) {
        List<Review> reviews = null;
        Path file = reportsFolder.resolve(
                MessageFormat.format(config.getString("reviews.data.file"), product.getId()));

        if (Files.notExists(file)) {
            reviews = new ArrayList<>();
        } else {
            try {
                reviews = Files.lines(file, Charset.forName("UTF-8"))
                        .map(text -> parseReview(text)).filter(review -> review != null)
                        .collect(Collectors.toList());
            } catch (IOException ex) {
                logger.log(Level.WARNING,"Error loading reviews " + ex.getMessage());
            }
        }
        return reviews;
    }


    private void loadAllData() {
        try {
            writeLock.lock();
            products = Files.list(dataFolder)
                    .filter(file -> file.getFileName().toString().startsWith("product"))
                    .map(file -> loadProduct(file))
                    .filter(product -> product != null)
                    .collect(Collectors.toMap(product -> product, product -> loadReviews(product)));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading data" + e.getMessage(), e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Sth wrong with lock " + e.getMessage(), e);
        } finally {
            writeLock.unlock();
        }
    }

    private Product loadProduct (Path file) {
        Product product = null;
        try {
            writeLock.lock();
            product = parseProduct(Files.lines(dataFolder.resolve(file), Charset.forName("UTF-8")).findFirst().orElseThrow());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error loading product", e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Sth wrong with lock " + e.getMessage(), e);
        } finally {
            writeLock.unlock();
        }

        return product;
    }

    private Review parseReview (String text) {
        Review review = null;
        try {
            Object[] values = reviewFormat.parse(text);
            review = new Review(Rateable.convert(Integer.parseInt((String) values[0])), (String) values[1]);

        } catch (ParseException | NumberFormatException pex) {
            logger.log(Level.WARNING, "Error parsing review: " + text + " " + pex.getMessage());
        }
        return review;
    }

    private Product parseProduct(String text) {
        Product product = null;
        try {
            Object[] values = productFormat.parse(text);
            int id = Integer.parseInt((String) values[1]);
            String name = (String) values [2];
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String) values[3]));
            Rating rating = Rateable.convert(Integer.parseInt((String) values[4]));

            switch ((String) values[0]) {
                case "D":
                    product = new Drink(id, name, price, rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse((String) values[5]);
                    product = new Food(id, name, price, rating, bestBefore);
                    break;
            }

        } catch (ParseException | NumberFormatException | DateTimeParseException pex) {
            logger.log(Level.WARNING, "Error parsing product: " + text + " " + pex.getMessage());
        }
        return product;
    }


    private void dumpData() {
        try {
            if (Files.notExists(tempFolder))
                Files.createDirectory(tempFolder);

            Path tempFile = tempFolder.resolve(
                    MessageFormat.format(config.getString("temp.file"), LocalDate.now()));

            try (ObjectOutputStream out = new ObjectOutputStream(
                    Files.newOutputStream(tempFile, StandardOpenOption.CREATE))) {

                try {
                    readLock.lock();
                    out.writeObject(products);
                    products = new HashMap<>();
                }  catch (Exception e) {
                    logger.log(Level.SEVERE, "Sth wrong with lock " + e.getMessage(), e);
                } finally {
                    readLock.unlock();
                }

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error writing data " + e.getMessage(), e);
            }

        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error dumping data " + e.getMessage(), e);
        }
    }


    @SuppressWarnings("unchecked")
    private void restoreData() {
        try {
            Path tempFile = Files.list(tempFolder)
                    .filter(path -> path.getFileName().toString().endsWith("tmp"))
                    .findFirst().orElseThrow();

            try (ObjectInputStream in = new ObjectInputStream(
                    Files.newInputStream(tempFile, StandardOpenOption.DELETE_ON_CLOSE))) {

                try {
                    writeLock.lock();
                    products = (HashMap) in.readObject();
                }  catch (Exception e) {
                    logger.log(Level.SEVERE, "Sth wrong with lock " + e.getMessage(), e);
                } finally {
                    writeLock.unlock();
                }

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error with loading data from file " + e.getMessage(), e);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error restoring data " + e.getMessage(), e);
        }
    }


    public static ProductFileManager getInstance() {
        return pm;
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product product = null;

        try {
            writeLock.lock();
            product = new Food(id, name, price, rating, bestBefore);
            products.putIfAbsent(product, new ArrayList<>());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Product can't be added " + e.getMessage(), e);
            return null;
        } finally {
            writeLock.unlock();
        }

        return product;
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
        Product product;

        try {
            writeLock.lock();
            product = new Drink(id, name, price, rating);
            products.putIfAbsent(product, new ArrayList<>());
        }catch (Exception e) {
            logger.log(Level.SEVERE, "Product can't be added " + e.getMessage(), e);
            return null;
        } finally {
            writeLock.unlock();
        }

        return product;
    }

    private Product reviewProduct(Product product, Rating rating, String comments) {
        List <Review> reviews = null;

        reviews = products.get(product);
        products.remove(product, reviews);

        reviews.add(new Review(rating, comments));
        product = product.applyRating(Rateable.convert((int) Math.round(
                reviews.stream().mapToInt(p -> p.getRating().ordinal()).average().orElse(0))));

        try {
            writeLock.lock();
            products.put(product, reviews);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Sth wrong with lock " + e.getMessage(), e);
        } finally {
            writeLock.unlock();
        }

        return product;
    }

    public Product reviewProduct(int id, Rating rating, String comments) {
        try {
            writeLock.lock();
            return reviewProduct(findProduct(id), rating, comments);
        } catch (ProductManagerException ex) {
            logger.log(Level.INFO, ex.getMessage());
            return null;
        } finally {
            writeLock.unlock();
        }
    }


    public Product findProduct(int id) throws ProductManagerException {
        try {
            readLock.lock();
            return products.keySet().stream().filter(p -> p.getId() == id).
                    findFirst().orElseThrow(() -> new ProductManagerException("Product with id = " + id + " not found!"));
        } finally {
            readLock.unlock();
        }
    }

    private void printProductReport(Product product, String languageTag, String client) throws IOException {

        ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
        Path productFile = reportsFolder.resolve(MessageFormat.format(config.getString(
                "report.file"), product.getId(), client));

        List<Review> reviews = products.get(product);
        Collections.sort(reviews);


        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(productFile, StandardOpenOption.CREATE), "UTF-8"))) {
            out.append(formatter.formatProduct(product) + System.lineSeparator());

            if (reviews.isEmpty()) {
                out.append(formatter.getText("no.reviews") + System.lineSeparator());
            } else {
                //the same as up + join this strings together, map: Review into String
                out.append(reviews.stream().map(r -> formatter.formatReview(r) + System.lineSeparator())
                        .collect(Collectors.joining()));
            }
            System.out.println(out);
        }
    }

    public void printProductReport(int id, String languageTag, String client) {
        try {
            writeLock.lock();
            printProductReport(findProduct(id), languageTag, client);
        } catch (ProductManagerException ex) {
            logger.log(Level.INFO, ex.getMessage());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error printing product report " + ex.getMessage(), ex);
        } finally {
            writeLock.unlock();
        }
    }

    public List<Product> findProducts(Predicate<Product> filter) {
        try {
            readLock.lock();
            return products.keySet().stream().filter(filter).collect(Collectors.toList());

        } finally {
            readLock.unlock();
        }
    }

    public List<Review> findReviews(int id) {
        try {
            Product product = findProduct(id);
            return loadReviews(product);
        } catch (Exception e) {
            return null;
        }
    }

    public  Map<Rating, BigDecimal> getDiscounts() {
        try {
            readLock.lock();

            return products.keySet().stream().collect(
                    Collectors.groupingBy(
                            Product::getRating,
                            Collectors.collectingAndThen(
                                    Collectors.summingDouble(
                                            p -> p.getDiscount().doubleValue()),
                                    BigDecimal::valueOf)));
        } finally {
            readLock.unlock();
        }
    }


    public void printProducts(Predicate<Product> filter, Comparator <Product> sorter, String languageTag) {
        try {
            readLock.lock();

            ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
            StringBuilder txt = new StringBuilder();
            products.keySet().stream().sorted(sorter).filter(filter).
                    forEach(p -> txt.append(formatter.formatProduct(p)).append('\n'));

            System.out.println(txt);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Function that calculate a total of all discount values
     * for each group of products that have the same rating
     * @return value of discount using Streams API
     */
    public Map<String, String> getDiscount(String languageTag) {
        try {
            readLock.lock();
            ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));

            return products.keySet().stream().collect(
                    Collectors.groupingBy(
                            p -> p.getRating().getStars(),
                            Collectors.collectingAndThen(
                                    Collectors.summingDouble(
                                            p -> p.getDiscount().doubleValue()),
                                    discount -> formatter.getMoneyFormat().format(discount))));
        } finally {
            readLock.unlock();
        }
    }

    public static Set<String> getSupportedLocales () {
        return formatters.keySet();
    }

}
