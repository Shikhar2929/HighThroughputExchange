package runner;

import hte.api.ServerApplication;
import hte.common.TaskQueue;
import org.springframework.boot.SpringApplication;

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
