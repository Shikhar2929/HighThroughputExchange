package HighThroughPutExchange.MatchingEngine;

public class LeaderboardEntry implements Comparable<LeaderboardEntry> {

    private String username;
    private double balance;

    public LeaderboardEntry(String username, double balance) {
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

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public int compareTo(LeaderboardEntry o) {
        if (o == null) {return 1;}
        int comp = Double.compare(this.balance, o.balance);
        if (comp == 0) {
            return this.username.compareTo(o.username);
        }
        return comp;
    }
}
