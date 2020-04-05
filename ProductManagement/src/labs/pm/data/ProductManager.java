package labs.pm.data;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProductManager {
    private Map<Product, List<Review>> products = new HashMap<>();
    private ResourceFormatter formatter;
    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());          //customize logger
    private ResourceBundle config = ResourceBundle.getBundle("labs.pm.data.config");
    private MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
    private MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
    private static Map<String, ResourceFormatter> formatters =
            Map.of("en-GB", new ResourceFormatter(Locale.UK),
                    "en-US", new ResourceFormatter(Locale.US),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "ru-RU", new ResourceFormatter(new Locale("ru", "RU")),
                    "zh-CN", new ResourceFormatter(Locale.CHINA),
                    "pl-PL", new ResourceFormatter(new Locale("pl", "PL")) );       //jak nie zadziała to Locale.getDefault()


    public ProductManager(Locale locale) {
        this(locale.toLanguageTag());
    }

    public ProductManager(String languageTag) {
        changeLocale(languageTag);
    }

    public void parseReview (String text) {
        try {
            Object[] values = reviewFormat.parse(text);
            reviewProduct(Integer.parseInt((String) values[0]),
                    Rateable.convert(Integer.parseInt((String) values[1])),
                    (String) values[2]);

        } catch (ParseException | NumberFormatException pex) {
            logger.log(Level.WARNING, "Error parsing review: " + text + " " + pex.getMessage());
        }
    }

    public void parseProduct(String text) {
        try {
            Object[] values = productFormat.parse(text);
            int id = Integer.parseInt((String) values[1]);
            String name = (String) values [2];
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String) values[3]));
            Rating rating = Rateable.convert(Integer.parseInt((String) values[4]));

            switch ((String) values[0]) {
                case "D":
                    createProduct(id, name, price, rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse((String) values[5]);
                    createProduct(id, name, price, rating, bestBefore);
                    break;
            }

        } catch (ParseException | NumberFormatException | DateTimeParseException pex) {
            logger.log(Level.WARNING, "Error parsing product: " + text + " " + pex.getMessage());
        }
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product product = new Food(id, name, price, rating, bestBefore);
        products.putIfAbsent(product, new ArrayList<>());
        return product;
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
        Product product = new Drink(id, name, price, rating);
        products.putIfAbsent(product, new ArrayList<>());
        return product;
    }

    public Product reviewProduct(Product product, Rating rating, String comments) {
        List <Review> reviews = products.get(product);
        products.remove(product, reviews);
        reviews.add(new Review(rating, comments));

//        int sum = 0;
//        for(Review review : reviews) {
//            sum += review.getRating().ordinal();
//        }
//
//        product = product.applyRating(Rateable.convert(Math.round((float) sum/reviews.size())));

        //stream returns double value, Math.round, convert to int and apply
        product = product.applyRating(Rateable.convert((int) Math.round(
                reviews.stream().mapToInt(p -> p.getRating().ordinal()).average().orElse(0))));

        products.put(product, reviews);
        return product;
    }

    public Product reviewProduct(int id, Rating rating, String comments) {
        try {
            return reviewProduct(findProduct(id), rating, comments);
        } catch (ProductManagerException ex) {
            Logger.getLogger(ProductManagerException.class.getName()).log(Level.INFO, ex.getMessage());
            return null;
        }
    }


    public Product findProduct(int id) throws ProductManagerException {
        //first version of this method
//        Product result = null;
//        for (Product product : products.keySet()){
//            if(product.getId() == id) {
//                result = product;
//                break;
//            }
//        }
//        return result;

        //version with streams and lambda expression
        //return the product with matching id or null
        return products.keySet().stream().filter(p -> p.getId() == id).
                findFirst().orElseThrow(() -> new ProductManagerException("Product with id = " + id + " not found!"));
    }

    public void printProductReport(Product product) {
        StringBuilder txt = new StringBuilder();
        List <Review> reviews = products.get(product);
        Collections.sort(reviews);

        txt.append(formatter.formatProduct(product));
        txt.append('\n');
//        for (Review review: reviews) {
//            txt.append(formatter.formatReview(review));
//            txt.append('\n');
//        }
//
//        if (reviews.isEmpty()) {     //only in this case there isn't any reviews
//            txt.append(formatter.getText("no.reviews"));
//            txt.append('\n');
//        }
        if (reviews.isEmpty()) {
            txt.append(formatter.getText("no.reviews") + '\n');
        } else {
            //the same as up + join this strings together, map: Review into String
            txt.append(reviews.stream().map(r->formatter.formatReview(r) +'\n')
                    .collect(Collectors.joining()));
        }
        System.out.println(txt);
    }

    public void printProductReport(int id) {
        try {
            printProductReport(findProduct(id));
        } catch (ProductManagerException ex) {
            Logger.getLogger(ProductManagerException.class.getName()).log(Level.INFO, ex.getMessage());
        }
    }

    public void printProducts(Predicate<Product> filter, Comparator <Product> sorter) {
//        List <Product> productList = new ArrayList<>(products.keySet());
//        productList.sort(sorter);
        StringBuilder txt = new StringBuilder();

//        for (Product product : productList) {
//            txt.append(formatter.formatProduct(product));
//            txt.append('\n');
//        }
        products.keySet().stream().sorted(sorter).filter(filter).
                forEach(p -> txt.append(formatter.formatProduct(p)).append('\n'));
        System.out.println(txt);
    }

    /**
     * Function that calculate a total of all discount values
     * for each group of products that have the same rating
     * @return value of discount using Streams API
     */
    public Map<String, String> getDiscounts() {
        return products.keySet().stream().collect(
                Collectors.groupingBy(
                        p -> p.getRating().getStars(),
                        Collectors.collectingAndThen(
                                Collectors.summingDouble(
                                        p -> p.getDiscount().doubleValue()),
                                        discount -> formatter.moneyFormat.format(discount))));
    }

    public void changeLocale (String languageTag) {
        formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
    }

    public static Set<String> getSupportedLocales () {
        return formatters.keySet();
    }


    private static class ResourceFormatter {
        private Locale locale;
        private ResourceBundle resources;
        private DateTimeFormatter dateFormat;
        private NumberFormat moneyFormat;

        private ResourceFormatter (Locale locale) {
            this.locale = locale;
            resources = ResourceBundle.getBundle("labs.pm.data.resources", locale);
            dateFormat =DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
            moneyFormat = NumberFormat.getCurrencyInstance(locale);
        }

        private String formatProduct (Product product) {
            return MessageFormat.format(resources.getString("product"),
                    product.getName(),
                    moneyFormat.format(product.getPrice()),
                    product.getRating().getStars(),
                    dateFormat.format(product.getBestBefore()));
        }

        private String formatReview (Review review) {
            return MessageFormat.format(resources.getString("review"),
                    review.getRating().getStars(),
                    review.getComments());
        }

        private String getText (String key) {
            return resources.getString(key);
        }
    }

}
