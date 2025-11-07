package HighThroughPutExchange.API.auth;

import static org.junit.jupiter.api.Assertions.*;

import HighThroughPutExchange.API.api_objects.requests.BaseAdminRequest;
import HighThroughPutExchange.API.authentication.AdminPageAuthenticator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AdminPageAuthenticatorTest {
    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Test
    void authenticatesWithCorrectCredentials() {
        AdminPageAuthenticator auth = AdminPageAuthenticator.getInstance();
        BaseAdminRequest req = new BaseAdminRequest(adminUsername, adminPassword);
        assertTrue(auth.authenticate(req));
    }

    @Test
    void rejectsWrongCredentials() {
        AdminPageAuthenticator auth = AdminPageAuthenticator.getInstance();
        BaseAdminRequest req = new BaseAdminRequest("wrong", "nope");
        assertFalse(auth.authenticate(req));
    }
}
