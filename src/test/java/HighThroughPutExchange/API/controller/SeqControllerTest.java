package HighThroughPutExchange.API.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import HighThroughPutExchange.API.ServerApplication;
import HighThroughPutExchange.Common.OrderbookSeqLog;
import HighThroughPutExchange.Common.OrderbookUpdate;
import HighThroughPutExchange.Common.SeqGenerator;
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

@WebMvcTest(controllers = SeqController.class)
@AutoConfigureMockMvc
class SeqControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeqGenerator seqGenerator;

    @MockBean
    private MatchingEngine matchingEngine;

    @MockBean
    private OrderbookSeqLog orderbookSeqLog;

    // Present in other controller tests; included to satisfy any wiring
    // expectations.
    @MockBean
    private ServerApplication app;

  @Test
  void getLatestSeq_success() throws Exception {
    when(seqGenerator.get()).thenReturn(123L);

    mockMvc
        .perform(get("/latestSeq"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.latestSeq").value(123));
  }

    @Test
    void getUpdates_missingFrom_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/updates")).andExpect(status().isBadRequest());
    }

    @Test
    void getUpdates_success_returnsMetadataAndUpdates() throws Exception {
        long from = 10L;
        when(seqGenerator.get()).thenReturn(999L);
        when(orderbookSeqLog.getMinSeq()).thenReturn(11L);

        OrderbookUpdate u11 = new OrderbookUpdate(11L, List.<PriceChange>of());
        OrderbookUpdate u12 = new OrderbookUpdate(12L, List.of(new PriceChange("ABC", 100, 7, Side.BID)));
        when(orderbookSeqLog.get(from)).thenReturn(List.of(u11, u12));

        mockMvc.perform(get("/updates").param("fromExclusive", String.valueOf(from))).andExpect(status().isOk())
                .andExpect(jsonPath("$.fromExclusive").value(10)).andExpect(jsonPath("$.latestSeq").value(999))
                .andExpect(jsonPath("$.updates").isArray()).andExpect(jsonPath("$.updates.length()").value(2))
                .andExpect(jsonPath("$.updates[0].seq").value(11)).andExpect(jsonPath("$.updates[1].seq").value(12))
                .andExpect(jsonPath("$.updates[1].priceChanges[0].ticker").value("ABC"))
                .andExpect(jsonPath("$.updates[1].priceChanges[0].price").value(100))
                .andExpect(jsonPath("$.updates[1].priceChanges[0].volume").value(7))
                .andExpect(jsonPath("$.updates[1].priceChanges[0].side").value("BID"));
    }

  @Test
  void getUpdates_minSeqUnavailable_returnsGone() throws Exception {
    when(orderbookSeqLog.getMinSeq()).thenReturn(null);
    when(seqGenerator.get()).thenReturn(999L);

    mockMvc
        .perform(get("/updates").param("fromExclusive", "10"))
        .andExpect(status().isGone())
        .andExpect(jsonPath("$.error").value("min-seq-unavailable"))
        .andExpect(jsonPath("$.fromExclusive").value(10))
        .andExpect(jsonPath("$.latestSeq").value(999));
  }

  @Test
  void getUpdates_fromTooOld_returnsGone() throws Exception {
    when(orderbookSeqLog.getMinSeq()).thenReturn(50L);
    when(seqGenerator.get()).thenReturn(999L);

    mockMvc
        .perform(get("/updates").param("fromExclusive", "0"))
        .andExpect(status().isGone())
        .andExpect(jsonPath("$.error").value("from-too-old"))
        .andExpect(jsonPath("$.minAvailableSeq").value(50))
        .andExpect(jsonPath("$.latestSeq").value(999));
  }

  @Test
  void snapshot_success_returnsRawSnapshotJsonAndLatestSeq() throws Exception {
    when(seqGenerator.get()).thenReturn(777L);
    when(matchingEngine.serializeOrderBooks())
        .thenReturn("{\"ticker\":\"ABC\",\"bids\":[],\"asks\":[]}");

    mockMvc
        .perform(post("/snapshot"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.latestSeq").value(777))
        .andExpect(jsonPath("$.snapshot.ticker").value("ABC"))
        .andExpect(jsonPath("$.snapshot.bids").isArray())
        .andExpect(jsonPath("$.snapshot.asks").isArray());
  }
}
