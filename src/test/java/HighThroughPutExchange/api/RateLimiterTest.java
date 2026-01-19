package HighThroughPutExchange.api;

import static org.junit.jupiter.api.Assertions.*;

import HighThroughPutExchange.api.auth.RateLimiter;
import HighThroughPutExchange.api.dtos.requests.BasePrivateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RateLimiterTest {
    @Test
    void allowsUpTo15RequestsPerSecond_thenBlocks() {
        RateLimiter limiter = new RateLimiter();
        BasePrivateRequest req = new BasePrivateRequest("alice", "token");

        // First 15 should pass
        for (int i = 0; i < 15; i++) {
            assertTrue(limiter.processRequest(req), "request " + i + " should pass");
        }
        // 16th within same second should be blocked
        assertFalse(limiter.processRequest(req));
    }
}
