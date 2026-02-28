package hte.api.service;

import hte.api.dtos.requests.BidAuctionRequest;
import hte.api.dtos.responses.BidAuctionResponse;
import hte.api.dtos.responses.GetLeadingAuctionBidResponse;
import hte.auction.Auction;
import hte.auction.AuctionResult;
import hte.common.Message;
import hte.common.TaskFuture;
import hte.common.TaskQueue;
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
                    AuctionResult result = this.getAuctionResult();
                    future.getData().setUser(result.getUser());
                    future.getData().setBid(result.getBid());
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
                    AuctionResult result = this.getAuctionResult();
                    future.getData().setUser(result.getUser());
                    future.getData().setBid(result.getBid());
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

    private AuctionResult getAuctionResult() {
        return auction.getAuctionResult();
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
