package HighThroughPutExchange.api.dtos.requests;

public class ShutdownRequest extends BaseAdminRequest {
    public ShutdownRequest(String adminUsername, String adminPassword) {
        super(adminUsername, adminPassword);
    }
}
