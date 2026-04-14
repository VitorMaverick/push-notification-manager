package br.edu.acad.ifma.adapters.api.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.acad.ifma.app.domain.notification.NotificationStatus;
import br.edu.acad.ifma.app.domain.notification.PushNotification;
import br.edu.acad.ifma.app.domain.shared.FcmToken;
import br.edu.acad.ifma.app.domain.shared.NotificationBody;
import br.edu.acad.ifma.app.domain.shared.NotificationTitle;
import br.edu.acad.ifma.app.domain.shared.exception.NotificationNotFoundException;
import br.edu.acad.ifma.app.usecase.notification.GetNotificationByIdUseCase;
import br.edu.acad.ifma.app.usecase.notification.GetNotificationHistoryUseCase;
import br.edu.acad.ifma.app.usecase.notification.SendPushNotificationUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = NotificationController.class)
@WithMockUser
class NotificationControllerTest {

    private static final String VALID_TOKEN = "abcdefghijklmnopqrstuvwxyz";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SendPushNotificationUseCase sendUseCase;

    @MockBean
    private GetNotificationHistoryUseCase historyUseCase;

    @MockBean
    private GetNotificationByIdUseCase getByIdUseCase;

    @Test
    void post_returns_202() throws Exception {
        PushNotification n = PushNotification.builder()
            .withId(1L)
            .withTitle(new NotificationTitle("Hello"))
            .withBody(new NotificationBody("World"))
            .withRecipientToken(new FcmToken(VALID_TOKEN))
            .withStatus(NotificationStatus.SENT)
            .withFcmMessageId("fcm-001")
            .withCreatedAt(Instant.now())
            .build();
        when(sendUseCase.execute(any())).thenReturn(n);

        String body = objectMapper.writeValueAsString(
            new java.util.HashMap<String, String>() {
                {
                    put("deviceToken", VALID_TOKEN);
                    put("title", "Hello");
                    put("body", "World");
                }
            }
        );

        mockMvc
            .perform(post("/api/v1/notifications").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    void post_invalid_returns_400() throws Exception {
        String body = "{\"deviceToken\":\"\",\"title\":\"\",\"body\":\"\"}";
        mockMvc
            .perform(post("/api/v1/notifications").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void get_list_returns_200() throws Exception {
        when(historyUseCase.execute(any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/notifications")).andExpect(status().isOk());
    }

    @Test
    void get_by_id_not_found_returns_404() throws Exception {
        when(getByIdUseCase.execute(any())).thenThrow(new NotificationNotFoundException("Notification not found: 99"));
        mockMvc.perform(get("/api/v1/notifications/99")).andExpect(status().isNotFound());
    }
}
