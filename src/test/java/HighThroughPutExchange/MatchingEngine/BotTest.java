package HighThroughPutExchange.MatchingEngine;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BotTest {
    private static final int MAX_ACTIONS = 1000;
    private static final int CHECK_EVERY_N_ACTIONS = 50;

    @Test
    void testRandomOrdersConserveTotalBalance() {
        MatchingEngine matchingEngine = new MatchingEngine();
        ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
        List<String> users = Arrays.asList("User1", "User2");
        String ticker = "AAPL";

        matchingEngine.initializeTicker(ticker);
        matchingEngine.initializeUserBalance("User1", 10000);
        matchingEngine.initializeUserBalance("User2", 10000);
        matchingEngine.initializeUserTickerVolume("User1", ticker, 100);
        matchingEngine.initializeUserTickerVolume("User2", ticker, 100);

        for (int i = 0; i < 2; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        int price = 100 + new Random().nextInt(51); // Random int price between 100 and 150
                        int volume = 1 + new Random().nextInt(10); // Random int volume between 1 and 10
                        double rng = Math.random();
                        if (rng < 0.25) {
                            String randomUser = users.get(new Random().nextInt(users.size()));
                            // Add a bid order to the queue
                            queue.add(() -> {
                                Order bidOrder = new Order(randomUser, ticker, price, volume, Side.BID, Status.ACTIVE);
                                System.out.println("Bid Order Added: " + bidOrder);
                                matchingEngine.bidLimitOrder(randomUser, bidOrder);
                                // System.out.println("Bid Order Status: " + bidOrder.getStatus());
                            });
                        } else if (rng < 0.5) {
                            // Add an ask order to the queue
                            queue.add(() -> {
                                String randomUser = users.get(new Random().nextInt(users.size()));
                                Order askOrder = new Order(randomUser, ticker, price, volume, Side.ASK, Status.ACTIVE);
                                System.out.println("Ask Order Added: " + askOrder);
                                matchingEngine.askLimitOrder(randomUser, askOrder);
                            });
                        } else if (rng < 0.75) {
                            queue.add(() -> {
                                String randomUser = users.get(new Random().nextInt(users.size()));
                                int quantity = 1 + new Random().nextInt(10);
                                matchingEngine.bidMarketOrder(randomUser, ticker, quantity);
                            });
                        } else {
                            queue.add(() -> {
                                String randomUser = users.get(new Random().nextInt(users.size()));
                                int quantity = 1 + new Random().nextInt(10);
                                matchingEngine.askMarketOrder(randomUser, ticker, quantity);
                            });
                        }

                        try {
                            Thread.sleep(00); // Delay to simulate real-time trading
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        int count = 0;
        while (count <= MAX_ACTIONS) {
            while (!queue.isEmpty()) {
                count++;
                Runnable action = queue.poll();
                if (action != null) {
                    // System.out.println(action);
                    action.run();
                    if (count % CHECK_EVERY_N_ACTIONS == 0) {
                        matchingEngine.removeAll("User1");
                        matchingEngine.removeAll("User2");

                        // System.out.println("Reset Occuring");

                        int balance1 = matchingEngine.getUserBalance("User1");
                        int balance2 = matchingEngine.getUserBalance("User2");
                        int total = balance1 + balance2;

                        if (total > 20001) {
                            Assertions.fail("Money created out of nowhere. " + "Expected total balance of 20000 after resets, but got total=" + total
                                    + " (User1 Balance =" + balance1 + ", User2 Balance =" + balance2 + ")." + " (User1 Owned ="
                                    + matchingEngine.getTickerBalance("User1", ticker) + ", User2 Owned ="
                                    + matchingEngine.getTickerBalance("User2", ticker) + ").");
                        }

                        if (total < 19999) {
                            Assertions.fail("Money destroyed. " + "Expected total balance of 20000 after resets, but got total=" + total
                                    + " (User1 Balance =" + balance1 + ", User2 Balance =" + balance2 + ")." + " (User1 Owned ="
                                    + matchingEngine.getTickerBalance("User1", ticker) + ", User2 Owned ="
                                    + matchingEngine.getTickerBalance("User2", ticker) + ").");
                        }
                    }
                }

                if (count > MAX_ACTIONS) {
                    break;
                }
            }
        }
    }
}
