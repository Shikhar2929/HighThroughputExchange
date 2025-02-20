package HighThroughPutExchange.MatchingEngine;

public class LeaderboardEntry implements Comparable<LeaderboardEntry> {

    private String username;
    private int balance;

    public LeaderboardEntry(String username, int balance) {
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

    public void setBalance(int balance) {
        this.balance = balance;
    }

    @Override
    public int compareTo(LeaderboardEntry o) {
        if (o == null) {return 1;}
        int comp = Integer.compare(this.balance, o.balance);
        if (comp == 0) {
            return this.username.compareTo(o.username);
        }
        return comp;
    }
}
