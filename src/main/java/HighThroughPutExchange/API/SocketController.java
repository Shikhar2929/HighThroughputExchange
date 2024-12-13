package HighThroughPutExchange.API;

import HighThroughPutExchange.API.api_objects.requests.StartSocketRequest;
import HighThroughPutExchange.API.api_objects.responses.SocketResponse;
import HighThroughPutExchange.API.authentication.AdminPageAuthenticator;
import HighThroughPutExchange.MatchingEngine.PriceChange;
import HighThroughPutExchange.MatchingEngine.RecentTrades;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

@Controller
public class SocketController {

    @Autowired
    private SimpMessagingTemplate template;

    public void sendMessage(SocketResponse resp) {
        template.convertAndSend("/topic/orderbook", resp);
    }

    @MessageMapping("/start")
    public void startStream(StartSocketRequest req) throws Exception {
        if (!AdminPageAuthenticator.getInstance().authenticate(req)) {throw new Exception("authentication failed");}
        /*for (int i = 0; i < 50; ++i) {
            sendMessage(new SocketResponse(String.format("Message %d", i)));
            Thread.sleep(500);
        }
         */
    }
    @Scheduled(fixedRate = 500) // sends an update every 500 milliseconds
    public void sendRecentTrades() {
        List<PriceChange> recentTrades = RecentTrades.getRecentTrades();
        if (!recentTrades.isEmpty()) {
            recentTrades.forEach(trade ->
                    sendMessage(new SocketResponse(
                            String.format("Ticker: %s, Price: %.2f, Volume: %.2f, Side: %s",
                                    trade.getTicker(), trade.getPrice(), trade.getVolume(), trade.getSide())
                    ))
            );
        } else {
            sendMessage(new SocketResponse("No recent trades"));
        }
    }

}
