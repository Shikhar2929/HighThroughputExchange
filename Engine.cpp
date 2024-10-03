#include "Engine.h"

double MatchingEngine::processBid(deque<Order>& orders, Order& aggressor) {
    while (volume && orders.size()) {
        Order& order = deque.front();
        if (order.volume > aggressor.volume) {
            //MISSING SEND TRADE
            order.volume -= volume;
            aggressor.volume = 0; 
        }
        else {
            // MISSING SEND TRADE INFO
            aggressor.volume -= order.volume;
            order.volume = 0;
            order.status = FILLED;
        }
    }
}

long long MatchingEngine::bidLimitOrder(std::string name, Order order) {
    while (volume && asks.rbegin()->first <= price) {
        deque<Order>& orders = asks.rbegin()->second;
        order.volume = processBid(orders, order);
        if (!orders.size()) {
            asks.remove(asks.rbegin());
        }
    }
    if (volume) {
        bids[price].push_back(order);
    }
    return ++orderID;
}

long long MatchingEngine::askLimitOrder(std::string name, double price, double volume) {
    while (volume && bid.begin()->first >= price) {
        deque<Order>& orders = asks.rbegin()->second;
        volume = processBid(orders, volume);
        if (!orders.size()) {
            asks.remove(asks.rbegin());
        }
    }
    if (volume) {
        asks[price].push_back({name, price, volume, BID, ACTIVE });
    }
    return ++orderID;
}
long long MatchingEngine::ask

void MatchingEngine::display() {
    std::cout << "BID ---- \n";
    for (const auto& [price, listBid] : bids) {
        for (const auto& bid : listBid) {
            std::cout << bid.name << " " << bid.price << " " << bid.volume << std::endl;
        }
    }
}