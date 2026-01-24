package hte.api.auth;

import hte.api.dtos.requests.BaseAdminRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminPageAuthenticator {
    private final String adminUsername;
    private final String adminPassword;

    public AdminPageAuthenticator(
            @Value("${hte.admin.username}") String adminUsername,
            @Value("${hte.admin.password}") String adminPassword) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    public boolean authenticate(BaseAdminRequest req) {
        return req.getAdminUsername().equals(adminUsername)
                && req.getAdminPassword().equals(adminPassword);
    }
}
