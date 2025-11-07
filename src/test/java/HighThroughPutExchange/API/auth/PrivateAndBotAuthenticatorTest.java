package HighThroughPutExchange.API.auth;

import static org.junit.jupiter.api.Assertions.*;

import HighThroughPutExchange.API.api_objects.requests.BasePrivateRequest;
import HighThroughPutExchange.API.authentication.BotAuthenticator;
import HighThroughPutExchange.API.authentication.PrivatePageAuthenticator;
import HighThroughPutExchange.API.database_objects.Session;
import HighThroughPutExchange.Database.localdb.LocalDBTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PrivateAndBotAuthenticatorTest {

    private LocalDBTable<Session> sessions;

    @BeforeEach
    void setup() {
        sessions = new LocalDBTable<>("sessions");
        PrivatePageAuthenticator.buildInstance(sessions);
        BotAuthenticator.buildInstance(sessions);
    }

    @Test
    void privateAuth_acceptsEitherToken() {
        Session s = new Session("t1", "t2", "alice");
        sessions.getBacking().put("alice", s);

        BasePrivateRequest r1 = new BasePrivateRequest("alice", "t1");
        BasePrivateRequest r2 = new BasePrivateRequest("alice", "t2");
        BasePrivateRequest r3 = new BasePrivateRequest("alice", "bad");

        assertTrue(PrivatePageAuthenticator.getInstance().authenticate(r1));
        assertTrue(PrivatePageAuthenticator.getInstance().authenticate(r2));
        assertFalse(PrivatePageAuthenticator.getInstance().authenticate(r3));
    }

    @Test
    void botAuth_acceptsOnlyPrimaryToken() {
        Session s = new Session("t1", "t2", "bot1");
        sessions.getBacking().put("bot1", s);

        BasePrivateRequest r1 = new BasePrivateRequest("bot1", "t1");
        BasePrivateRequest r2 = new BasePrivateRequest("bot1", "t2");
        BasePrivateRequest r3 = new BasePrivateRequest("bot1", "bad");

        assertTrue(BotAuthenticator.getInstance().authenticate(r1));
        assertFalse(BotAuthenticator.getInstance().authenticate(r2));
        assertFalse(BotAuthenticator.getInstance().authenticate(r3));
    }

    @Test
    void missingUser_failsAuthentication() {
        BasePrivateRequest req = new BasePrivateRequest("ghost", "t1");
        assertFalse(PrivatePageAuthenticator.getInstance().authenticate(req));
        assertFalse(BotAuthenticator.getInstance().authenticate(req));
    }
}
