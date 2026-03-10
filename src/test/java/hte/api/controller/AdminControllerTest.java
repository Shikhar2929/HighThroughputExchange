package hte.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hte.api.entities.User;
import hte.api.service.AdminService;
import hte.api.service.AuthService;
import hte.common.SeqGenerator;
import hte.matchingengine.MatchingEngine;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc
class AdminControllerTest {
    @Autowired private MockMvc mockMvc;

    @MockBean private AdminService adminService;

    @MockBean private AuthService authService;

    @MockBean private SimpMessagingTemplate messagingTemplate;

    @MockBean private SimpUserRegistry simpUserRegistry;

    @MockBean private SeqGenerator seqGenerator;

    @MockBean private MatchingEngine matchingEngine;

    @Test
    void addUser_success() throws Exception {
        when(authService.authenticateAdmin(any())).thenReturn(true);
        when(adminService.usernameExists("trader")).thenReturn(false);
        when(adminService.addUser(
                        Mockito.eq("trader"),
                        Mockito.eq("trader"),
                        Mockito.eq("trader@example.com")))
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
        mockMvc.perform(post("/add_user").contentType(MediaType.APPLICATION_JSON).content(body))
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
        mockMvc.perform(post("/set_state").contentType(MediaType.APPLICATION_JSON).content(body))
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
        mockMvc.perform(post("/leaderboard").contentType(MediaType.APPLICATION_JSON).content(body))
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
        mockMvc.perform(post("/set_price").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"));
    }

    @Test
    void setTickers_success() throws Exception {
        when(authService.authenticateAdmin(any())).thenReturn(true);
        when(adminService.setTickers(Mockito.any())).thenReturn(new String[] {"AAPL", "GOOG"});

        String body =
                """
        {
          "adminUsername": "root",
          "adminPassword": "pw",
          "tickers": ["AAPL", "GOOG"]
        }
        """;

        mockMvc.perform(post("/set_tickers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message.errorCode").value(0))
                .andExpect(jsonPath("$.tickers[0]").value("AAPL"))
                .andExpect(jsonPath("$.tickers[1]").value("GOOG"));
    }

    @Test
    void setTickers_unauthorized() throws Exception {
        when(authService.authenticateAdmin(any())).thenReturn(false);

        String body =
                """
        {
          "adminUsername": "root",
          "adminPassword": "bad",
          "tickers": ["AAPL"]
        }
        """;

        mockMvc.perform(post("/set_tickers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message.errorCode").value(1));
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
        mockMvc.perform(post("/shutdown").contentType(MediaType.APPLICATION_JSON).content(body))
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
        mockMvc.perform(post("/add_bot").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiKey").value("BOTKEY"))
                .andExpect(jsonPath("$.message.errorCode").value(0));
    }

    @Test
    void sendUserWsMessage_success() throws Exception {
        when(authService.authenticateAdmin(any())).thenReturn(true);

        String body =
                """
        {
          \"adminUsername\": \"root\",
          \"adminPassword\": \"pw\",
          \"targetUsername\": \"trader\",
          \"message\": \"hello from admin\"
        }
        """;

        mockMvc.perform(
                        post("/send_user_ws_message")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message.errorCode").value(0));

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate)
                .convertAndSendToUser(eq("trader"), eq("/queue/admin"), payloadCaptor.capture());

        Object payloadObj = payloadCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertTrue(payloadObj instanceof java.util.Map);

        java.util.Map<?, ?> payload = (java.util.Map<?, ?>) payloadObj;
        org.junit.jupiter.api.Assertions.assertEquals("admin_message", payload.get("type"));
        org.junit.jupiter.api.Assertions.assertEquals("hello from admin", payload.get("message"));
    }

    @Test
    void sendUserWsMessage_broadcast_sendsToEachConnectedUser() throws Exception {
        when(authService.authenticateAdmin(any())).thenReturn(true);

        SimpUser u1 = Mockito.mock(SimpUser.class);
        when(u1.getName()).thenReturn("alice");
        SimpUser u2 = Mockito.mock(SimpUser.class);
        when(u2.getName()).thenReturn("bob");

        java.util.Set<SimpUser> users = new java.util.HashSet<>();
        users.add(u1);
        users.add(u2);
        when(simpUserRegistry.getUsers()).thenReturn(users);

        String body =
                """
        {
          \"adminUsername\": \"root\",
          \"adminPassword\": \"pw\",
          \"targetUsername\": \"*\",
          \"message\": \"announcement\"
        }
        """;

        mockMvc.perform(
                        post("/send_user_ws_message")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message.errorCode").value(0));

        verify(messagingTemplate).convertAndSendToUser(eq("alice"), eq("/queue/admin"), any());
        verify(messagingTemplate).convertAndSendToUser(eq("bob"), eq("/queue/admin"), any());
    }
}
