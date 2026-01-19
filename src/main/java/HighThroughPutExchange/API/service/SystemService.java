package HighThroughPutExchange.api.service;

import HighThroughPutExchange.matchingengine.MatchingEngine;
import org.springframework.stereotype.Service;

@Service
public class SystemService {
    private final MatchingEngine matchingEngine;

    public SystemService(MatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    public String getUserDetails(String username) {
        return matchingEngine.getUserDetails(username);
    }
}
