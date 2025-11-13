package HighThroughPutExchange.API.config;

import HighThroughPutExchange.Auction.Auction;
import HighThroughPutExchange.Common.MatchingEngineSingleton;
import HighThroughPutExchange.MatchingEngine.MatchingEngine;
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
