package HighThroughPutExchange.API;

import HighThroughPutExchange.API.api_objects.requests.StartSocketRequest;
import HighThroughPutExchange.API.api_objects.responses.SocketResponse;
import HighThroughPutExchange.API.authentication.AdminPageAuthenticator;
import HighThroughPutExchange.MatchingEngine.PriceChange;
import HighThroughPutExchange.MatchingEngine.RecentTrades;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.socket.messaging.SessionConnectEvent;

@Controller
public class SocketController {

    @Autowired
    private SimpMessagingTemplate template;
    @Autowired
    private SimpUserRegistry simpUserRegistry;

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
        String recentTradesJson = RecentTrades.getRecentTradesAsJson();
        if (!recentTradesJson.isEmpty() && !recentTradesJson.equals("[]")) { // Ensure JSON is not empty
            sendMessage(new SocketResponse(recentTradesJson));
        } else {
            sendMessage(new SocketResponse("No recent trades"));
        }
    }
    @Scheduled(fixedRate = 2000)
    public void sendUserBalances() {
        for (SimpUser user : simpUserRegistry.getUsers()) {
            //For Debugging: System.out.println("Onboarded User: " + user.getName());
            sendUserInfo(user.getName());
        }
    }
    public void sendUserInfo(String username) {
        String resp = username;
        template.convertAndSendToUser(username, "/queue/private", resp);
    }
    /*
    For Debugging:
    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        if (user != null) {
            System.out.println("User connected: " + user.getName());
        } else {
            System.out.println("No Principal found for the session.");
        }
    }
    */
}
