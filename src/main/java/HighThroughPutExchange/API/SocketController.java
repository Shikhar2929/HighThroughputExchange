package HighThroughPutExchange.API;

import HighThroughPutExchange.API.api_objects.requests.StartSocketRequest;
import HighThroughPutExchange.API.api_objects.responses.SocketResponse;
import HighThroughPutExchange.API.authentication.AdminPageAuthenticator;
import HighThroughPutExchange.Common.ChartTrackerSingleton;
import HighThroughPutExchange.Common.MatchingEngineSingleton;
import HighThroughPutExchange.Common.OHLCData;
import HighThroughPutExchange.Common.TaskQueue;
import HighThroughPutExchange.MatchingEngine.MatchingEngine;
import HighThroughPutExchange.MatchingEngine.RecentTrades;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Controller
public class SocketController {

    @Autowired
    private SimpMessagingTemplate template;
    @Autowired
    private SimpUserRegistry simpUserRegistry;
    @Autowired
    private AdminPageAuthenticator adminPageAuthenticator;
    private MatchingEngine matchingEngine = MatchingEngineSingleton.getMatchingEngine();
    private ChartTrackerSingleton chartTrackerSingleton = ChartTrackerSingleton.getInstance();

    public void sendMessage(SocketResponse resp) {
        template.convertAndSend("/topic/orderbook", resp);
    }

    @MessageMapping("/start")
    public void startStream(StartSocketRequest req) throws Exception {
        if (!adminPageAuthenticator.authenticate(req)) {
            throw new Exception("authentication failed");
        }
        /*
         * for (int i = 0; i < 50; ++i) { sendMessage(new
         * SocketResponse(String.format("Message %d", i))); Thread.sleep(500); }
         */
    }

    @Scheduled(fixedRate = 200) // sends an update every 500 milliseconds
    public void sendRecentTrades() {
        String recentTradesJson = RecentTrades.getRecentTradesAsJson();
        if (!recentTradesJson.isEmpty() && !recentTradesJson.equals("[]")) { // Ensure JSON is not empty
            sendMessage(new SocketResponse(recentTradesJson));
        } else {
            sendMessage(new SocketResponse("No recent trades"));
        }
    }

    @Scheduled(fixedRate = 200)
    public void sendUserBalances() {
        TaskQueue.addTask(() -> {
            for (SimpUser user : simpUserRegistry.getUsers()) {
                String userDetailsJson = matchingEngine.getUserDetails(user.getName());
                sendUserInfo(user.getName(), userDetailsJson);
            }
        });
    }

    public void sendUserInfo(String username, String resp) {
        template.convertAndSendToUser(username, "/queue/private", resp);
    }

    @Scheduled(fixedRate = 5000)
    public void sendChartUpdates() {
        Map<String, OHLCData> allCurrentOHLC = chartTrackerSingleton.getCurrentData();
        if (!allCurrentOHLC.isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Map<String, Integer>> formattedChartData = new HashMap<>();

                for (Map.Entry<String, OHLCData> entry : allCurrentOHLC.entrySet()) {
                    String ticker = entry.getKey();
                    OHLCData ohlc = entry.getValue();

                    // Format OHLC data for each ticker
                    Map<String, Integer> ohlcMap = new HashMap<>();
                    ohlcMap.put("open", ohlc.open());
                    ohlcMap.put("high", ohlc.high());
                    ohlcMap.put("low", ohlc.low());
                    ohlcMap.put("close", ohlc.close());

                    formattedChartData.put(ticker, ohlcMap);
                }

                // Convert to JSON
                String jsonPayload = objectMapper.writeValueAsString(formattedChartData);

                // Send the JSON-encoded OHLC data to all WebSocket subscribers
                template.convertAndSend("/topic/chart", jsonPayload);

                // Reset all OHLC data after sending updates
                chartTrackerSingleton.resetAll();

            } catch (Exception e) {
                System.err.println("Error serializing chart data: " + e.getMessage());
            }
        }
    }
    /*
     * For Debugging:
     *
     * @EventListener public void handleSessionConnected(SessionConnectEvent event)
     * { StompHeaderAccessor headerAccessor =
     * StompHeaderAccessor.wrap(event.getMessage()); Principal user =
     * headerAccessor.getUser(); if (user != null) {
     * System.out.println("User connected: " + user.getName()); } else {
     * System.out.println("No Principal found for the session."); } }
     */
}
