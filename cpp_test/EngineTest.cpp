#include <iostream>
#include <string>
#include <cmath>  
#include "Engine.h"

void runTest(const std::string& testName, bool (*testFunction)()) {
    if (testFunction()) {
        std::cout << testName << ": SUCCESS\n";
    } else {
        std::cout << testName << ": FAILURE\n";
    }
}
bool almostEqual(double a, double b, double tolerance = 1e-6) {
    return std::fabs(a - b) < tolerance;
}
bool testBidLimitOrder_AddsBidSuccessfully() {
    MatchingEngine engine;
    Order bidOrder = {"TraderA", 100.0, 10.0, BID, ACTIVE};

    long long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
    if (orderId <= 0) return false;  // Check that orderId is valid
    // Check that the bid was added to the bids map at the correct price level
    #ifdef TESTING
    const auto& bids = engine.getBids();
    if (bids.find(bidOrder.price) == bids.end()) return false;
    return bids.at(bidOrder.price).front().volume == bidOrder.volume &&
           bids.at(bidOrder.price).front().name == bidOrder.name;
    #endif
    return true;
}

// Test for placing an ask order
bool testAskLimitOrder_AddsAskSuccessfully() {
    MatchingEngine engine;
    Order askOrder = {"TraderB", 105.0, 15.0, ASK, ACTIVE};

    long long orderId = engine.askLimitOrder(askOrder.name, askOrder);
    if (orderId <= 0) return false;

    #ifdef TESTING
    const auto& asks = engine.getAsks();
    if (asks.find(askOrder.price) == asks.end()) return false;
    return asks.at(askOrder.price).front().volume == askOrder.volume &&
           asks.at(askOrder.price).front().name == askOrder.name;
    #else
    return true;
    #endif
}

// Test for getting the highest bid after multiple bids
bool testGetHighestBid_AfterMultipleBids() {
    MatchingEngine engine;
    engine.bidLimitOrder("TraderA", {"TraderA", 100.0, 10.0, BID, ACTIVE});
    engine.bidLimitOrder("TraderB", {"TraderB", 105.0, 5.0, BID, ACTIVE});
    return almostEqual(engine.getHighestBid(), 105.0);
}

// Test for getting the lowest ask after multiple asks
bool testGetLowestAsk_AfterMultipleAsks() {
    MatchingEngine engine;
    engine.askLimitOrder("TraderA", {"TraderA", 110.0, 10.0, ASK, ACTIVE});
    engine.askLimitOrder("TraderB", {"TraderB", 105.0, 5.0, ASK, ACTIVE});
    return almostEqual(engine.getLowestAsk(), 105.0);
}

// Test for matching bid and ask orders
bool testMatchingBidAndAskOrders() {
    MatchingEngine engine;
    engine.bidLimitOrder("TraderA", {"TraderA", 105.0, 10.0, BID, ACTIVE});
    engine.askLimitOrder("TraderB", {"TraderB", 105.0, 10.0, ASK, ACTIVE});
    return almostEqual(engine.getHighestBid(), 0.0) && almostEqual(engine.getLowestAsk(), 0.0);
}

bool testInsertBid() {
    MatchingEngine engine;
    Order bidOrder = {"TraderA", 100.0, 10.0, BID, ACTIVE};

    long long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
    assert(orderId > 0);

    #ifdef TESTING
    const auto& bids = engine.getBids();
    return bids.contains(100.0) && bids.at(100.0).front().volume == 10.0;
    #else
    return true;
    #endif
}

// Test case for inserting an ask
bool testInsertAsk() {
    MatchingEngine engine;
    Order askOrder = {"TraderB", 100.0, 5.0, ASK, ACTIVE};

    long long orderId = engine.askLimitOrder(askOrder.name, askOrder);
    assert(orderId > 0);

    #ifdef TESTING
    const auto& asks = engine.getAsks();
    return asks.contains(100.0) && asks.at(100.0).front().volume == 5.0;
    #else
    return true;
    #endif
}

// Test case for a complete order fill
bool testFillOrderCompletely() {
    MatchingEngine engine;
    Order askOrder = {"TraderA", 100.0, 5.0, ASK, ACTIVE};
    Order bidOrder = {"TraderB", 100.0, 5.0, BID, ACTIVE};

    engine.askLimitOrder(askOrder.name, askOrder);
    long long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);

    #ifdef TESTING
    return engine.getAsks().empty() && engine.getBids().empty();
    #else
    return true;
    #endif
}

// Test case for partial order fill
bool testPartialFill() {
    MatchingEngine engine;
    Order askOrder = {"TraderA", 100.0, 5.0, ASK, ACTIVE};
    Order bidOrder = {"TraderB", 100.0, 10.0, BID, ACTIVE};

    engine.askLimitOrder(askOrder.name, askOrder);
    long long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);

    #ifdef TESTING
    return engine.getAsks().empty() && 
           engine.getBids().contains(100.0) && 
           engine.getBids().at(100.0).front().volume == 5.0;
    #else
    return true;
    #endif
}

// Test case for race condition on asks
bool testRaceCondition() {
    MatchingEngine engine;
    Order ask1 = {"TraderA", 100.0, 5.0, ASK, ACTIVE};
    Order ask2 = {"TraderB", 100.0, 7.0, ASK, ACTIVE};
    Order bid = {"TraderC", 100.0, 6.0, BID, ACTIVE};

    engine.askLimitOrder(ask1.name, ask1);
    engine.askLimitOrder(ask2.name, ask2);
    long long orderId = engine.bidLimitOrder(bid.name, bid);

    #ifdef TESTING
    const auto& asks = engine.getAsks();
    return asks.contains(100.0) && asks.at(100.0).front().volume == 6.0;
    #else
    return true;
    #endif
}

// Test case for non-matching bid and ask prices
bool testDifferentPricesNoMatch() {
    MatchingEngine engine;
    Order bid = {"TraderA", 95.0, 10.0, BID, ACTIVE};
    Order ask = {"TraderB", 105.0, 5.0, ASK, ACTIVE};

    engine.bidLimitOrder(bid.name, bid);
    engine.askLimitOrder(ask.name, ask);

    #ifdef TESTING
    const auto& bids = engine.getBids();
    const auto& asks = engine.getAsks();
    return bids.contains(95.0) && asks.contains(105.0);
    #else
    return true;
    #endif
}



int main() {
    runTest("BidLimitOrder_AddsBidSuccessfully", testBidLimitOrder_AddsBidSuccessfully);
    runTest("AskLimitOrder_AddsAskSuccessfully", testAskLimitOrder_AddsAskSuccessfully);
    runTest("GetHighestBid_AfterMultipleBids", testGetHighestBid_AfterMultipleBids);
    runTest("GetLowestAsk_AfterMultipleAsks", testGetLowestAsk_AfterMultipleAsks);
    runTest("MatchingBidAndAskOrders", testMatchingBidAndAskOrders);
    runTest("testInsertBid", testInsertBid);
    runTest("testInsertAsk", testInsertAsk);
    runTest("testFillOrderCompletely", testFillOrderCompletely);
    runTest("testPartialFill", testPartialFill);
    runTest("testRaceCondition", testRaceCondition);
    runTest("testDifferentPricesNoMatch", testDifferentPricesNoMatch);
}
