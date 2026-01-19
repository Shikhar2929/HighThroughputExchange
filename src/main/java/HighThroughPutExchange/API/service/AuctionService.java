package HighThroughPutExchange.api.service;

import HighThroughPutExchange.api.dtos.requests.BidAuctionRequest;
import HighThroughPutExchange.api.dtos.responses.BidAuctionResponse;
import HighThroughPutExchange.api.dtos.responses.GetLeadingAuctionBidResponse;
import HighThroughPutExchange.auction.Auction;
import HighThroughPutExchange.common.Message;
import HighThroughPutExchange.common.TaskFuture;
import HighThroughPutExchange.common.TaskQueue;
import org.springframework.stereotype.Service;

@Service
public class AuctionService {
    private final Auction auction;

    public AuctionService(Auction auction) {
        this.auction = auction;
    }

    public GetLeadingAuctionBidResponse getLeadingAuctionBid() {
        TaskFuture<GetLeadingAuctionBidResponse> future = new TaskFuture<>();
        future.setData(new GetLeadingAuctionBidResponse(Message.SUCCESS.toString()));
        TaskQueue.addTask(
                () -> {
                    future.getData().setUser(this.getBestUser());
                    future.getData().setBid(this.getBestBid());
                    future.markAsComplete();
                });
        future.waitForCompletion();
        return future.getData();
    }

    public GetLeadingAuctionBidResponse terminateAuction() {
        TaskFuture<GetLeadingAuctionBidResponse> future = new TaskFuture<>();
        future.setData(new GetLeadingAuctionBidResponse(Message.SUCCESS.toString()));

        TaskQueue.addTask(
                () -> {
                    future.getData().setUser(this.getBestUser());
                    future.getData().setBid(this.getBestBid());
                    this.executeAuction();
                    this.resetAuction();
                    future.markAsComplete();
                });
        future.waitForCompletion();
        return future.getData();
    }

    public BidAuctionResponse bidAuctionInvalid() {
        return new BidAuctionResponse(
                String.format(
                        "{\"errorCode\": %d, \"errorMessage\": \"%s\"}",
                        Message.BAD_INPUT.getErrorCode(),
                        String.format(
                                "Bad Input! Bid amount cannot exceed %d.", this.getMaxBid())));
    }

    public BidAuctionResponse bidAuction(BidAuctionRequest form) {
        TaskQueue.addTask(() -> this.placeBid(form.getUsername(), form.getBid()));

        return new BidAuctionResponse(
                String.format(
                        "{\"errorCode\": %d, \"errorMessage\": \"%s\"}",
                        Message.SUCCESS.getErrorCode(),
                        String.format("Success! Placed auction bid for %d.", form.getBid())));
    }

    public boolean isValid(String user, int bid) {
        return auction.isValid(user, bid);
    }

    private String getBestUser() {
        return auction.getBestUser();
    }

    private int getBestBid() {
        return auction.getBestBid();
    }

    private void executeAuction() {
        auction.executeAuction();
    }

    private void resetAuction() {
        auction.reset();
    }

    private int getMaxBid() {
        return auction.getMaxBid();
    }

    private void placeBid(String user, int bid) {
        auction.placeBid(user, bid);
    }
}
