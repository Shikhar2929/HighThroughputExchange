package HighThroughPutExchange.API.api_objects.responses;

public class SetStateResponse extends AbstractMessageResponse {
    private int newState;
    public SetStateResponse(String message, int newState) {
        super(message);
        this.newState = newState;
    }

    public void setNewState(int newState) {this.newState = newState;}
    public int getNewState() {return newState;}
}
