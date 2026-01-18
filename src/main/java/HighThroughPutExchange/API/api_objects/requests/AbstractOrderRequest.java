package HighThroughPutExchange.API.api_objects.requests;

import jakarta.validation.constraints.NotNull;
import java.util.function.IntUnaryOperator;

/** Shared state and helpers for authenticated order placement requests. */
public abstract class AbstractOrderRequest extends BasePrivateRequest {
    @NotNull private String ticker;
    @NotNull private int volume;
    @NotNull private boolean isBid;

    private final IntUnaryOperator volumeProcessor;

    protected AbstractOrderRequest(
            String username,
            String sessionToken,
            String ticker,
            int volume,
            boolean isBid,
            IntUnaryOperator volumeProcessor) {
        super(username, sessionToken);
        this.ticker = ticker;
        this.isBid = isBid;
        this.volumeProcessor = volumeProcessor;
        this.volume = applyVolumeProcessor(volume);
    }

    private int applyVolumeProcessor(int volume) {
        return volumeProcessor.applyAsInt(volume);
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = applyVolumeProcessor(volume);
    }

    public boolean getBid() {
        return isBid;
    }

    public void setBid(boolean bid) {
        this.isBid = bid;
    }
}
