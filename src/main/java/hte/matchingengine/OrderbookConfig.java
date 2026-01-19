package hte.matchingengine;

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
