#include "Order.h"
#ifndef MATCHING_ENGINE 
#define MATCHING_ENGINE
class MatchingEngine {
    public: 
        MatchingEngine() : orderID(0) {}
        double getHighestBid();
        double getLowestAsk();
        long long bidLimitOrder(std::string name, double price, double volume);
        long long askLimitOrder(std::string name, double price, double volume);
        void display();
    private: 
        std::map<double, std::deque<Order>> bids;
        std::map<double, std::deque<Order>> asks;
        std::unordered_map<long long, Order> orderMap;
        long long orderID = 0;
};
#endif