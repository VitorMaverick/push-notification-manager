package br.edu.acad.ifma.web.rest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.acad.ifma.app.usecase.notification.SendPushNotificationUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
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
    private SendPushNotificationUseCase sendUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSendPush_accepts_request() throws Exception {
        FcmRequest req = new FcmRequest();
        req.setToken("abcdefghijklmnopqrstuvwxyz");
        req.setTitulo("Hello");
        req.setCorpo("Body");

        mockMvc
            .perform(
                post("/api/fcm/send").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req))
            )
            .andExpect(status().isAccepted());
    }
}
