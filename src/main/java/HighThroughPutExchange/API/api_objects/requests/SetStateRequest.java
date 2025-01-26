package HighThroughPutExchange.API.api_objects.requests;

public class SetStateRequest extends BaseAdminRequest {
    private int targetState;
    public SetStateRequest(String adminUsername, String adminPassword, int targetState) {
        super(adminUsername, adminPassword);
        this.targetState = targetState;
    }
    public int getTargetState() {return targetState;}

    public void setTargetState(int state) {
        this.targetState = targetState;
    }
}
