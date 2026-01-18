package HighThroughPutExchange.API.api_objects.requests;

import jakarta.validation.constraints.NotNull;
import java.util.function.IntUnaryOperator;

/** Base class for limit order requests that layers price handling on top of core order data. */
public abstract class AbstractLimitOrderRequest extends AbstractOrderRequest {
    @NotNull private int price;

    private final IntUnaryOperator priceProcessor;

    protected AbstractLimitOrderRequest(
            String username,
            String sessionToken,
            String ticker,
            int volume,
            int price,
            boolean isBid,
            IntUnaryOperator volumeProcessor,
            IntUnaryOperator priceProcessor) {
        super(username, sessionToken, ticker, volume, isBid, volumeProcessor);
        this.priceProcessor = priceProcessor;
        this.price = applyPriceProcessor(price);
    }

    private int applyPriceProcessor(int price) {
        return priceProcessor.applyAsInt(price);
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = applyPriceProcessor(price);
    }
}
