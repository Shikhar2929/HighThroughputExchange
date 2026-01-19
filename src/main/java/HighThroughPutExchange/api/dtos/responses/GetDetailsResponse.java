package HighThroughPutExchange.api.dtos.responses;

public class GetDetailsResponse extends AbstractMessageResponse {

    public void setUserDetails(String userDetails) {
        this.userDetails = userDetails;
    }

    private String userDetails;

    public String getUserDetails() {
        return userDetails;
    }

    public GetDetailsResponse(String message, String userDetails) {
        super(message);
        this.userDetails = userDetails;
    }
}
