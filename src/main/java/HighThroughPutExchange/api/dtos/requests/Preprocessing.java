package HighThroughPutExchange.api.dtos.requests;

public class Preprocessing {

    public static final int MAX_PRICE = 3000;
    public static final int MIN_PRICE = 0;
    public static final int MAX_VOLUME = 1000;
    public static final int MIN_VOLUME = 0;

    public static int truncate(int x) {
        return ((int) ((int) (x * 100))) / 100;
    }

    public static int preprocessPrice(int price) {
        // return price;//
        return Math.min(MAX_PRICE, Math.max(MIN_PRICE, truncate(price)));
    }

    public static int preprocessVolume(int volume) {
        return Math.min(MAX_VOLUME, Math.max(MIN_VOLUME, truncate(volume)));
        // return volume;
    }

    public static int botPreprocessVolume(int volume) {
        return Math.max(truncate(volume), 0);
    }
}
