package HighThroughPutExchange.API.api_objects.responses;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class GetLeadingAuctionBidResponse {

    @JsonRawValue
    private String message;
    private String user;
    private double bid;

    public GetLeadingAuctionBidResponse(String message) {
        this(message, "", 0);
    }

    public GetLeadingAuctionBidResponse(String message, String user, double bid) {
        this.message = message;
        this.user = user;
        this.bid = bid;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
