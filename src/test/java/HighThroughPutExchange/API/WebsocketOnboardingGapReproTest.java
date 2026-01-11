package HighThroughPutExchange.API;

import static org.junit.jupiter.api.Assertions.*;

import HighThroughPutExchange.API.database_objects.User;
import HighThroughPutExchange.Database.localdb.LocalDBTable;
import HighThroughPutExchange.MatchingEngine.MatchingEngine;
import HighThroughPutExchange.MatchingEngine.Order;
import HighThroughPutExchange.MatchingEngine.Side;
import HighThroughPutExchange.MatchingEngine.Status;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.task.scheduling.enabled=false"})
class WebsocketOnboardingGapReproTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SocketController socketController;

    @Autowired
    private MatchingEngine matchingEngine;

    @Autowired
    @Qualifier("usersTable")
    private LocalDBTable<User> usersTable;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void updateBetweenSnapshotAndSubscribe_canBeLost() throws Exception {
        // Arrange: authenticate a real user from data.json via /buildup
        String username = "team49";
        User user = usersTable.getItem(username);
        assertNotNull(user, "Expected test user to exist in data.json");

        String sessionToken = doBuildupAndGetSessionToken(username, user.getApiKey());

        // Step 1: Client gets a snapshot (this is the vulnerable window)
        String snapshotBefore = matchingEngine.serializeOrderBooks();

        // Step 2: A book update happens BEFORE the client is subscribed
        int priceBeforeSubscribe = 100;
        matchingEngine.bidLimitOrder(
                username,
                new Order(username, "A", priceBeforeSubscribe, 1, Side.BID, Status.ACTIVE));

        // Force the websocket broadcaster to flush-and-clear changes while nobody is subscribed.
        // This simulates: update sent while client is still onboarding / before subscribe.
        socketController.sendRecentTrades();

        // Step 3: Client subscribes
        BlockingQueue<String> receivedContents = new LinkedBlockingQueue<>();
        WebSocket socket = connectAndSubscribe(username, sessionToken, receivedContents);
        try {
            // Ensure the subscription is live before we send the post-subscribe update.
            // (STOMP receipts are not reliably emitted in this setup.)
            socketController.sendRecentTrades();
            assertNotNull(
                awaitAnySocketContent(receivedContents, 2, TimeUnit.SECONDS),
                "Expected to receive at least one websocket message after subscribing");
            receivedContents.clear();

            // Step 4: Another update happens AFTER subscribe; client should receive this one.
            int priceAfterSubscribe = 101;
            matchingEngine.bidLimitOrder(
                    username,
                    new Order(username, "A", priceAfterSubscribe, 1, Side.BID, Status.ACTIVE));
            socketController.sendRecentTrades();

                String payload =
                    awaitTradePayloadContainingPrice(
                        receivedContents, priceAfterSubscribe, 3, TimeUnit.SECONDS);
                assertNotNull(
                    payload,
                    "Expected to receive a websocket update containing the post-subscribe price");

                // Assert: client did NOT receive the pre-subscribe update (this is the bug)
                assertFalse(
                    payload.contains("\"price\" : " + priceBeforeSubscribe),
                    "Pre-subscribe update was lost; this reproduces the onboarding gap bug");

                // Stronger check: ensure the pre-subscribe update never arrives later.
                assertFalse(
                    anyPayloadContainsPrice(receivedContents, priceBeforeSubscribe, 800, TimeUnit.MILLISECONDS),
                    "Pre-subscribe update should not arrive after subscribing; expected it to be lost");
            } finally {
            socket.sendClose(WebSocket.NORMAL_CLOSURE, "bye").join();
        }

        // Sanity check: server state changed vs snapshotBefore
        String snapshotAfter = matchingEngine.serializeOrderBooks();
        assertNotEquals(snapshotBefore, snapshotAfter, "Expected server-side orderbooks to change during the test");
    }

    private String doBuildupAndGetSessionToken(String username, String apiKey) throws Exception {
        String url = "http://localhost:" + port + "/buildup";
        String requestBody =
                "{\n" +
                "  \"username\": \"" + username + "\",\n" +
                "  \"apiKey\": \"" + apiKey + "\"\n" +
                "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        String responseBody = restTemplate.postForObject(url, entity, String.class);
        assertNotNull(responseBody);

        JsonNode json = OBJECT_MAPPER.readTree(responseBody);
        JsonNode tokenNode = json.get("sessionToken");
        assertNotNull(tokenNode, "Expected buildup response to contain sessionToken");
        return tokenNode.asText();
    }

    private WebSocket connectAndSubscribe(String username, String sessionToken, BlockingQueue<String> receivedContents)
            throws Exception {
        String wsUrl =
                "ws://localhost:"
                        + port
                        + "/exchange-socket?Session-ID="
                        + sessionToken
                        + "&Username="
                        + username;

        BlockingQueue<String> rawFrames = new LinkedBlockingQueue<>();
        Listener listener =
                new Listener() {
                    private final StringBuilder buffer = new StringBuilder();

                    @Override
                    public void onOpen(WebSocket webSocket) {
                        webSocket.request(1);
                    }

                    @Override
                    public java.util.concurrent.CompletionStage<?> onText(
                            WebSocket webSocket, CharSequence data, boolean last) {
                        buffer.append(data);
                        if (last) {
                            rawFrames.add(buffer.toString());
                            buffer.setLength(0);
                        }
                        webSocket.request(1);
                        return null;
                    }

                    @Override
                    public java.util.concurrent.CompletionStage<?> onClose(
                            WebSocket webSocket, int statusCode, String reason) {
                        return null;
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        // Surface errors via the rawFrames queue; the test will fail on timeout.
                        rawFrames.add("ERROR: " + error.getMessage());
                    }
                };

        WebSocket socket =
                HttpClient.newHttpClient().newWebSocketBuilder().buildAsync(URI.create(wsUrl), listener)
                        .get(5, TimeUnit.SECONDS);

        // STOMP CONNECT
        socket.sendText("CONNECT\naccept-version:1.1,1.0\nhost:localhost\n\n\u0000", true).join();
        awaitConnectedFrame(rawFrames, 2, TimeUnit.SECONDS);

        // STOMP SUBSCRIBE
        socket.sendText(
                "SUBSCRIBE\nid:sub-0\ndestination:/topic/orderbook\nack:auto\n\n\u0000",
                        true)
                .join();

        // Start a lightweight parser that converts MESSAGE frames into extracted SocketResponse.content
        // (which itself is a JSON string representing an array of PriceChange objects).
        Thread parserThread =
                new Thread(
                        () -> {
                            while (true) {
                                try {
                                    String frame = rawFrames.take();
                                    if (frame.startsWith("ERROR:")) {
                                        continue;
                                    }

                                    // A single WebSocket text message may contain multiple STOMP frames.
                                    // Split on the STOMP frame terminator (NUL) and parse each frame body.
                                    for (String stompFrame : frame.split("\u0000")) {
                                        String trimmed = stompFrame.trim();
                                        if (trimmed.isEmpty()) {
                                            continue;
                                        }
                                        if (!trimmed.startsWith("MESSAGE") || !trimmed.contains("\n\n")) {
                                            continue;
                                        }

                                        String[] parts = trimmed.split("\\n\\n", 2);
                                        if (parts.length < 2) {
                                            continue;
                                        }
                                        String body = parts[1].replace("\u0000", "").trim();
                                        if (body.isEmpty()) {
                                            continue;
                                        }
                                        JsonNode json = OBJECT_MAPPER.readTree(body);
                                        JsonNode contentNode = json.get("content");
                                        if (contentNode != null) {
                                            receivedContents.add(contentNode.asText());
                                        }
                                    }
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    return;
                                } catch (Exception ignored) {
                                    // Ignore malformed frames.
                                }
                            }
                        },
                        "stomp-test-parser");
        parserThread.setDaemon(true);
        parserThread.start();

        return socket;
    }

    private void awaitConnectedFrame(BlockingQueue<String> rawFrames, long timeout, TimeUnit unit)
            throws InterruptedException {
        long deadlineNanos = System.nanoTime() + unit.toNanos(timeout);
        while (System.nanoTime() < deadlineNanos) {
            String frame = rawFrames.poll(200, TimeUnit.MILLISECONDS);
            if (frame == null) {
                continue;
            }

            for (String stompFrame : frame.split("\u0000")) {
                if (stompFrame.trim().startsWith("CONNECTED")) {
                    return;
                }
            }
        }
        fail("Timed out waiting for STOMP CONNECTED frame");
    }

    private String awaitTradePayloadContainingPrice(
            BlockingQueue<String> receivedContents,
            int requiredPrice,
            long timeout,
            TimeUnit unit)
            throws InterruptedException {
        long deadlineNanos = System.nanoTime() + unit.toNanos(timeout);
        while (System.nanoTime() < deadlineNanos) {
            String content = receivedContents.poll(200, TimeUnit.MILLISECONDS);
            if (content == null) {
                continue;
            }
            // SocketController sometimes emits "No recent trades"; skip until we get a JSON array payload.
            String trimmed = content.trim();
            if (!trimmed.startsWith("[")) {
                continue;
            }
            if (trimmed.contains("\"price\" : " + requiredPrice)) {
                return trimmed;
            }
        }
        return null;
    }

    private String awaitAnySocketContent(BlockingQueue<String> receivedContents, long timeout, TimeUnit unit)
            throws InterruptedException {
        return receivedContents.poll(timeout, unit);
    }

    private boolean anyPayloadContainsPrice(
            BlockingQueue<String> receivedContents, int forbiddenPrice, long timeout, TimeUnit unit)
            throws InterruptedException {
        long deadlineNanos = System.nanoTime() + unit.toNanos(timeout);
        while (System.nanoTime() < deadlineNanos) {
            String content = receivedContents.poll(200, TimeUnit.MILLISECONDS);
            if (content == null) {
                continue;
            }
            String trimmed = content.trim();
            if (trimmed.startsWith("[") && trimmed.contains("\"price\" : " + forbiddenPrice)) {
                return true;
            }
        }
        return false;
    }
}
