package HighThroughPutExchange.api.service;

import HighThroughPutExchange.api.auth.AdminPageAuthenticator;
import HighThroughPutExchange.api.auth.BotAuthenticator;
import HighThroughPutExchange.api.auth.PrivatePageAuthenticator;
import HighThroughPutExchange.api.dtos.requests.BaseAdminRequest;
import HighThroughPutExchange.api.dtos.requests.BasePrivateRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PrivatePageAuthenticator privatePageAuthenticator;
    private final BotAuthenticator botAuthenticator;
    private final AdminPageAuthenticator adminPageAuthenticator;

    public AuthService(
            PrivatePageAuthenticator privatePageAuthenticator,
            BotAuthenticator botAuthenticator,
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
