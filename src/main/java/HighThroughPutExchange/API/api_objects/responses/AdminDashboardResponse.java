package HighThroughPutExchange.API.api_objects.responses;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class AdminDashboardResponse {
    @JsonRawValue
    private String message;
    private String data;

    public AdminDashboardResponse(String message, String data) {
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
