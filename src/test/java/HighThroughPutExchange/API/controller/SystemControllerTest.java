package HighThroughPutExchange.API.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import HighThroughPutExchange.API.ServerApplication;
import HighThroughPutExchange.API.authentication.RateLimiter;
import HighThroughPutExchange.API.service.AuthService;
import HighThroughPutExchange.API.service.SystemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SystemController.class)
@AutoConfigureMockMvc
class SystemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private SystemService systemService;

    @MockBean
    private RateLimiter rateLimiter;

    @MockBean
    private ServerApplication app;

  @Test
  void getDetails_success() throws Exception {
    when(authService.authenticatePrivate(any())).thenReturn(true);
    when(rateLimiter.processRequest(any())).thenReturn(true);
    when(systemService.getUserDetails("trader")).thenReturn("DETAILS");

    String body = "{\"username\":\"trader\",\"sessionToken\":\"tok\"}";
    mockMvc
        .perform(post("/get_details").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userDetails").value("DETAILS"));
  }
}
