package HighThroughPutExchange.API;

import HighThroughPutExchange.API.api_objects.requests.StartSocketRequest;
import HighThroughPutExchange.API.api_objects.responses.SocketResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Controller
public class SocketController {

    @Autowired
    private SimpMessagingTemplate template;

    public void sendMessage(SocketResponse resp) {
        template.convertAndSend("/topic/orderbook", resp);
    }

    @MessageMapping("/start")
    public void startStream(StartSocketRequest req) throws Exception {
        // todo authenticate request
        // todo make authenticators singleton objects
        for (int i = 0; i < 50; ++i) {
            sendMessage(new SocketResponse(String.format("Message %d", i)));
            Thread.sleep(500);
        }
    }
}
