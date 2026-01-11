package HighThroughPutExchange.API.service;

import HighThroughPutExchange.API.api_objects.requests.BaseAdminRequest;
import HighThroughPutExchange.API.api_objects.requests.BasePrivateRequest;
import HighThroughPutExchange.API.authentication.AdminPageAuthenticator;
import HighThroughPutExchange.API.authentication.BotAuthenticator;
import HighThroughPutExchange.API.authentication.PrivatePageAuthenticator;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PrivatePageAuthenticator privatePageAuthenticator;
    private final BotAuthenticator botAuthenticator;
    private final AdminPageAuthenticator adminPageAuthenticator;

    public AuthService(PrivatePageAuthenticator privatePageAuthenticator, BotAuthenticator botAuthenticator,
            AdminPageAuthenticator adminPageAuthenticator) {
        this.privatePageAuthenticator = privatePageAuthenticator;
        this.botAuthenticator = botAuthenticator;
        this.adminPageAuthenticator = adminPageAuthenticator;
    }

    public boolean authenticatePrivate(BasePrivateRequest form) {
        return privatePageAuthenticator.authenticate(form);
    }

    public boolean authenticateBot(BasePrivateRequest form) {
        return botAuthenticator.authenticate(form);
    }

    public boolean authenticateAdmin(BaseAdminRequest form) {
        return adminPageAuthenticator.authenticate(form);
    }
}
