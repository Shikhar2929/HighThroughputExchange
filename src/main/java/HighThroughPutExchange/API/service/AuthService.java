package HighThroughPutExchange.API.service;

import HighThroughPutExchange.API.api_objects.requests.BaseAdminRequest;
import HighThroughPutExchange.API.api_objects.requests.BasePrivateRequest;
import HighThroughPutExchange.API.authentication.AdminPageAuthenticator;
import HighThroughPutExchange.API.authentication.BotAuthenticator;
import HighThroughPutExchange.API.authentication.PrivatePageAuthenticator;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public boolean authenticatePrivate(BasePrivateRequest form) {
        return PrivatePageAuthenticator.getInstance().authenticate(form);
    }

    public boolean authenticateBot(BasePrivateRequest form) {
        return BotAuthenticator.getInstance().authenticate(form);
    }

    public boolean authenticateAdmin(BaseAdminRequest form) {
        return AdminPageAuthenticator.getInstance().authenticate(form);
    }
}
