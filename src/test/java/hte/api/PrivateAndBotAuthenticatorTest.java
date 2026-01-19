package hte.api;

import static org.junit.jupiter.api.Assertions.*;

import hte.api.auth.BotAuthenticator;
import hte.api.auth.PrivatePageAuthenticator;
import hte.api.dtos.requests.BasePrivateRequest;
import hte.api.entities.Session;
import hte.api.repository.BotSessionsRepository;
import hte.api.repository.LocalBotSessionsRepository;
import hte.api.repository.LocalSessionsRepository;
import hte.api.repository.SessionsRepository;
import hte.database.localdb.LocalDBTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PrivateAndBotAuthenticatorTest {

    private LocalDBTable<Session> sessions;
    private SessionsRepository sessionsRepository;
    private BotSessionsRepository botSessionsRepository;
    private PrivatePageAuthenticator privatePageAuthenticator;
    private BotAuthenticator botAuthenticator;

    @BeforeEach
    void setup() {
        sessions = new LocalDBTable<>("sessions");
        sessionsRepository = new LocalSessionsRepository(sessions);
        botSessionsRepository = new LocalBotSessionsRepository(sessions);
        privatePageAuthenticator = new PrivatePageAuthenticator(sessionsRepository);
        botAuthenticator = new BotAuthenticator(botSessionsRepository);
    }

    @Test
    void privateAuth_acceptsEitherToken() {
        Session s = new Session("t1", "t2", "alice");
        sessions.getBacking().put("alice", s);

        BasePrivateRequest r1 = new BasePrivateRequest("alice", "t1");
        BasePrivateRequest r2 = new BasePrivateRequest("alice", "t2");
        BasePrivateRequest r3 = new BasePrivateRequest("alice", "bad");

        assertTrue(privatePageAuthenticator.authenticate(r1));
        assertTrue(privatePageAuthenticator.authenticate(r2));
        assertFalse(privatePageAuthenticator.authenticate(r3));
    }

    @Test
    void botAuth_acceptsOnlyPrimaryToken() {
        Session s = new Session("t1", "t2", "bot1");
        sessions.getBacking().put("bot1", s);

        BasePrivateRequest r1 = new BasePrivateRequest("bot1", "t1");
        BasePrivateRequest r2 = new BasePrivateRequest("bot1", "t2");
        BasePrivateRequest r3 = new BasePrivateRequest("bot1", "bad");

        assertTrue(botAuthenticator.authenticate(r1));
        assertFalse(botAuthenticator.authenticate(r2));
        assertFalse(botAuthenticator.authenticate(r3));
    }

    @Test
    void missingUser_failsAuthentication() {
        BasePrivateRequest req = new BasePrivateRequest("ghost", "t1");
        assertFalse(privatePageAuthenticator.authenticate(req));
        assertFalse(botAuthenticator.authenticate(req));
    }
}
