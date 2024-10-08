#include "Engine.h"

void MatchingEngine::processBid(std::deque<Order>& orders, Order& aggressor) {
    while (aggressor.volume && orders.size()) {
        Order& order = orders.front();
        if (order.volume > aggressor.volume) {
            //MISSING SEND TRADE
            order.volume -= aggressor.volume;
            aggressor.volume = 0; 
            aggressor.status = FILLED;
        }
        else {
            // MISSING SEND TRADE INFO
            aggressor.volume -= order.volume;
            order.volume = 0;
            order.status = FILLED;
            orders.pop_front();
        }
    }
}
void MatchingEngine::processAsk(std::deque<Order>& orders, Order& aggressor) {
    while (aggressor.volume && orders.size()) {
        Order& order = orders.front();
        if (order.volume > aggressor.volume) {
            //MISSING SEND TRADE
            order.volume -= aggressor.volume;
            aggressor.volume = 0; 
            aggressor.status = FILLED;
        }
        else {
            // MISSING SEND TRADE INFO
            aggressor.volume -= order.volume;
            order.volume = 0;
            order.status = FILLED;
            orders.pop_front();
        }
    }
}

long long MatchingEngine::bidLimitOrder(std::string name, Order order) {
    while (order.volume && asks.size() && asks.begin()->first <= order.price) {
        std::deque<Order>& orderList = asks.begin()->second;
        processBid(orderList, order);
        if (!orderList.size()) {
            asks.erase(asks.begin());
        }
    }
    if (order.volume) {
        order.status = ACTIVE;
        bids[order.price].push_back(order);
        return ++orderID;
    }
    return 0;
}

long long MatchingEngine::askLimitOrder(std::string name, Order order) {
    while (order.volume && bids.size() && bids.rbegin()->first >= order.price) {
        std::deque<Order>& orderList = bids.rbegin()->second;
        processAsk(orderList, order);
        if (!orderList.size()) {
            bids.erase(std::prev(bids.end()));
        }
    }
    if (order.volume) {
        order.status = ACTIVE;
        asks[order.price].push_back(order);
        return ++orderID;
    }
    return 0;
}
double MatchingEngine::getHighestBid() {
        if (bids.empty()) {
            return 0.0;
        }
        return bids.rbegin()->first;
}
double MatchingEngine:: getLowestAsk() {
        if (asks.empty()) {
            return 0.0;
        }
        return asks.begin()->first;
    }
void MatchingEngine::display() {
    std::cout << "BID ---- \n";
    for (const auto& [price, listBid] : bids) {
        for (const auto& bid : listBid) {
            std::cout << bid.name << " " << bid.price << " " << bid.volume << std::endl;
        }
    }
}