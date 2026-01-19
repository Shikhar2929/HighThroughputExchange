package hte.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hte.api.ServerApplication;
import hte.api.State;
import hte.api.dtos.responses.OperationResponse;
import hte.api.service.AuthService;
import hte.api.service.BatchService;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = BatchController.class)
@AutoConfigureMockMvc
class BatchControllerTest {
    @Autowired private MockMvc mockMvc;

    @MockBean private BatchService batchService;

    @MockBean private AuthService authService;

    @MockBean private ServerApplication app;

    @Test
    void batch_success_empty() throws Exception {
        when(authService.authenticateBot(any())).thenReturn(true);
        when(app.getState()).thenReturn(State.TRADE);
        when(batchService.getMaxOperations()).thenReturn(20);
        when(batchService.processBatch(Mockito.eq("bot1"), Mockito.anyList()))
                .thenReturn(new ArrayList<OperationResponse>());

        String body =
                """
        {
            "username": "bot1",
            "sessionToken": "t",
            "operations": []
        }
        """;
        mockMvc.perform(post("/batch").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }
}
