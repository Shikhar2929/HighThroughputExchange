#include "Engine.h"
int main() {
    MatchingEngine matchEngine;
    matchEngine.insertBid("Jack", 1.0, 100.0);
    matchEngine.display();
    matchEngine.insertBid("Rohan", 1.5, 300);
    matchEngine.display();
    matchEngine.insertBid("Shikhar", 1.5, 300);
    matchEngine.display();
}