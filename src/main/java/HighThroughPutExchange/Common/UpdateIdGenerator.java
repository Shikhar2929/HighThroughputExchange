package HighThroughPutExchange.Common;

import java.util.concurrent.atomic.AtomicLong;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class UpdateIdGenerator {
    private final AtomicLong currentUpdateId = new AtomicLong(0);

    @PostConstruct
    public void init() {
        currentUpdateId.set(0);
    }

    public long getAndIncrement() {
        return currentUpdateId.getAndIncrement();
    }

    public long get() {
        return currentUpdateId.get();
    }

    public long getErrorId() {
        return -1;
    }

    public void reset() {
        currentUpdateId.set(0);
    }
}
