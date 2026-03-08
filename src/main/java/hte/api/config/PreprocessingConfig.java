package hte.api.config;

import hte.api.dtos.requests.Preprocessing;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PreprocessingConfig {

    @Value("${hte.order.max.price}")
    private int maxPrice;

    @Value("${hte.order.min.price}")
    private int minPrice;

    @Value("${hte.order.max.volume}")
    private int maxVolume;

    @Value("${hte.order.min.volume}")
    private int minVolume;

    @PostConstruct
    public void init() {
        Preprocessing.configure(maxPrice, minPrice, maxVolume, minVolume);
        System.err.println(
                "PreprocessingConfig initialized with: maxPrice="
                        + maxPrice
                        + ", minPrice="
                        + minPrice
                        + ", maxVolume="
                        + maxVolume
                        + ", minVolume="
                        + minVolume);
    }
}
