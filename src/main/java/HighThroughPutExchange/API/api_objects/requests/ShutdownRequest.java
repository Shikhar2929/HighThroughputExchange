package HighThroughPutExchange.API.api_objects.requests;

import HighThroughPutExchange.API.api_objects.requests.BaseAdminRequest;

public class ShutdownRequest extends BaseAdminRequest {
    public ShutdownRequest(String adminUsername, String adminPassword) {
        super(adminUsername, adminPassword);
    }
}
