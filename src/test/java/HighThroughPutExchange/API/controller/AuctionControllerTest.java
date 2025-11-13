package HighThroughPutExchange.API.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import HighThroughPutExchange.API.ServerApplication;
import HighThroughPutExchange.API.State;
import HighThroughPutExchange.API.api_objects.responses.GetLeadingAuctionBidResponse;
import HighThroughPutExchange.API.authentication.RateLimiter;
import HighThroughPutExchange.API.service.AuctionService;
import HighThroughPutExchange.API.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuctionController.class)
@AutoConfigureMockMvc
class AuctionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuctionService auctionService;

    @MockBean
    private AuthService authService;

    @MockBean
    private ServerApplication app;

    @MockBean
    private RateLimiter rateLimiter;

  @Test
  void getLeadingAuctionBid_success() throws Exception {
    when(authService.authenticateAdmin(any())).thenReturn(true);
    when(auctionService.getLeadingAuctionBid())
        .thenReturn(new GetLeadingAuctionBidResponse("SUCCESS"));

    String body = "{\"adminUsername\":\"root\",\"adminPassword\":\"pw\"}";
    mockMvc
        .perform(
            post("/get_leading_auction_bid").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk());
  }

  @Test
  void terminateAuction_locked() throws Exception {
    when(authService.authenticateAdmin(any())).thenReturn(true);
    when(app.getState()).thenReturn(State.STOP);

    String body = "{\"adminUsername\":\"root\",\"adminPassword\":\"pw\"}";
    mockMvc
        .perform(post("/terminate_auction").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isLocked());
  }
}
