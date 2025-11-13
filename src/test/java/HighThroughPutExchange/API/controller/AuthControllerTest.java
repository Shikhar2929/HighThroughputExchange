package HighThroughPutExchange.API.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import HighThroughPutExchange.API.authentication.RateLimiter;
import HighThroughPutExchange.API.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private RateLimiter rateLimiter;

  @Test
  void adminPage_success() throws Exception {
    when(authService.authenticateAdmin(any())).thenReturn(true);

    String body = "{\"adminUsername\":\"root\",\"adminPassword\":\"pw\"}";
    mockMvc
        .perform(post("/admin_page").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk());
  }

  @Test
  void privatePage_rateLimited() throws Exception {
    when(authService.authenticatePrivate(any())).thenReturn(true);
    when(rateLimiter.processRequest(any())).thenReturn(false);

    String body = "{\"username\":\"trader\",\"sessionToken\":\"tok\"}";
    mockMvc
        .perform(post("/privatePage").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isTooManyRequests());
  }
}
