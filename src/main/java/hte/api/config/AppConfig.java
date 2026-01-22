package hte.api.config;

import hte.auction.Auction;
import hte.matchingengine.MatchingEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    MatchingEngine matchingEngine(@Value("${hte.config.path:config.json}") String configLocation) {
        MatchingEngine engine = new MatchingEngine(true, configLocation);
        engine.initializeAllTickers();
        return engine;
    }

    @Bean
    Auction auction(MatchingEngine matchingEngine) {
        return new Auction(matchingEngine);
    }
}
