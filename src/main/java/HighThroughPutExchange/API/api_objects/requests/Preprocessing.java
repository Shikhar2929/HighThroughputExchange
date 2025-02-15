package HighThroughPutExchange.API.api_objects.requests;

public class Preprocessing {

    public static final double MAX_PRICE = 10000;
    public static final double MIN_PRICE = 0;
    public static final double MAX_VOLUME = 10000;
    public static final double MIN_VOLUME = -10000;

    public static double truncate(double x) {
        return ((double) ((int) (x * 100))) / 100;
    }
    public static double preprocessPrice(double price) {
        //return price;//
        return Math.min(MAX_PRICE, Math.max(MIN_PRICE, truncate(price)));
    }
    public static double preprocessVolume(double volume) {
        return Math.min(MAX_VOLUME, Math.max(MIN_VOLUME, truncate(volume)));
        //return volume;
    }
}
