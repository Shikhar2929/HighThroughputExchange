package HighThroughPutExchange.API.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import HighThroughPutExchange.API.ServerApplication;
import HighThroughPutExchange.API.State;
import HighThroughPutExchange.API.authentication.BotAuthenticator;
import HighThroughPutExchange.API.authentication.PrivatePageAuthenticator;
import HighThroughPutExchange.API.authentication.RateLimiter;
import HighThroughPutExchange.API.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private RateLimiter rateLimiter;

    @MockBean
    private ServerApplication app;
    
    @MockBean
    private PrivatePageAuthenticator privatePageAuthenticator;
    
    @MockBean
    private BotAuthenticator botAuthenticator;

  @Test
  void limitOrder_success() throws Exception {
    when(app.getState()).thenReturn(State.TRADE);
    when(privatePageAuthenticator.authenticate(any())).thenReturn(true);
    when(rateLimiter.processRequest(any())).thenReturn(true);
    when(orderService.placeLimitOrder(
            Mockito.eq("trader"),
            Mockito.eq("AAPL"),
            Mockito.eq(100),
            Mockito.eq(1),
            Mockito.eq(true)))
        .thenReturn("OK");

    String body =
        """
        {
            "username": "trader",
            "sessionToken": "tok",
            "ticker": "AAPL",
            "price": 100,
            "volume": 1,
            "bid": true
        }
        """;
    mockMvc
        .perform(post("/limit_order").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk());
  }
}
