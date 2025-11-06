package HighThroughPutExchange.Common;

import HighThroughPutExchange.MatchingEngine.MatchingEngine;

public class MatchingEngineSingleton {
    private static MatchingEngine matchingEngine;

    public static MatchingEngine getMatchingEngine() {
        if (matchingEngine == null) {
            synchronized (MatchingEngineSingleton.class) {
                if (matchingEngine == null) {
                    matchingEngine = new MatchingEngine(true);
                }
            }
        }
        return matchingEngine;
    }
}
