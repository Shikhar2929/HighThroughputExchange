package HighThroughPutExchange.API.api_objects.responses;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class GetDetailsResponse {
    @JsonRawValue
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUserDetails(String userDetails) {
        this.userDetails = userDetails;
    }

    private String userDetails;

    public String getUserDetails() {
        return userDetails;
    }
    public GetDetailsResponse(String message, String userDetails) {
        this.message = message;
        this.userDetails = userDetails;
    }
}
