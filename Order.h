#include "bits/stdc++.h"
enum SIDE {
    BID, 
    ASK
};
enum STATUS {
    ACTIVE, 
    FILLED, 
    CANCELLED
};
struct Order {
    std::string name;
    double price;
    double volume;
    SIDE side;
    STATUS status;   
};