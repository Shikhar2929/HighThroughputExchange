package HighThroughPutExchange.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import HighThroughPutExchange.api.ServerApplication;
import HighThroughPutExchange.common.OrderbookSeqLog;
import HighThroughPutExchange.common.OrderbookUpdate;
import HighThroughPutExchange.common.SeqGenerator;
import HighThroughPutExchange.matchingengine.MatchingEngine;
import HighThroughPutExchange.matchingengine.PriceChange;
import HighThroughPutExchange.matchingengine.Side;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SeqController.class)
@AutoConfigureMockMvc
class SeqControllerTest {
    @Autowired private MockMvc mockMvc;

    @MockBean private SeqGenerator seqGenerator;

    @MockBean private MatchingEngine matchingEngine;

    @MockBean private OrderbookSeqLog orderbookSeqLog;

    // Present in other controller tests; included to satisfy any wiring
    // expectations.
    @MockBean private ServerApplication app;

    @Test
    void getLatestSeq_success() throws Exception {
        when(seqGenerator.get()).thenReturn(123L);

        mockMvc.perform(get("/latestSeq"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message.errorCode").value(0))
                .andExpect(jsonPath("$.latestSeq").value(123));
    }

    @Test
    void getUpdates_missingSeq_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/updates")).andExpect(status().isBadRequest());
    }

    @Test
    void getUpdates_success_returnsUpdate() throws Exception {
        long seq = 11L;
        OrderbookUpdate u11 =
                new OrderbookUpdate(11L, List.of(new PriceChange("ABC", 100, 7, Side.BID)));
        when(orderbookSeqLog.getBySeq(seq)).thenReturn(Optional.of(u11));

        mockMvc.perform(get("/updates").param("seq", String.valueOf(seq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message.errorCode").value(0))
                .andExpect(jsonPath("$.seq").value(11))
                .andExpect(jsonPath("$.update").isMap())
                .andExpect(jsonPath("$.update.seq").value(11))
                .andExpect(jsonPath("$.update.priceChanges").isArray())
                .andExpect(jsonPath("$.update.priceChanges[0].ticker").value("ABC"));
    }

    @Test
    void getUpdates_invalidSeq_returnsBadRequest() throws Exception {
        when(orderbookSeqLog.getBySeq(123L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/updates").param("seq", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.errorCode").value(8));
    }

    @Test
    void snapshot_success_returnsRawSnapshotJsonAndLatestSeq() throws Exception {
        when(seqGenerator.get()).thenReturn(777L);
        when(matchingEngine.serializeOrderBooks())
                .thenReturn("{\"ticker\":\"ABC\",\"bids\":[],\"asks\":[]}");

        mockMvc.perform(post("/snapshot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latestSeq").value(777))
                .andExpect(jsonPath("$.snapshot.ticker").value("ABC"))
                .andExpect(jsonPath("$.snapshot.bids").isArray())
                .andExpect(jsonPath("$.snapshot.asks").isArray());
    }
}
