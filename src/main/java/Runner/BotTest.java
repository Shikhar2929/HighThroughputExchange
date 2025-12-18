package Runner;

import HighThroughPutExchange.MatchingEngine.MatchingEngine;
import HighThroughPutExchange.MatchingEngine.Order;
import HighThroughPutExchange.MatchingEngine.Side;
import HighThroughPutExchange.MatchingEngine.Status;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BotTest {
    public static void main(String[] args) {
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
            Thread producer = new Thread(() -> {
                Random random = new Random();
                while (true) {
                    int price = 100 + random.nextInt(51); // 100..150
                    int volume = 1 + random.nextInt(10); // 1..10

                    double rng = random.nextDouble();
                    if (rng < 0.25) {
                        String randomUser = users.get(random.nextInt(users.size()));
                        queue.add(() -> {
                            Order bidOrder = new Order(randomUser, ticker, price, volume, Side.BID, Status.ACTIVE);
                            System.out.println("Bid Order Added: " + bidOrder);
                            matchingEngine.bidLimitOrder(randomUser, bidOrder);
                        });
                    } else if (rng < 0.5) {
                        queue.add(() -> {
                            String randomUser = users.get(random.nextInt(users.size()));
                            Order askOrder = new Order(randomUser, ticker, price, volume, Side.ASK, Status.ACTIVE);
                            System.out.println("Ask Order Added: " + askOrder);
                            matchingEngine.askLimitOrder(randomUser, askOrder);
                        });
                    } else if (rng < 0.75) {
                        queue.add(() -> {
                            String randomUser = users.get(random.nextInt(users.size()));
                            int quantity = 1 + random.nextInt(10);
                            matchingEngine.bidMarketOrder(randomUser, ticker, quantity);
                        });
                    } else {
                        queue.add(() -> {
                            String randomUser = users.get(random.nextInt(users.size()));
                            int quantity = 1 + random.nextInt(10);
                            matchingEngine.askMarketOrder(randomUser, ticker, quantity);
                        });
                    }

                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            });
            producer.setDaemon(true);
            producer.start();
        }

        int count = 0;
        while (true) {
            Runnable action = queue.poll();
            if (action == null) {
                continue;
            }

            count++;
            action.run();

            if (count % 50 == 0) {
                matchingEngine.removeAll("User1");
                matchingEngine.removeAll("User2");
                System.out.println("Reset Occurring");

                int balance1 = matchingEngine.getUserBalance("User1");
                int balance2 = matchingEngine.getUserBalance("User2");
                int total = balance1 + balance2;

                if (total > 20001) {
                    throw new RuntimeException("Money created out of nowhere: " + total);
                }
                if (total < 19999) {
                    throw new RuntimeException("Money destroyed: " + total);
                }
            }
        }
    }
}
