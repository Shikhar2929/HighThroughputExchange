#include "Engine.h"


long long MatchingEngine::insertBid(std::string name, double price, double volume) {
    bids[price].push_back({name, price, volume, BID, ACTIVE });
    return ++orderID;
}
void MatchingEngine::display() {
    std::cout << "BID ---- \n";
    for (const auto& [price, listBid] : bids) {
        for (const auto& bid : listBid) {
            std::cout << bid.name << " " << bid.price << " " << bid.volume << std::endl;
        }
    }
}