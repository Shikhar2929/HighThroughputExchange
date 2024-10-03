#include "Engine.h"
int main() {
    MatchingEngine matchEngine;
    matchEngine.bidLimitOrder("Jack", 1.0, 100.0);
    matchEngine.display();
    matchEngine.bidLimitOrder("Rohan", 1.5, 300);
    matchEngine.display();
    matchEngine.bidLimitOrder("Shikhar", 1.5, 300);
    matchEngine.display();
}