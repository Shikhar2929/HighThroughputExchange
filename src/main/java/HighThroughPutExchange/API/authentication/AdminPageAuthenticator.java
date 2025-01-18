package HighThroughPutExchange.API.authentication;

import HighThroughPutExchange.API.api_objects.requests.BaseAdminRequest;

public class AdminPageAuthenticator {
    // todo read values from env variables
    private static AdminPageAuthenticator instance;
    private final String adminUsername = "trading_club_admin";
    private final String adminPassword = "abcxyz";

    public AdminPageAuthenticator() {}

//    public static AdminPageAuthenticator getInstance() {
//        if (instance == null) {
//            instance = new AdminPageAuthenticator();
//        }
//        return instance;
//    }

    public boolean authenticate(BaseAdminRequest req) {
        return req.getAdminUsername().equals(adminUsername) && req.getAdminPassword().equals(adminPassword);
    }
}
