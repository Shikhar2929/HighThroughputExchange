package hte.matchingengine;

/**
 * Lifecycle status of an order inside the matching engine.
 *
 * <p>Orders generally start {@link #ACTIVE}, transition to {@link #FILLED} when fully executed, and
 * may become {@link #CANCELLED} if removed or if the remainder of a market order is discarded.
 */
public enum Status {
    /** Eligible to trade and/or rest on the book. */
    ACTIVE,
    /** Fully executed; no remaining volume. */
    FILLED,
    /** Cancelled; should not trade further. */
    CANCELLED;
}
