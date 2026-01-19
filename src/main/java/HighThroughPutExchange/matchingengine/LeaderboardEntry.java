package HighThroughPutExchange.matchingengine;

public class LeaderboardEntry implements Comparable<LeaderboardEntry> {

    private String username;
    private long balance;

    public LeaderboardEntry(String username, long balance) {
        this.username = username;
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    public int compareTo(LeaderboardEntry o) {
        if (o == null) {
            return 1;
        }
        int comp = Long.compare(this.balance, o.balance);
        if (comp == 0) {
            return this.username.compareTo(o.username);
        }
        return comp;
    }
}
