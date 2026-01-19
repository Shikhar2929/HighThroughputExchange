package HighThroughPutExchange.common;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class SeqGenerator {
    private final AtomicLong currentSeq = new AtomicLong(0);

    @PostConstruct
    public void init() {
        currentSeq.set(0);
    }

    public long getAndIncrement() {
        return currentSeq.getAndIncrement();
    }

    public long get() {
        return currentSeq.get();
    }

    public void reset() {
        currentSeq.set(0);
    }
}
