package HighThroughPutExchange.API.authentication;

import HighThroughPutExchange.API.api_objects.requests.BaseAdminRequest;
import org.springframework.stereotype.Component;

@Component
public class AdminPageAuthenticator {
    // todo read values from env variables
    private final String adminUsername = "trading_club_admin";
    private final String adminPassword = "ZY3yoQL5v8MahcmcWBnG";

    public boolean authenticate(BaseAdminRequest req) {
        return req.getAdminUsername().equals(adminUsername) && req.getAdminPassword().equals(adminPassword);
    }
}
