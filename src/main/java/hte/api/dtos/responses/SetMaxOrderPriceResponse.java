package hte.api.dtos.responses;

public class SetMaxOrderPriceResponse extends AbstractMessageResponse {
    private int maxPrice;

    public SetMaxOrderPriceResponse(String message, int maxPrice) {
        super(message);
        this.maxPrice = maxPrice;
    }

    public int getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(int maxPrice) {
        this.maxPrice = maxPrice;
    }
}
