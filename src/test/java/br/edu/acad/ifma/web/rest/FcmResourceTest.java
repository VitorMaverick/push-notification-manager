package br.edu.acad.ifma.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.acad.ifma.domain.Notification;
import br.edu.acad.ifma.service.notification.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = FcmResource.class)
@WithMockUser
class FcmResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSendPush_usesNotificationService() throws Exception {
        FcmRequest req = new FcmRequest();
        req.setToken("token-123");
        req.setTitulo("Hello");
        req.setCorpo("Body");

        mockMvc
            .perform(
                post("/api/fcm/send").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req))
            )
            .andExpect(status().isAccepted());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService, times(1)).createAndSend(captor.capture());
        Notification sent = captor.getValue();
        assertThat(sent.getChannel()).isNotNull();
        assertThat(sent.getRecipientToken()).isEqualTo("token-123");
        assertThat(sent.getSubject()).isEqualTo("Hello");
    }
}
