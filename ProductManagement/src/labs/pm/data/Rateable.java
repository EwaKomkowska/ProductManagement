package labs.pm.data;

@FunctionalInterface
public interface Rateable <T> {
    public static final Rating DEFAULT_RATING = Rating.NOT__RATED;

    public abstract T applyRating(Rating rating);

    public default T applyRating(int stars){
        return applyRating(convert(stars));
    }

    default Rating getRating() {        //metody są defaultowo publiczne
        return DEFAULT_RATING;
    }

    public static Rating convert(int stars) {
        return (stars >= 0 && stars <= 5) ? Rating.values()[stars] : DEFAULT_RATING;
    }

}
