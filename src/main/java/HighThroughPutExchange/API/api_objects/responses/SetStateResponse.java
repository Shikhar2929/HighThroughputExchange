package HighThroughPutExchange.API.api_objects.responses;

public class SetStateResponse {
    private int newState;
    public SetStateResponse(int newState) {
        this.newState = newState;
    }
    public void setNewState(int newState) {this.newState = newState;}
    public int getNewState() {return newState;}
}
