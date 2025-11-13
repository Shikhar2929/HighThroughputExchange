package HighThroughPutExchange.API.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import HighThroughPutExchange.API.database_objects.User;
import HighThroughPutExchange.API.service.AdminService;
import HighThroughPutExchange.API.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc
class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private AuthService authService;

  @Test
  void addUser_success() throws Exception {
    when(authService.authenticateAdmin(any())).thenReturn(true);
    when(adminService.usernameExists("trader")).thenReturn(false);
    when(adminService.addUser(
            Mockito.eq("trader"), Mockito.eq("trader"), Mockito.eq("trader@example.com")))
        .thenReturn(new User("trader", "trader", "KEY1", "KEY2", "a@b.com"));

    String body =
        """
        {
          "adminUsername": "root",
          "adminPassword": "pw",
          "username": "trader",
          "name": "trader",
          "email": "trader@example.com"
        }
        """;
    mockMvc
        .perform(post("/add_user").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message.errorCode").value(0))
        .andExpect(jsonPath("$.apiKey").value("KEY1"))
        .andExpect(jsonPath("$.apiKey2").value("KEY2"));
  }

  @Test
  void setState_success() throws Exception {
    when(authService.authenticateAdmin(any())).thenReturn(true);
    when(adminService.applyState(1)).thenReturn(1);

    String body =
        """
        {
          "adminUsername": "root",
          "adminPassword": "pw",
          "targetState": 1
        }
        """;
    mockMvc
        .perform(post("/set_state").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.newState").value(1))
        .andExpect(jsonPath("$.message.errorCode").value(0));
  }

  @Test
  void leaderboard_success() throws Exception {
    when(authService.authenticateAdmin(any())).thenReturn(true);
    when(adminService.getLeaderboard()).thenReturn(new java.util.ArrayList<>());

    String body =
        """
        {
          "adminUsername": "root",
          "adminPassword": "pw"
        }
        """;
    mockMvc
        .perform(post("/leaderboard").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message.errorCode").value(0));
  }

  @Test
  void setPrice_success() throws Exception {
    when(authService.authenticateAdmin(any())).thenReturn(true);
    when(adminService.setPrice(Mockito.anyMap())).thenReturn("OK");

    String body =
        """
        {
          "adminUsername": "root",
          "adminPassword": "pw",
          "prices": { "AAPL": 100 }
        }
        """;
    mockMvc
        .perform(post("/set_price").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("OK"));
  }

  @Test
  void shutdown_success() throws Exception {
    when(authService.authenticateAdmin(any())).thenReturn(true);

    String body =
        """
        {
          "adminUsername": "root",
          "adminPassword": "pw"
        }
        """;
    mockMvc
        .perform(post("/shutdown").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message.errorCode").value(0));
  }

  @Test
  void addBot_success() throws Exception {
    when(authService.authenticateAdmin(any())).thenReturn(true);
    when(adminService.usernameExists("bot1")).thenReturn(false);
    when(adminService.addBot("bot1")).thenReturn("BOTKEY");

    String body =
        """
        {
          "adminUsername": "root",
          "adminPassword": "pw",
          "username": "bot1"
        }
        """;
    mockMvc
        .perform(post("/add_bot").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.apiKey").value("BOTKEY"))
        .andExpect(jsonPath("$.message.errorCode").value(0));
  }
}
