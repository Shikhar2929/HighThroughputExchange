package hte.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hte.api.auth.AdminPageAuthenticator;
import hte.api.dtos.requests.StartSocketRequest;
import hte.api.dtos.responses.SocketResponse;
import hte.common.ChartTrackerSingleton;
import hte.common.OHLCData;
import hte.common.OrderbookSeqLog;
import hte.common.SeqGenerator;
import hte.common.TaskQueue;
import hte.matchingengine.MatchingEngine;
import hte.matchingengine.PriceChange;
import hte.matchingengine.RecentTrades;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Controller
public class SocketController {
    private static final Logger logger = LoggerFactory.getLogger(SocketController.class);
    @Autowired private SimpMessagingTemplate template;
    @Autowired private SimpUserRegistry simpUserRegistry;
    @Autowired private AdminPageAuthenticator adminPageAuthenticator;
    @Autowired private SeqGenerator seqGenerator;
    @Autowired private OrderbookSeqLog orderbookSeqLog;

    @Autowired private MatchingEngine matchingEngine;

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

    @Scheduled(fixedRate = 200)
    public void sendRecentTrades() {
        // get new trades that happened since last update
        List<PriceChange> recentTrades = RecentTrades.getRecentTrades();
        String recentTradesJson = RecentTrades.recentTradesToJson(recentTrades);

        if (recentTrades != null
                && !recentTrades.isEmpty()
                && !recentTradesJson.isEmpty()
                && !recentTradesJson.equals("[ ]")) {
            // Allocate seq and append to replay log in one step (only for real updates)
            Long seq = orderbookSeqLog.nextSeqAndAppend(seqGenerator, recentTrades);
            sendMessage(new SocketResponse(recentTradesJson, seq));
        } else {
            // Heartbeat: do not allocate a seq. Use latest known seq (may repeat).
            long lastKnownSeq = seqGenerator.get() - 1;
            sendMessage(new SocketResponse("No recent trades", lastKnownSeq));
        }
    }

    @Scheduled(fixedRate = 200)
    public void sendUserBalances() {
        TaskQueue.addTask(
                () -> {
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
                logger.warn("Error serializing chart data", e);
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
