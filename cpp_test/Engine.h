#include "Order.h"
#ifndef MATCHING_ENGINE 
#define MATCHING_ENGINE
class MatchingEngine {
    public: 
        MatchingEngine() : orderID(0) {}
        double getHighestBid();
        double getLowestAsk();
        long long bidLimitOrder(std::string name, Order order);
        long long askLimitOrder(std::string name, Order order);
        void display();
        #ifdef TESTING
        const std::map<double, std::deque<Order>>& getAsks() const { return asks; }
        const std::map<double, std::deque<Order>>& getBids() const { return bids; }
        #endif

    private: 
        std::map<double, std::deque<Order>> bids;
        std::map<double, std::deque<Order>> asks;
        std::unordered_map<long long, Order> orderMap;
        long long orderID = 0;
        void processBid(std::deque<Order>& orders, Order& aggressor);
        void processAsk(std::deque<Order>& orders, Order& aggressor);
};
#endif