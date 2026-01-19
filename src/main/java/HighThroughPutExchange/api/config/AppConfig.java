package HighThroughPutExchange.api.config;

import HighThroughPutExchange.auction.Auction;
import HighThroughPutExchange.common.MatchingEngineSingleton;
import HighThroughPutExchange.matchingengine.MatchingEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public MatchingEngine matchingEngine() {
        MatchingEngine engine = MatchingEngineSingleton.getMatchingEngine();
        engine.initializeAllTickers();
        return engine;
    }

    @Bean
    public Auction auction(MatchingEngine matchingEngine) {
        return new Auction(matchingEngine);
    }
}
