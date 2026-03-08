package hte.api.dtos.requests;

public class Preprocessing {
    // Defaults, override via .env (see application.properties / README).
    public static volatile int MAX_PRICE = 1000;
    public static volatile int MIN_PRICE = 0;
    public static volatile int MAX_VOLUME = 1000;
    public static volatile int MIN_VOLUME = 0;

    public static void configure(int maxPrice, int minPrice, int maxVolume, int minVolume) {
        if (maxPrice < minPrice) {
            throw new IllegalArgumentException(
                    "Invalid price bounds: maxPrice < minPrice ("
                            + maxPrice
                            + " < "
                            + minPrice
                            + ")");
        }
        if (maxVolume < minVolume) {
            throw new IllegalArgumentException(
                    "Invalid volume bounds: maxVolume < minVolume ("
                            + maxVolume
                            + " < "
                            + minVolume
                            + ")");
        }
        MAX_PRICE = maxPrice;
        MIN_PRICE = minPrice;
        MAX_VOLUME = maxVolume;
        MIN_VOLUME = minVolume;
    }

    public static int getMaxPrice() {
        return MAX_PRICE;
    }

    public static void setMaxPrice(int maxPrice) {
        if (maxPrice < MIN_PRICE) {
            throw new IllegalArgumentException(
                    "Invalid maxPrice: maxPrice < MIN_PRICE ("
                            + maxPrice
                            + " < "
                            + MIN_PRICE
                            + ")");
        }
        MAX_PRICE = maxPrice;
    }

    public static int truncate(int x) {
        return ((int) ((int) (x * 100))) / 100;
    }

    public static int preprocessPrice(int price) {
        return Math.min(MAX_PRICE, Math.max(MIN_PRICE, truncate(price)));
    }

    public static int preprocessVolume(int volume) {
        return Math.min(MAX_VOLUME, Math.max(MIN_VOLUME, truncate(volume)));
    }

    public static int botPreprocessVolume(int volume) {
        return Math.max(truncate(volume), 0);
    }
}
