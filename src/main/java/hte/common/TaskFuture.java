package hte.common;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TaskFuture<T> {
    private boolean completed;
    private ReentrantLock mutex;
    private Condition cv;
    private T data;

    public TaskFuture() {
        completed = false;
        mutex = new ReentrantLock();
        cv = mutex.newCondition();
    }

    public void waitForCompletion() {
        mutex.lock();
        while (!completed) {
            try {
                cv.await();
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }
        mutex.unlock();
    }

    public void markAsComplete() {
        mutex.lock();
        completed = true;
        cv.signalAll();
        mutex.unlock();
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
