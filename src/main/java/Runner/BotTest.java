package Runner;

public class BotTest {
    /*
     * private static final String API_BASE_URL = "http://localhost:8080"; public
     * static void main(String[] args) { MatchingEngine matchingEngine = new
     * MatchingEngine(); ConcurrentLinkedQueue<Runnable> queue = new
     * ConcurrentLinkedQueue<>(); List<String> users = Arrays.asList("User1",
     * "User2"); String ticker = "AAPL"; matchingEngine.initializeTicker(ticker);
     * matchingEngine.initializeUserBalance("User1", 10000.0);
     * matchingEngine.initializeUserBalance("User2", 10000.0);
     * matchingEngine.initializeUserTickerVolume("User1", ticker, 100.0);
     * matchingEngine.initializeUserTickerVolume("User2", ticker, 100.0);
     *
     * // spin up 10 threads for (int i = 0; i < 2; i++) { new Thread(new Runnable()
     * {
     *
     * @Override public void run() { while (true) { double price = 100 +
     * Math.random() * 50; // Random price between 100 and 150 double volume = 1 +
     * Math.random() * 10; // Random volume between 1 and 10 double rng =
     * Math.random(); if (rng < 0.25) { String randomUser = users.get(new
     * Random().nextInt(users.size())); // Add a bid order to the queue queue.add(()
     * -> { Order bidOrder = new Order(randomUser, ticker, price, volume, Side.BID,
     * Status.ACTIVE); System.out.println("Bid Order Added: " + bidOrder);
     * matchingEngine.bidLimitOrder(randomUser, bidOrder);
     * //System.out.println("Bid Order Status: " + bidOrder.getStatus()); }); } else
     * if (rng < 0.5) { // Add an ask order to the queue queue.add(() -> { String
     * randomUser = users.get(new Random().nextInt(users.size())); Order askOrder =
     * new Order(randomUser, ticker, price, volume, Side.ASK, Status.ACTIVE);
     * System.out.println("Ask Order Added: " + askOrder);
     * matchingEngine.askLimitOrder(randomUser, askOrder); }); } else if (rng <
     * 0.75) { queue.add(() -> { String randomUser = users.get(new
     * Random().nextInt(users.size())); double quantity = Math.random() * 10 + 1;
     * matchingEngine.bidMarketOrder(randomUser, ticker, quantity); }); } else {
     * queue.add(() -> { String randomUser = users.get(new
     * Random().nextInt(users.size())); double quantity = Math.random() * 10 + 1;
     * matchingEngine.askMarketOrder(randomUser, ticker, quantity); }); }
     *
     * try { Thread.sleep(00); // Delay to simulate real-time trading } catch
     * (InterruptedException e) { e.printStackTrace(); } } } }).start();
     *
     * } int count = 0; while (true) { while (!queue.isEmpty()) { count++; Runnable
     * action = queue.poll(); if (action != null) { //System.out.println(action);
     * action.run(); if (count % 50 == 0) { matchingEngine.removeAll("User1");
     * matchingEngine.removeAll("User2"); System.out.println("Reset Occuring");
     * double balance1 = matchingEngine.getUserBalance("User1"); double balance2 =
     * matchingEngine.getUserBalance("User2"); double total = balance1 + balance2;
     * /*System.out.println("User 1 Balance: " + balance1);
     * System.out.println("User 2 Balance:" + balance2);
     * System.out.println("User 1 Owned: " +
     * matchingEngine.getTickerBalance("User1", ticker));
     * System.out.println("User 2 Owned: " +
     * matchingEngine.getTickerBalance("User2", ticker));
     * System.out.println("Total Balance " + total); if (total > 20001) throw new
     * RuntimeException("Money created out of nowhere"); if (total < 19999) throw
     * new RuntimeException("Money destroyed"); } } } } }
     */
}
