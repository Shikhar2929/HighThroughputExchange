package hte.common;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskQueue {
    private static final ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

    public static void addTask(Runnable task) {
        taskQueue.add(task);
    }

    public static Runnable getNextTask() {
        return taskQueue.poll();
    }

    public static boolean isEmpty() {
        return taskQueue.isEmpty();
    }
}
