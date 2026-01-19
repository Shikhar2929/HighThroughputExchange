package hte.api.config;

import hte.auction.Auction;
import hte.common.MatchingEngineSingleton;
import hte.matchingengine.MatchingEngine;
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
