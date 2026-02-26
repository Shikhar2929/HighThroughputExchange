package hte.auction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hte.matchingengine.MatchingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuctionTest {
    private MatchingEngine matchingEngine;
    private Auction auction;

    @BeforeEach
    void setUp() {
        matchingEngine = new MatchingEngine(1000);
        auction = new Auction(matchingEngine);
    }

    @Test
    void placeBid_singleUser_tracksBid() {
        auction.placeBid("alice", 500);
        assertEquals(500, auction.getBestBid());
        assertEquals("alice", auction.getBestUser());
    }

    @Test
    void placeBid_multipleUsers_highestWins() {
        auction.placeBid("alice", 300);
        auction.placeBid("bob", 500);
        auction.placeBid("charlie", 400);

        assertEquals(500, auction.getBestBid());
        assertEquals("bob", auction.getBestUser());
    }

    @Test
    void placeBid_sameUserMultipleTimes_takesMostRecent() {
        auction.placeBid("alice", 900);
        auction.placeBid("alice", 100);

        assertEquals(100, auction.getBestBid());
        assertEquals("alice", auction.getBestUser());
    }

    @Test
    void placeBid_sameUserLowersBid_anotherUserWins() {
        auction.placeBid("alice", 500);
        auction.placeBid("bob", 300);
        auction.placeBid("alice", 200);

        assertEquals(300, auction.getBestBid());
        assertEquals("bob", auction.getBestUser());
    }

    @Test
    void placeBid_sameUserRaisesBid_updatesCorrectly() {
        auction.placeBid("alice", 100);
        auction.placeBid("alice", 800);

        assertEquals(800, auction.getBestBid());
        assertEquals("alice", auction.getBestUser());
    }

    @Test
    void getBestBid_noBids_returnsZero() {
        assertEquals(0, auction.getBestBid());
        assertEquals("", auction.getBestUser());
    }

    @Test
    void reset_clearsBids() {
        auction.placeBid("alice", 500);
        auction.placeBid("bob", 300);
        auction.reset();

        assertEquals(0, auction.getBestBid());
        assertEquals("", auction.getBestUser());
    }

    @Test
    void isValid_withinLimit_returnsTrue() {
        assertTrue(auction.isValid("alice", 100000));
    }

    @Test
    void isValid_exceedsLimit_returnsFalse() {
        assertFalse(auction.isValid("alice", 100001));
    }

    @Test
    void executeAuction_adjustsWinnerBalance() {
        matchingEngine.initializeUser("alice");
        assertEquals(0, matchingEngine.getUserBalance("alice"));

        auction.placeBid("alice", 750);
        auction.executeAuction();

        assertEquals(-750, matchingEngine.getUserBalance("alice"));
    }
}
