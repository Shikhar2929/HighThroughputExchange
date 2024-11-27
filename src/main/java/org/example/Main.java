package org.example;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {
    public static void main(String[] args) {
        // MatchingEngine matchingEngine = new MatchingEngine();
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

        // spin up 10 threads
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        queue.add("meow meow meow");
                    }
                }
            }).start();
        }

        // Counter to track the number of messages processed
        long[] counter = {0};

        // Thread to print the count every second
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        System.out.println("Messages processed in the last second: " + counter[0]);
                        counter[0] = 0; // Reset the counter
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        while (true) {
            while (!queue.isEmpty()) {
                String message = queue.poll();
                // System.out.println(message);
                counter[0]++;
            }
        }
    }

}