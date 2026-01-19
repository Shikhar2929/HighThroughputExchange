package HighThroughPutExchange.api.auth;

import HighThroughPutExchange.api.dtos.requests.BaseAdminRequest;
import org.springframework.stereotype.Component;

@Component
public class AdminPageAuthenticator {
    private final String adminUsername = "trading_club_admin";
    private final String adminPassword = "ZY3yoQL5v8MahcmcWBnG";

    public boolean authenticate(BaseAdminRequest req) {
        return req.getAdminUsername().equals(adminUsername)
                && req.getAdminPassword().equals(adminPassword);
    }
}
