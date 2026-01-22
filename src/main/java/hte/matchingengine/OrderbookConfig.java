package hte.matchingengine;

/**
 * Jackson-mappable config root for reading the engine config JSON.
 *
 * <p>Used by "alternative*" initialization methods.
 */
public class OrderbookConfig {
    private OrderbookConfigDefaults defaults;

    public OrderbookConfig() {}

    public OrderbookConfigDefaults getDefaults() {
        return defaults;
    }

    public void setDefaults(OrderbookConfigDefaults defaults) {
        this.defaults = defaults;
    }
}
