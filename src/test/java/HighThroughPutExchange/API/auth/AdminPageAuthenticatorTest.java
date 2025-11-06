package HighThroughPutExchange.API.auth;

import HighThroughPutExchange.API.api_objects.requests.BaseAdminRequest;
import HighThroughPutExchange.API.authentication.AdminPageAuthenticator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AdminPageAuthenticatorTest {

    @Test
    void authenticatesWithCorrectCredentials() {
        AdminPageAuthenticator auth = AdminPageAuthenticator.getInstance();
        BaseAdminRequest req = new BaseAdminRequest("trading_club_admin", "ZY3yoQL5v8MahcmcWBnG");
        assertTrue(auth.authenticate(req));
    }

    @Test
    void rejectsWrongCredentials() {
        AdminPageAuthenticator auth = AdminPageAuthenticator.getInstance();
        BaseAdminRequest req = new BaseAdminRequest("wrong", "nope");
        assertFalse(auth.authenticate(req));
    }
}
