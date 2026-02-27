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
        AuctionResult result = auction.getAuctionResult();
        assertEquals(500, result.getBid());
        assertEquals("alice", result.getUser());
    }

    @Test
    void placeBid_multipleUsers_highestWins() {
        auction.placeBid("alice", 300);
        auction.placeBid("bob", 500);
        auction.placeBid("charlie", 400);

        AuctionResult result = auction.getAuctionResult();
        assertEquals(500, result.getBid());
        assertEquals("bob", result.getUser());
    }

    @Test
    void placeBid_sameUserMultipleTimes_takesMostRecent() {
        auction.placeBid("alice", 900);
        auction.placeBid("alice", 100);

        AuctionResult result = auction.getAuctionResult();
        assertEquals(100, result.getBid());
        assertEquals("alice", result.getUser());
    }

    @Test
    void placeBid_sameUserLowersBid_anotherUserWins() {
        auction.placeBid("alice", 500);
        auction.placeBid("bob", 300);
        auction.placeBid("alice", 200);

        AuctionResult result = auction.getAuctionResult();
        assertEquals(300, result.getBid());
        assertEquals("bob", result.getUser());
    }

    @Test
    void placeBid_sameUserRaisesBid_updatesCorrectly() {
        auction.placeBid("alice", 100);
        auction.placeBid("alice", 800);

        AuctionResult result = auction.getAuctionResult();
        assertEquals(800, result.getBid());
        assertEquals("alice", result.getUser());
    }

    @Test
    void getAuctionResult_noBids_returnsDefaults() {
        AuctionResult result = auction.getAuctionResult();
        assertEquals(0, result.getBid());
        assertEquals("", result.getUser());
    }

    @Test
    void reset_clearsBids() {
        auction.placeBid("alice", 500);
        auction.placeBid("bob", 300);
        auction.reset();

        AuctionResult result = auction.getAuctionResult();
        assertEquals(0, result.getBid());
        assertEquals("", result.getUser());
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
