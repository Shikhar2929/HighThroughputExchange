package HighThroughPutExchange.API.api_objects.responses;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class SetStateResponse {
    @JsonRawValue
    private String message;
    private int newState;
    public SetStateResponse(String message, int newState) {
        this.message = message;
        this.newState = newState;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setNewState(int newState) {this.newState = newState;}
    public int getNewState() {return newState;}
}
