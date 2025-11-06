package Runner;

import HighThroughPutExchange.API.ServerApplication;
import HighThroughPutExchange.Common.TaskQueue;
import org.springframework.boot.SpringApplication;

// import org.springframework.context.ApplicationContext;

public class Main {
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
        while (true) {
            if (!TaskQueue.isEmpty()) {
                Runnable action = TaskQueue.getNextTask();
                if (action != null) {
                    action.run();
                    // System.out.println("Processing");
                }
            }
        }
    }
}
