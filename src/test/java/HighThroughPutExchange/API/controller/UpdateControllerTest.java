package HighThroughPutExchange.API.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import HighThroughPutExchange.API.ServerApplication;
import HighThroughPutExchange.Common.OrderbookUpdate;
import HighThroughPutExchange.Common.OrderbookUpdateLog;
import HighThroughPutExchange.Common.UpdateIdGenerator;
import HighThroughPutExchange.MatchingEngine.MatchingEngine;
import HighThroughPutExchange.MatchingEngine.PriceChange;
import HighThroughPutExchange.MatchingEngine.Side;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UpdateController.class)
@AutoConfigureMockMvc
class UpdateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UpdateIdGenerator updateIdGenerator;

    @MockBean
    private MatchingEngine matchingEngine;

    @MockBean
    private OrderbookUpdateLog orderbookUpdateLog;

    // Present in other controller tests; included to satisfy any wiring
    // expectations.
    @MockBean
    private ServerApplication app;

  @Test
  void getVersion_success() throws Exception {
    when(updateIdGenerator.get()).thenReturn(123L);

    mockMvc
        .perform(get("/version"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").value(123));
  }

    @Test
    void getUpdates_missingFrom_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/updates")).andExpect(status().isBadRequest());
    }

    @Test
    void getUpdates_success_returnsMetadataAndUpdates() throws Exception {
        long from = 10L;
        when(updateIdGenerator.get()).thenReturn(999L);

        OrderbookUpdate u11 = new OrderbookUpdate(11L, List.<PriceChange>of());
        OrderbookUpdate u12 = new OrderbookUpdate(12L, List.of(new PriceChange("ABC", 100, 7, Side.BID)));
        when(orderbookUpdateLog.get(from)).thenReturn(List.of(u11, u12));

        mockMvc.perform(get("/updates").param("from", String.valueOf(from))).andExpect(status().isOk())
                .andExpect(jsonPath("$.fromExclusive").value(10)).andExpect(jsonPath("$.latestSeq").value(999))
                .andExpect(jsonPath("$.updates").isArray()).andExpect(jsonPath("$.updates.length()").value(2))
                .andExpect(jsonPath("$.updates[0].seq").value(11)).andExpect(jsonPath("$.updates[1].seq").value(12))
                .andExpect(jsonPath("$.updates[1].priceChanges[0].ticker").value("ABC"))
                .andExpect(jsonPath("$.updates[1].priceChanges[0].price").value(100))
                .andExpect(jsonPath("$.updates[1].priceChanges[0].volume").value(7))
                .andExpect(jsonPath("$.updates[1].priceChanges[0].side").value("BID"));
    }

  @Test
  void snapshot_success_returnsRawSnapshotJsonAndVersion() throws Exception {
    when(updateIdGenerator.get()).thenReturn(777L);
    when(matchingEngine.serializeOrderBooks())
        .thenReturn("{\"ticker\":\"ABC\",\"bids\":[],\"asks\":[]}");

    mockMvc
        .perform(post("/snapshot"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").value(777))
        .andExpect(jsonPath("$.snapshot.ticker").value("ABC"))
        .andExpect(jsonPath("$.snapshot.bids").isArray())
        .andExpect(jsonPath("$.snapshot.asks").isArray());
  }
}
